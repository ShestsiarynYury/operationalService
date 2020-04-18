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
//|----------------------------------------|
//| методы для работы с 'current' расходом |
//|----------------------------------------|
@RestController
public class ExpenditureCurrentController {
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
    
    // 1 -> проверка указано ли имя подразделения
    // 2 -> проверка создан ли "штатный" расход
    // 3 -> проверка создан ли "списочный" расход
    // 4 -> проверка может ли пользователь (подавать расход, редактировать расход)
    // 5 -> проверка может ли пользователь (просмотреть расход)
    // 6 -> проверка создан ли уже расход "налицо" за текущую дату
    @RequestMapping(value = "/do/expenditure/current/form", method = RequestMethod.POST)
    public ModelAndView getFormExpenditureCurrent(@RequestParam(value = "subdivisionName", required = true) String subdivisionName, 
                                                    @RequestParam(value = "type", required = true) String type,
                                                        HttpServletRequest httpServletRequest,
                                                            @RequestParam(value = "date", required = false) String strDate) {
        
        ModelAndView modelAndView = null;
        
        // 1 проверка
        if (subdivisionName.equals("") || subdivisionName == null) {
            // значит нам не передали имя подразделения и мы не можем его дать форму
            modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет такого подразделения или вы не выбрали подразделение в дереве подразделений");
            return modelAndView;
        }
        
        // 2 проверка
        Expenditure dbExpenditureState = null;
        dbExpenditureState = this.expenditureRepository.findTypeStateExpenditure(subdivisionName);
        if (dbExpenditureState == null) {
            // значит расход по "штату" нет для этого подразделения и мы не можем дать форму
            modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет расхода по штату для этого подразделения, сначала создайте расход по штату");
            return modelAndView;
        }
        
        // мы должны найти все expenditure_category_count для этого расхода и положить в него
        try {
            List<ExpenditureCategoryCount> listDbEXpenditureCategoryCount = null;
            listDbEXpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForStateByIdExpenditure(dbExpenditureState.getIdExpenditure());
            dbExpenditureState.setListExpenditureCategoryCount(listDbEXpenditureCategoryCount);
        } catch (Exception e) {
            modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет информации для этого расхода. Невозможно отобразить форму");
            return modelAndView;
        }
        
        // 3 проверка
        Expenditure dbExpenditureList = null;
        dbExpenditureList = this.expenditureRepository.findTypeListExpenditure(subdivisionName);
        if (dbExpenditureList == null) {
            // значит расход по "списку" нет для этого подразделения и мы не можем дать форму
            modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет расхода по списку для этого подразделения, сначала создайте расход по списку");
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
        
        HttpSession httpSession = httpServletRequest.getSession();
        User user = (User) httpSession.getAttribute("user");
        User dbUser = this.userRepository.findByName(user.getName()).get();
        List<Title> listCategoryType = this.categoryTypeRepository.findAll();
        
        switch (type) {
            case ("post") : {
                // 4 проверка
                if (dbUser.isMySubdivisionIsEdit(subdivisionName) == false) {
                    // значит у данного пользователя нет право на подачу расхода для этого подразделения
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Извините, но у вас нет право подавать расход за данное подразделение");
                    return modelAndView;
                }
                
                // 6 проверка
                if (this.expenditureRepository.findTypeCurrentExpenditure(subdivisionName, LocalDate.now().toString()) != null) {
                    // значит за данное подразделение уже создан расход "налицо"
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Извините, но за это подразделение уже создан расход на текущую дату");
                    return modelAndView;
                }

                modelAndView = new ModelAndView();
                modelAndView.setViewName("expenditure_current");
                modelAndView.addObject("subdivisionName", subdivisionName);
                modelAndView.addObject("type", type);
                modelAndView.addObject("dateNow", LocalDate.now().toString());
                
                // правильно заполняем Map<Category, arrayExpenditureCategoryCount>
                for (Title categoryType : listCategoryType) {
                    Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
                    for (Category category : categoryType.getListCategory()) {
                        int[] arrayCountCategoryByExpenditure = new int[3];
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureState.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                arrayCountCategoryByExpenditure[0] = expenditureCategoryCount.getCountCategory();
                                break;
                            }
                        }
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureList.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                arrayCountCategoryByExpenditure[1] = expenditureCategoryCount.getCountCategory();
                                break;
                            }
                        }
                        arrayCountCategoryByExpenditure[2] = 0;
                        Map<String, Integer> mapAbsenceAndCountAbsence = new HashMap<String,Integer>();
                        for (Absence absence : category.getListAbsence()) {
                            String nameAbsence = absence.getName();
                            int countAbsence = 0;
                            mapAbsenceAndCountAbsence.put(nameAbsence, countAbsence);
                        }
                        category.setMapAbsenceAndCountAbsence(mapAbsenceAndCountAbsence);
                        mapCategoryAndCountByExpenditure.put(category, arrayCountCategoryByExpenditure);
                    }
                    categoryType.setMapCategoryAndCountByExpenditure(mapCategoryAndCountByExpenditure);
                }
                modelAndView.addObject("listCategoryType", listCategoryType);
                
