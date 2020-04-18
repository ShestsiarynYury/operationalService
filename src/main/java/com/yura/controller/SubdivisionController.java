package com.yura.controller;

import com.yura.entity.CategoryAbsenceCount;
import com.yura.entity.Expenditure;
import com.yura.entity.ExpenditureCategoryCount;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import com.yura.entity.Subdivision;
import com.yura.entity.User;
import com.yura.entity.UserSubdivision;
import com.yura.repository.CategoryAbsenceCountRepository;
import com.yura.repository.ExpenditureCategoryCountRepository;
import com.yura.repository.ExpenditureRepository;
import com.yura.repository.SubdivisionRepository;
import com.yura.repository.UserSubdivisionRepository;

@RestController
public class SubdivisionController {
    @Autowired private SubdivisionRepository subdivisionRepository;
    @Autowired private UserSubdivisionRepository userSubdivisionRepository;
    @Autowired private ExpenditureRepository expenditureRepository;
    @Autowired private ExpenditureCategoryCountRepository expenditureCategoryCountRepository;
    @Autowired private CategoryAbsenceCountRepository categoryAbsenceCountRepository;

    @RequestMapping(value = "/do/subdivision/tree", method = RequestMethod.POST)
    public ModelAndView getTreeSubdivision(HttpServletRequest httpServletRequest,ModelAndView modelAndView) {

        HttpSession httpSession = httpServletRequest.getSession();
        User user = (User) httpSession.getAttribute("user");
        modelAndView.addObject("user", user);

        List<Subdivision> listSubdivision;
        try {
            listSubdivision = subdivisionRepository.findAllOfRoot();		
        } catch (java.lang.NullPointerException exception) {
            modelAndView.setViewName("error");
            return modelAndView;
        }
        modelAndView.addObject("listSubdivision", listSubdivision);	
        modelAndView.setViewName("tree_subdivision");

        return modelAndView;
    }
	
    @RequestMapping(value = "/do/subdivision/form", method = RequestMethod.POST)
    public ModelAndView form() {
		ModelAndView modelAndView = new ModelAndView();
		
        List<Subdivision> listSubdivision = null;
        listSubdivision = this.subdivisionRepository.findAll();		
        modelAndView.addObject("listSubdivision", listSubdivision);
        modelAndView.setViewName("subdivision");

        return modelAndView;
    }
	
