/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yura.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yura.entity.*;
import com.yura.entity.Title;
import com.yura.repository.AbsenceRepository;
import com.yura.repository.CategoryAbsenceCountRepository;
import com.yura.repository.CategoryAbsenceRepository;
import com.yura.repository.CategoryRepository;
import com.yura.repository.ExpenditureCategoryCountRepository;
import com.yura.repository.ExpenditureRepository;
import com.yura.repository.SubdivisionRepository;
import com.yura.repository.UserRepository;
import com.yura.repository.UserSubdivisionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import com.yura.repository.TitleRepository;

/**
 *
 * @author shyv
 */
//|-------------------------------------|
//| методы для работы с 'list' расходом |
//|-------------------------------------|
@RestController
public class ExpenditureListController {
    @Autowired private TitleRepository categoryTypeRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SubdivisionRepository subdivisionRepository;
    @Autowired private ExpenditureRepository expenditureRepository;
    @Autowired private ExpenditureCategoryCountRepository expenditureCategoryCountRepository;
    @Autowired private UserSubdivisionRepository userSubdivisionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AbsenceRepository absenceRepository;
    @Autowired private CategoryAbsenceRepository categoryAbsenceRepository;
    @Autowired private CategoryAbsenceCountRepository categoryAbsenceCountRepository;
    