                break;
            }
            case ("edit") : {
                // 4 проверка
                if (dbUser.isMySubdivisionIsEdit(subdivisionName) == false) {
                    // значит у данного пользователя нет право на редактирование расхода для этого подразделения
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Извините, но у вас нет право редактировать расход за данное подразделение");
                    return modelAndView;
                }
                
                // 6 проверка
                Expenditure dbExpenditureCurrent = this.expenditureRepository.findTypeCurrentExpenditure(subdivisionName, LocalDate.now().toString());
                if (dbExpenditureCurrent == null) {
                    // значит за данное подразделение уже создан расход "налицо"
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Извините, но за это подразделение еще не создан расход на текущую дату");
                    return modelAndView;
                }
                
                // мы должны найти все expenditure_category_count для этого расхода и положить в него
                try {
                    List<ExpenditureCategoryCount> listDbEXpenditureCategoryCount = null;
                    listDbEXpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForCurrentByIdExpenditure(dbExpenditureCurrent.getIdExpenditure());
                    dbExpenditureCurrent.setListExpenditureCategoryCount(listDbEXpenditureCategoryCount);
                } catch (Exception e) {
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет информации для этого расхода. Невозможно отобразить форму");
                    return modelAndView;
                }

                modelAndView = new ModelAndView();
                modelAndView.setViewName("expenditure_current");
                modelAndView.addObject("subdivisionName", subdivisionName);
                modelAndView.addObject("type", type);
                modelAndView.addObject("dateNow", LocalDate.now().toString());

                // правильно заполняем Map<Category, int[]>
                for (Title categoryType : listCategoryType) {
                    Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
                    for (Category category : categoryType.getListCategory()) {
                        int[] arrayCountByExpenditure = new int[3];
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
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureCurrent.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                arrayCountByExpenditure[2] = expenditureCategoryCount.getCountCategory();
                                //
                                Map<String, Integer> mapAbsenceAndCountAbsence = new HashMap<String,Integer>();
                                for (CategoryAbsenceCount categoryAbsenceCount : expenditureCategoryCount.getListCategoryAbsenceCount()) {
                                    String nameAbsence = categoryAbsenceCount.getCategoryAbsence().getAbsence().getName();
                                    int countAbsence = categoryAbsenceCount.getCountAbsence();
                                    mapAbsenceAndCountAbsence.put(nameAbsence, countAbsence);
                                }
                                category.setMapAbsenceAndCountAbsence(mapAbsenceAndCountAbsence);
                                //
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
            case ("show") : {
                // 5 проверка
                if (dbUser.isMySubdivision(subdivisionName) == false) {
                    // значит у данного пользователя нет право на просмотр расхода для этого подразделения
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Извините, но у вас нет право на просмотр расхода за данное подразделение");
                    return modelAndView;
                }
                
                // 6 проверка
                 Expenditure dbExpenditureCurrent = this.expenditureRepository.findTypeCurrentExpenditure(subdivisionName, LocalDate.now().toString());
                if (dbExpenditureCurrent == null) {
                    // значит за данное подразделение уже создан расход "налицо"
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Извините, но за это подразделение еще не создан расход на текущую дату");
                    return modelAndView;
                }
                
                // мы должны найти все expenditure_category_count для этого расхода и положить в него
                try {
                    List<ExpenditureCategoryCount> listDbEXpenditureCategoryCount = null;
                    listDbEXpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForCurrentByIdExpenditure(dbExpenditureCurrent.getIdExpenditure());
                    dbExpenditureCurrent.setListExpenditureCategoryCount(listDbEXpenditureCategoryCount);
                } catch (Exception e) {
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет информации для этого расхода. Невозможно отобразить форму");
                    return modelAndView;
                }

                modelAndView = new ModelAndView();
                modelAndView.setViewName("expenditure_current");
                modelAndView.addObject("subdivisionName", subdivisionName);
                modelAndView.addObject("type", type);
                modelAndView.addObject("dateNow", LocalDate.now().toString());

                // правильно заполняем Map<Category, int[]>
                for (Title categoryType : listCategoryType) {
                    Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
                    for (Category category : categoryType.getListCategory()) {
                        int[] arrayCountByExpenditure = new int[3];
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
                        ExpenditureCategoryCount expenditureCategoryCountCach = null;
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureCurrent.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                //1 мы извлекакем значение для int[2]
                                arrayCountByExpenditure[2] = expenditureCategoryCount.getCountCategory();
                                expenditureCategoryCountCach = expenditureCategoryCount;
                                break;
                            }
                        }
                        //2 мы прибовляем массив из причин отсутствия
                        Map<String, Integer> mapAbsenceAndCountAbsence = new HashMap<String,Integer>();
                        for (CategoryAbsenceCount categoryAbsenceCount : expenditureCategoryCountCach.getListCategoryAbsenceCount()) {
                            String nameAbsence = categoryAbsenceCount.getCategoryAbsence().getAbsence().getName();
                            int countAbsence = categoryAbsenceCount.getCountAbsence();
                            mapAbsenceAndCountAbsence.put(nameAbsence, countAbsence);
                        }
                        category.setMapAbsenceAndCountAbsence(mapAbsenceAndCountAbsence);
                        
                        // для categoryType Map<Category, int[]>
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

    // 1 проверка создан расход за этот день
    @Transactional()
    @RequestMapping(value = "/do/expenditure/current/create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity createExpenditureCurrent(@RequestBody(required = true) String expenditurePrototype) throws JsonParseException, IOException {
        ResponseEntity responseEntity = null;

        ObjectMapper objectMapper = new ObjectMapper();

        String subdivisionNameJson = objectMapper.readTree(expenditurePrototype).get("nameSubdivision").asText();
        Subdivision subdivision = this.subdivisionRepository.findByName(subdivisionNameJson);

        try {
            Expenditure expenditureCurrent = new Expenditure();
            expenditureCurrent.setNameSubdivision(subdivision.getName());
            expenditureCurrent.setTypeExpenditure("current");
            Date date = new Date();
            expenditureCurrent.setDateAndTime(new Timestamp(date.getTime()));
            this.expenditureRepository.save(expenditureCurrent);
            
            int idExpenditure = this.expenditureRepository.getMaxId();
            
            JsonNode arrNodeCategory = objectMapper.readTree(expenditurePrototype).get("listExpenditureCategoryCount");
            if (arrNodeCategory.isArray()) {
                for (JsonNode objNodeCategory : arrNodeCategory) {
                    String nameCategoryJson = objNodeCategory.get("category").get("name").asText();
                    Category category = this.categoryRepository.findByName(nameCategoryJson).get();
                    int countCategoryJson =  Integer.parseInt(objNodeCategory.get("countCategory").asText());
                    ExpenditureCategoryCount expenditureCategoryCount = new ExpenditureCategoryCount();

                    expenditureCategoryCount.setCategory(category);
                    expenditureCategoryCount.setCountCategory(countCategoryJson);
                    expenditureCategoryCount.setIdExpenditure(idExpenditure);
                    List<CategoryAbsenceCount> listCategoryAbsenceCount = new ArrayList<CategoryAbsenceCount>();

                    JsonNode arrNodeAbsence = objNodeCategory.get("mapAbsenceCount");
                    if (arrNodeAbsence.isArray()) {
                        for (JsonNode objNodeAbsence : arrNodeAbsence) {
                            String nameAbsenceJson = objNodeAbsence.get("absence").get("name").asText();
                            Absence absence = this.absenceRepository.findByName(nameAbsenceJson).get();
                            int countAbsenceJson = Integer.parseInt(objNodeAbsence.get("countAbsence").asText());
                            CategoryAbsence categoryAbsence = this.categoryAbsenceRepository.findByCategoryAndAbsence(category, absence).get();
                            CategoryAbsenceCount categoryAbsenceCount = new CategoryAbsenceCount();
                            categoryAbsenceCount.setCategoryAbsence(categoryAbsence);
                            categoryAbsenceCount.setCountAbsence(countAbsenceJson);
                            categoryAbsenceCount.setExpenditureCategory(expenditureCategoryCount);
                            listCategoryAbsenceCount.add(categoryAbsenceCount);
                        }
                    }
                    expenditureCategoryCount.setListCategoryAbsenceCount(listCategoryAbsenceCount);
                    this.expenditureCategoryCountRepository.save(expenditureCategoryCount);
                }
            }
        } catch (Exception exception) {
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
        }
        responseEntity = new ResponseEntity(HttpStatus.OK);


        return responseEntity;
    }

    @Transactional()
    @RequestMapping(value = "/do/expenditure/current/update", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity updateExpenditureCurrent(@RequestBody(required = true) String expenditurePrototype) throws JsonParseException, IOException {
        ResponseEntity responseEntity = null;
        
        ObjectMapper objectMapper = new ObjectMapper();
        
        String subdivisionNameJson = objectMapper.readTree(expenditurePrototype).get("nameSubdivision").asText();
        Subdivision subdivision = this.subdivisionRepository.findByName(subdivisionNameJson);
        
        // 1 сначала удаляем старый расход
        // и все expenditure_category_count старого расхода
        Expenditure dbExpenditureCurrent = this.expenditureRepository.findTypeCurrentExpenditure(subdivisionNameJson, LocalDate.now().toString());
        //
            Object[] arrayCategory = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForCurrentByIdExpenditure(dbExpenditureCurrent.getIdExpenditure()).toArray();
            for(int i = 0; i < arrayCategory.length; i++) {
                ExpenditureCategoryCount categoryCount = (ExpenditureCategoryCount) arrayCategory[i];
                Object[] arrayAbsence = categoryCount.getListCategoryAbsenceCount().toArray();
                for (int y = 0; y < arrayAbsence.length; y++) {
                    CategoryAbsenceCount absenceCount = (CategoryAbsenceCount) arrayAbsence[y];
                    this.categoryAbsenceCountRepository.delete(absenceCount);
                }
                this.expenditureCategoryCountRepository.delete(categoryCount);
            }
        //
        this.expenditureRepository.delete(dbExpenditureCurrent);
        
        // 2 создаем новый расход и сохраним его в бд 
        try {
            Expenditure expenditureCurrent_NEW = new Expenditure();
            expenditureCurrent_NEW.setNameSubdivision(subdivision.getName());
            expenditureCurrent_NEW.setTypeExpenditure("current");
            Date date = new Date();
            expenditureCurrent_NEW.setDateAndTime(new Timestamp(date.getTime()));
            this.expenditureRepository.save(expenditureCurrent_NEW);
            
            int idExpenditure = this.expenditureRepository.getMaxId();
            
            // 3 создаем expenditure_category_count и сохраняем его в бд
            JsonNode arrNodeCategory = objectMapper.readTree(expenditurePrototype).get("listExpenditureCategoryCount");
            if (arrNodeCategory.isArray()) {
                for (JsonNode objNodeCategory : arrNodeCategory) {
                    String nameCategoryJson = objNodeCategory.get("category").get("name").asText();
                    Category category = this.categoryRepository.findByName(nameCategoryJson).get();
                    int countCategoryJson =  Integer.parseInt(objNodeCategory.get("countCategory").asText());
                    ExpenditureCategoryCount expenditureCategoryCount = new ExpenditureCategoryCount();

                    expenditureCategoryCount.setCategory(category);
                    expenditureCategoryCount.setCountCategory(countCategoryJson);
                    expenditureCategoryCount.setIdExpenditure(idExpenditure);
                    List<CategoryAbsenceCount> listCategoryAbsenceCount = new ArrayList<CategoryAbsenceCount>();

                    JsonNode arrNodeAbsence = objNodeCategory.get("mapAbsenceCount");
                    if (arrNodeAbsence.isArray()) {
                        for (JsonNode objNodeAbsence : arrNodeAbsence) {
                            String nameAbsenceJson = objNodeAbsence.get("absence").get("name").asText();
                            Absence absence = this.absenceRepository.findByName(nameAbsenceJson).get();
                            int countAbsenceJson = Integer.parseInt(objNodeAbsence.get("countAbsence").asText());
                            CategoryAbsence categoryAbsence = this.categoryAbsenceRepository.findByCategoryAndAbsence(category, absence).get();
                            CategoryAbsenceCount categoryAbsenceCount = new CategoryAbsenceCount();
                            categoryAbsenceCount.setCategoryAbsence(categoryAbsence);
                            categoryAbsenceCount.setCountAbsence(countAbsenceJson);
                            categoryAbsenceCount.setExpenditureCategory(expenditureCategoryCount);
                            listCategoryAbsenceCount.add(categoryAbsenceCount);
                        }
                    }
                    expenditureCategoryCount.setListCategoryAbsenceCount(listCategoryAbsenceCount);
                    this.expenditureCategoryCountRepository.save(expenditureCategoryCount);
                }
            }
        } catch (Exception exception) {
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
        }
        responseEntity = new ResponseEntity(HttpStatus.OK);


        return responseEntity;
    }
}