	@RequestMapping(value = "/do/subdivision/add", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity add(@RequestBody Subdivision subdivision) {

        ResponseEntity responseEntity = null;
	
        try {
            Subdivision dbParent = this.subdivisionRepository.findByName(subdivision.getParent().getName());
			subdivision.setParent(dbParent);
			this.subdivisionRepository.save(subdivision);
        } catch (Exception exception) {
			responseEntity =  new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
        }

        responseEntity = new ResponseEntity(HttpStatus.OK);
        return responseEntity;
    }
	
	@Transactional()
    @RequestMapping(value = "/do/subdivision/delete", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity delete(@RequestBody Subdivision subdivision) {
        ResponseEntity responseEntity = null;
		
		Subdivision dbSubdivision = null;
		
        try {
            dbSubdivision = this.subdivisionRepository.findByName(subdivision.getName());
        
            List<Subdivision> children = null;
            children = this.subdivisionRepository.getAllChildrenByIdParent(dbSubdivision.getIdSubdivision());

            //1) -------------
            //       0       |
            //       |       |
            //	     *	     |
            //     /   \     |
            //    *   ( * )  |
            //----------------
            if ((children.size()  == 0 || children == null) && dbSubdivision.getParent() != null) {
                // удаляем связанные UserSubdivision если они есть
                if (dbSubdivision.getListUserSubdivision() != null && dbSubdivision.getListUserSubdivision().size() != 0) {
                    Object[] arrayUserSubdivision = null;
                    arrayUserSubdivision = dbSubdivision.getListUserSubdivision().toArray();
                    for (int i = 0; i < arrayUserSubdivision.length; i++) {
                        UserSubdivision userSubdivision = (UserSubdivision) arrayUserSubdivision[i];
                        userSubdivision.getUser().getListUserSubdivision().remove(userSubdivision);
                        userSubdivision.setUser(null);
                        userSubdivision.setSubdivision(null);
                        dbSubdivision.getListUserSubdivision().remove(userSubdivision);
                        this.userSubdivisionRepository.delete(userSubdivision);
                    }
                }

                // удаляем все расходы
                Object[] expenditures = this.expenditureRepository.findAllExpenditureByNameSubdivision(dbSubdivision.getName()).toArray();
                if (expenditures != null && expenditures.length != 0) {
                    for (int i = 0; i < expenditures.length; i++) {
                        this.deleteExpenditure(((Expenditure) expenditures[i]));
                    }
                }

                // удаляем само подразделение
                this.subdivisionRepository.delete(dbSubdivision);
                
                responseEntity = new ResponseEntity(HttpStatus.OK);
                return responseEntity; 
            }

            //2) -------------
            //      0        |
            //      |        |
            //	  ( * )      |
            //	  /	  \      |
            //	 *     *     |
            //----------------
            if ((children.size() != 0 || children != null) && dbSubdivision.getParent() == null) {
                // отвязываем дочерние подразеления
                Object[] arrCh = children.toArray();
                for (int i = 0; i < arrCh.length; i++) {
                    ((Subdivision) arrCh[i]).setParent(null);
                    this.subdivisionRepository.save(((Subdivision) arrCh[i]));
                }
                
                // удаляем связанные UserSubdivision если они есть
                if (dbSubdivision.getListUserSubdivision() != null || dbSubdivision.getListUserSubdivision().size() != 0) {
                    Object[] arrayUserSubdivision = null;
                    arrayUserSubdivision = dbSubdivision.getListUserSubdivision().toArray();
                    for (int i = 0; i < arrayUserSubdivision.length; i++) {
                        UserSubdivision userSubdivision = (UserSubdivision) arrayUserSubdivision[i];
                        userSubdivision.getUser().getListUserSubdivision().remove(userSubdivision);
                        userSubdivision.setUser(null);
                        userSubdivision.setSubdivision(null);
                        dbSubdivision.getListUserSubdivision().remove(userSubdivision);
                        this.userSubdivisionRepository.delete(userSubdivision);
                    }
                }

                // удаляем все расходы
                Object[] expenditures = this.expenditureRepository.findAllExpenditureByNameSubdivision(dbSubdivision.getName()).toArray();
                if (expenditures != null && expenditures.length != 0) {
                    for (int i = 0; i < expenditures.length; i++) {
                        this.deleteExpenditure(((Expenditure) expenditures[i]));
                    }
                }

                // удаляем само подразделение
                this.subdivisionRepository.delete(dbSubdivision);
                
                responseEntity = new ResponseEntity(HttpStatus.OK);
                return responseEntity; 
            }

            //3) -------------
            //		*        |
            //		|        |
            //	  ( * )      |
            //	  /	  \      |
            //	 *     *     |
            //----------------
            if ((children.size() != 0 || children != null) && dbSubdivision.getParent() != null) {
                // удаляем у родителя все элементы
                Object[] arrCh = dbSubdivision.getParent().getChildren().toArray();
                for (int i = 0; i < arrCh.length; i++) {
                    if (((Subdivision)arrCh[i]) == subdivision) {
                        dbSubdivision.getParent().getChildren().remove(((Subdivision)arrCh[i]));
                        this.subdivisionRepository.save(dbSubdivision.getParent());
                        break;
                    }
                }
                    
                // удаляем все дочерние элементы у нажего подразделения
                arrCh = children.toArray();
                for (int i = 0; i < arrCh.length; i++) {
                    ((Subdivision) arrCh[i]).setParent(null);
                    this.subdivisionRepository.save(((Subdivision) arrCh[i]));
                }	
                
                // удаляем связанные UserSubdivision если они есть
                if (dbSubdivision.getListUserSubdivision() != null || dbSubdivision.getListUserSubdivision().size() != 0) {
                    Object[] arrayUserSubdivision = null;
                    arrayUserSubdivision = dbSubdivision.getListUserSubdivision().toArray();
                    for (int i = 0; i < arrayUserSubdivision.length; i++) {
                        UserSubdivision userSubdivision = (UserSubdivision) arrayUserSubdivision[i];
                        userSubdivision.getUser().getListUserSubdivision().remove(userSubdivision);
                        userSubdivision.setUser(null);
                        userSubdivision.setSubdivision(null);
                        dbSubdivision.getListUserSubdivision().remove(userSubdivision);
                        this.userSubdivisionRepository.delete(userSubdivision);
                    }
                }

                // удаляем все расходы
                Object[] expenditures = this.expenditureRepository.findAllExpenditureByNameSubdivision(dbSubdivision.getName()).toArray();
                if (expenditures != null && expenditures.length != 0) {
                    for (int i = 0; i < expenditures.length; i++) {
                        this.deleteExpenditure(((Expenditure) expenditures[i]));
                    }
                }

                // удаляем само подразделение
                this.subdivisionRepository.delete(dbSubdivision);
                
                responseEntity = new ResponseEntity(HttpStatus.OK);
                return responseEntity; 
            }

            //4) -------------
            //		  O      |
            //		  |	     |
            //		( * )    |
            //		  |      |
            //		  O      |
            //----------------
            if ((children.size() == 0 || children == null) && dbSubdivision.getParent() == null) {
                // удаляем связанные UserSubdivision если они есть
                if (dbSubdivision.getListUserSubdivision() != null || dbSubdivision.getListUserSubdivision().size() != 0) {
                    Object[] arrayUserSubdivision = null;
                    arrayUserSubdivision = dbSubdivision.getListUserSubdivision().toArray();
                    for (int i = 0; i < arrayUserSubdivision.length; i++) {
                        UserSubdivision userSubdivision = (UserSubdivision) arrayUserSubdivision[i];
                        userSubdivision.getUser().getListUserSubdivision().remove(userSubdivision);
                        userSubdivision.setUser(null);
                        userSubdivision.setSubdivision(null);
                        dbSubdivision.getListUserSubdivision().remove(userSubdivision);
                        this.userSubdivisionRepository.delete(userSubdivision);
                    }
                }

                // удаляем все расходы
                Object[] expenditures = this.expenditureRepository.findAllExpenditureByNameSubdivision(dbSubdivision.getName()).toArray();
                if (expenditures != null && expenditures.length != 0) {
                    for (int i = 0; i < expenditures.length; i++) {
                        this.deleteExpenditure(((Expenditure) expenditures[i]));
                    }
                }

                // удаляем само подразделение
                this.subdivisionRepository.delete(dbSubdivision);
                
                responseEntity = new ResponseEntity(HttpStatus.OK);
                return responseEntity; 
            }
        } catch (Exception exception) {
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
        
        responseEntity = new ResponseEntity(HttpStatus.OK);
		return responseEntity; 
    }
	
    @Transactional()
    @RequestMapping(value = "/do/subdivision/update", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity update(@RequestBody Subdivision subdivision) {
        ResponseEntity responseEntity = null;
		Subdivision dbSubdivision, dbParent = null;
		
        try {
            dbSubdivision = this.subdivisionRepository.findById(subdivision.getIdSubdivision()).get();
            List<Expenditure> listExpenditure = null;
            
            // изменилось ли название у подразделения
            if (subdivision.getName().equals(dbSubdivision.getName()) == false) {
                listExpenditure = this.expenditureRepository.findAllExpenditureByNameSubdivision(dbSubdivision.getName());
                if (listExpenditure != null && !listExpenditure.isEmpty()) {
                    for (Expenditure expenditure : listExpenditure) {
                        expenditure.setNameSubdivision(subdivision.getName());
                        this.expenditureRepository.save(expenditure);
                    }
                }
                dbSubdivision.setName(subdivision.getName());
            }
            // parent link disconnected
            if (dbSubdivision.getParent() != null) {
                dbSubdivision.getParent().getChildren().remove(dbSubdivision);
                this.subdivisionRepository.save(dbSubdivision.getParent());
                dbSubdivision.setParent(null);
            }
            // children link disconnected
            if (!dbSubdivision.getChildren().isEmpty()) {
                Object[] array = dbSubdivision.getChildren().toArray();
                for (int i = 0; i < array.length; i++) {
                    ((Subdivision) array[i]).setParent(null);
                    this.subdivisionRepository.save((Subdivision) array[i]);
                }
                dbSubdivision.getChildren().clear();
            }
            // set up new parameters
            if (subdivision.getParent().getName().equals("root")) {
                dbSubdivision.setParent(null);
            } else {
                dbParent = this.subdivisionRepository.findByName(subdivision.getParent().getName());
                dbParent.getChildren().add(dbSubdivision);
                dbSubdivision.setParent(dbParent);
            }
            // save parameters
            this.subdivisionRepository.save(dbSubdivision);
            if (dbParent != null)
                this.subdivisionRepository.save(dbParent);
        } catch (Exception exception) {
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
        
		responseEntity = new ResponseEntity(HttpStatus.OK);
		return responseEntity;
    }

    public void deleteExpenditure(Expenditure expenditure) {
        // какой тип расхода
        if (expenditure.getTypeExpenditure().equals("state")) {
            // сначала удаляем все expenditure_category_counts
            Object[] arrayExpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForStateByIdExpenditure(expenditure.getIdExpenditure()).toArray();
            for (int i = 0; i < arrayExpenditureCategoryCount.length; i++) {
                this.expenditureCategoryCountRepository.delete((ExpenditureCategoryCount) arrayExpenditureCategoryCount[i]);
            }
            // удаляем сам расход
            this.expenditureRepository.delete(expenditure);
        } else if (expenditure.getTypeExpenditure().equals("list")) {
            // сначала удаляем все expenditure_category_counts
            Object[] arrayExpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForListByIdExpenditure(expenditure.getIdExpenditure()).toArray();
            for (int i = 0; i < arrayExpenditureCategoryCount.length; i++) {
                this.expenditureCategoryCountRepository.delete((ExpenditureCategoryCount) arrayExpenditureCategoryCount[i]);
            }
            // удаляем сам расход
            this.expenditureRepository.delete(expenditure);
        } else if (expenditure.getTypeExpenditure().equals("current")) {
            // удаляем expenditure_category_counts и category_absence_counts
            Object[] arrECC = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForCurrentByIdExpenditure(expenditure.getIdExpenditure()).toArray();
            for(int i = 0; i < arrECC.length; i++) {
                ExpenditureCategoryCount ecc = (ExpenditureCategoryCount) arrECC[i];
                Object[] arrCAC = ecc.getListCategoryAbsenceCount().toArray();
                for (int y = 0; y < arrCAC.length; y++) {
                    CategoryAbsenceCount acc = (CategoryAbsenceCount) arrCAC[y];
                    this.categoryAbsenceCountRepository.delete(acc);
                }
                this.expenditureCategoryCountRepository.delete(ecc);
            }
            // удаляем сам расход
            this.expenditureRepository.delete(expenditure);
        }
    }
}