    // структура Map<Category, arrayExcepnitureCategoryCount>
    // где arrayExpenditureCategoryCount имеет структуру
    // arrayExpenditureCategoryCount[0] --> значение Category расхода по штату
    // arrayExpenditureCategoryCount[1] --> значение Category расхода по списку
    @RequestMapping(value = "/do/expenditure/list/form", method = RequestMethod.POST)
    public ModelAndView getFormExpenditureList(@RequestParam(value = "subdivisionName", required = true) String subdivisionName, @RequestParam(value = "type", required = true) String type) {
        ModelAndView modelAndView = null;
        
        if (subdivisionName.equals("")) {
            // значит нам не передали имя подразделения и мы не можем его дать форму
            modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет такого подразделения или вы не выбрали подразделение в дереве подразделений");
            return modelAndView;
        }
        
        Expenditure dbExpenditureState = null;
        dbExpenditureState = this.expenditureRepository.findTypeStateExpenditure(subdivisionName);
        
        /*if (dbExpenditureState == null) {
            // значит расход по "штату" нет для этого подразделения и мы не можем дать форму
            modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет расхода по штату для этого подразделения, сначала создайте расход по штату");
            return modelAndView;
        }*/
        
        // мы должны найти все expenditure_category_count для этого расхода и положить в него
        try {
            List<ExpenditureCategoryCount> listDbEXpenditureCategoryCount = null;
            listDbEXpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForStateByIdExpenditure(dbExpenditureState.getIdExpenditure());
            dbExpenditureState.setListExpenditureCategoryCount(listDbEXpenditureCategoryCount);
        } catch (Exception e) {
            modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет информации для этого расхода. Невозможно отобразить форму");
            return modelAndView;
        }
        
        Expenditure dbExpenditureList = null;
        dbExpenditureList = this.expenditureRepository.findTypeListExpenditure(subdivisionName);
        
        List<Title> listCategoryType = this.categoryTypeRepository.findAll();
        
        // создаем и заполняем правильную Map в зависимости от type
        switch (type) {
            case ("add") : {
                // значит расход по "штату" нет для этого подразделения и мы не можем дать форму для создания
                if (dbExpenditureState == null) {
                    // значит расход по "штату" нет для этого подразделения и мы не можем дать форму
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет расхода по штату для этого подразделения, сначала создайте расход по штату");
                    return modelAndView;
                }
                
                if (dbExpenditureList != null) {
                    // значит такой расход уже есть и мы должны перенаправить на страницу ошибки
                    // нужно отправить modelAndView на другой controller и он возвратит httpStatus ошибки
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=У этого подразделения уже есть расход по списку");
                    return modelAndView;
                }
                
                modelAndView = new ModelAndView();
                modelAndView.setViewName("expenditure_list");
                modelAndView.addObject("subdivisionName", subdivisionName);
                modelAndView.addObject("type", type);
                
                // правильно заполняем Map<Category, int[]>
                for (Title categoryType : listCategoryType) {
                    Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
                    for (Category category : categoryType.getListCategory()) {
                        int[] arrayCountByExpenditure = new int[2];
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureState.getListExpenditureCategoryCount()) {
                            if (category.getIdCategory() == expenditureCategoryCount.getCategory().getIdCategory()) {
                                arrayCountByExpenditure[0] = expenditureCategoryCount.getCountCategory();
                                arrayCountByExpenditure[1] = 0;
                                break;
                            }
                        }
                        mapCategoryAndCountByExpenditure.put(category, arrayCountByExpenditure);
                    }
                    categoryType.setMapCategoryAndCountByExpenditure(mapCategoryAndCountByExpenditure);
                }
                modelAndView.addObject("listCategoryType", listCategoryType);
                
                break;
            }
            case ("edit") : {
                // значит расход по "штату" нет для этого подразделения и мы не можем дать форму для создания
                if (dbExpenditureState == null) {
                    // значит расход по "штату" нет для этого подразделения и мы не можем дать форму
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет расхода по штату для этого подразделения, сначала создайте расход по штату");
                    return modelAndView;
                }
                
                if (dbExpenditureList == null) {
                    // значит такого расхода нет и мы должны перенаправить на страницу ошибки
                    // нечего редактировать
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=У вас нет расхода по списку. Сначала создайте его");
                    return modelAndView;
                }
                
                // мы должны найти все expenditure_category_count для этого расхода и положить в него
                try {
                    List<ExpenditureCategoryCount> listDbEXpenditureCategoryCount = null;
                    listDbEXpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForListByIdExpenditure(dbExpenditureList.getIdExpenditure());
                    dbExpenditureList.setListExpenditureCategoryCount(listDbEXpenditureCategoryCount);
                } catch (Exception e) {
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет информации для этого расхода. Невозможно отобразить форму");
                    return modelAndView;
                }
                
                modelAndView = new ModelAndView();
                modelAndView.setViewName("expenditure_list");
                modelAndView.addObject("subdivisionName", subdivisionName);
                modelAndView.addObject("type", type);
                
                // правильно заполняем Map<Category, int[]>
                for (Title categoryType : listCategoryType) {
                    Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
                    for (Category category : categoryType.getListCategory()) {
                        int[] arrayCountByExpenditure = new int[2];
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureState.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                arrayCountByExpenditure[0] = expenditureCategoryCount.getCountCategory();
                                break;
                            }
                        }
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureList.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                arrayCountByExpenditure[1] = expenditureCategoryCount.getCountCategory();
                                break;
                            }
                        }
                        mapCategoryAndCountByExpenditure.put(category, arrayCountByExpenditure);
                    }
                    categoryType.setMapCategoryAndCountByExpenditure(mapCategoryAndCountByExpenditure);
                }
                modelAndView.addObject("listCategoryType", listCategoryType);
                
                break;
            }
            case ("delete") : {
                /*if (dbExpenditureState == null) {
                    // значит такого расхода нет и мы должны перенаправить на страницу ошибки
                    // нечего удалять
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=У вас нет расхода по списку. Сначала создайте его");
                    return modelAndView;
                }*/
                
                // мы должны найти все expenditure_category_count для этого расхода и положить в него
                try {
                    List<ExpenditureCategoryCount> listDbEXpenditureCategoryCount = null;
                    listDbEXpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForListByIdExpenditure(dbExpenditureList.getIdExpenditure());
                    dbExpenditureList.setListExpenditureCategoryCount(listDbEXpenditureCategoryCount);
                } catch (Exception e) {
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет информации для этого расхода. Невозможно отобразить форму");
                    return modelAndView;
                }
                
                modelAndView = new ModelAndView();
                modelAndView.setViewName("expenditure_list");
                modelAndView.addObject("subdivisionName", subdivisionName);
                modelAndView.addObject("type", type);
                
                // правильно заполняем Map<Category, int[]>
                for (Title categoryType : listCategoryType) {
                    Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
                    for (Category category : categoryType.getListCategory()) {
                        int[] arrayCountByExpenditure = new int[2];
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureState.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                arrayCountByExpenditure[0] = expenditureCategoryCount.getCountCategory();
                                break;
                            }
                        }
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureList.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                arrayCountByExpenditure[1] = expenditureCategoryCount.getCountCategory();
                                break;
                            }
                        }
                        mapCategoryAndCountByExpenditure.put(category, arrayCountByExpenditure);
                    }
                    categoryType.setMapCategoryAndCountByExpenditure(mapCategoryAndCountByExpenditure);
                }
                modelAndView.addObject("listCategoryType", listCategoryType);
                
                break;
            }
        }
        
        return modelAndView;
    }

    @Transactional()
    @RequestMapping(value = "/do/expenditure/list/create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity createExpenditureList(@RequestBody(required = true) Expenditure expenditurePrototype) {
        ResponseEntity responseEntity = null;
        try {
            // мы принимаем от сервера "прототип" расхода по списку
            // необходимо на его основе построить "настоящий" расход по списку
            // и записать его в базу данных
            Expenditure expenditure = new Expenditure();
            expenditure.setDateAndTime(null);
            expenditure.setNameSubdivision(this.subdivisionRepository.findByName(expenditurePrototype.getNameSubdivision()).getName());
            expenditure.setTypeExpenditure("list");
            this.expenditureRepository.save(expenditure);
            
            int idExpenditure = this.expenditureRepository.getMaxId();
            
            // забираем list expenditure_category_count и сохраняем его в бд
            Object[] arrayExpenditureCategoryCountPrototype = expenditurePrototype.getListExpenditureCategoryCount().toArray();
            for (int i = 0; i < arrayExpenditureCategoryCountPrototype.length; i++) {
                ExpenditureCategoryCount expenditureCategoryCount = new ExpenditureCategoryCount();
                expenditureCategoryCount.setListCategoryAbsenceCount(null);
                expenditureCategoryCount.setCategory(this.categoryRepository.findByName(((ExpenditureCategoryCount) arrayExpenditureCategoryCountPrototype[i]).getCategory().getName()).get());
                expenditureCategoryCount.setCountCategory(((ExpenditureCategoryCount) arrayExpenditureCategoryCountPrototype[i]).getCountCategory());
                expenditureCategoryCount.setIdExpenditure(idExpenditure);
                this.expenditureCategoryCountRepository.save(expenditureCategoryCount);
            }
        } catch (Exception exception) {
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
        }
        return responseEntity;
    }

    @Transactional()
    @RequestMapping(value = "/do/expenditure/list/delete", method = RequestMethod.POST)
    public ResponseEntity deleteExpenditureList(@RequestParam(value = "subdivisionName", required = true) String subdivisionName) {
        ResponseEntity responseEntity = null;

        Expenditure dbExpenditureList = null;
        try {
            dbExpenditureList = this.expenditureRepository.findTypeListExpenditure(subdivisionName);
            Object[] arrayExpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForListByIdExpenditure(dbExpenditureList.getIdExpenditure()).toArray();
            for (int i = 0; i < arrayExpenditureCategoryCount.length; i++) {
                this.expenditureCategoryCountRepository.delete((ExpenditureCategoryCount) arrayExpenditureCategoryCount[i]);
            }
            this.expenditureRepository.delete(dbExpenditureList);
        } catch (Exception exception) {
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
        }
        responseEntity = new ResponseEntity(HttpStatus.OK);
        return responseEntity;
    }

    @Transactional()
    @RequestMapping(value = "/do/expenditure/list/update", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity updateExpenditureList(@RequestBody(required = true) Expenditure expenditurePrototype) {
        ResponseEntity responseEntity = null;
        try {
            // мы принимаем от сервера "прототип" расхода по списку
            // необходимо на его основе обновить "настоящий" расход по списку
            // и записать его в базу данных
            // достаем обьект Expenditure типа list из БД
            Expenditure expenditureListDB = this.expenditureRepository.findTypeListExpenditure(expenditurePrototype.getNameSubdivision());
            if (expenditureListDB != null) {
                Object[] arrayExpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForListByIdExpenditure(expenditureListDB.getIdExpenditure()).toArray();
                for (int x = 0; x < arrayExpenditureCategoryCount.length; x++) {
                    String nameCategoryFromDB = ((ExpenditureCategoryCount) arrayExpenditureCategoryCount[x]).getCategory().getName();
                    for (ExpenditureCategoryCount expenditureCategoryCount : expenditurePrototype.getListExpenditureCategoryCount()) {
                        if (nameCategoryFromDB.equals(expenditureCategoryCount.getCategory().getName())) {
                            ((ExpenditureCategoryCount) arrayExpenditureCategoryCount[x]).setCountCategory(expenditureCategoryCount.getCountCategory());
                            break;
                        }
                    }
                    this.expenditureCategoryCountRepository.save((ExpenditureCategoryCount) arrayExpenditureCategoryCount[x]);
                }
            } else {
                throw new IOException();
            }
        } catch (Exception exception) {
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
        }
        responseEntity = new ResponseEntity(HttpStatus.OK);
        
        return responseEntity;
    }
}
