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
//|--------------------------------------|
//| методы для работы с 'state' расходом |
//|--------------------------------------|
@RestController
public class ExpenditureStateController {
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

    // структура Map<Category, ExcepnitureCategoryCount>
    // где arrayExpenditureCategoryCount --> значение Category расхода по штату
    @RequestMapping(value = "/do/expenditure/state/form", method = RequestMethod.POST)
    public ModelAndView getFormExpenditureState(@RequestParam(value = "subdivisionName", required = true) String subdivisionName, @RequestParam(value = "type", required = true) String type) {
        ModelAndView modelAndView = null;

        if (subdivisionName.equals("")) {
            // значит нам не передали имя подразделения и мы не можем его дать форму
            modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=Нет такого подразделения или вы не выбрали подразделение в дереве подразделений");
            return modelAndView;
        }

        Expenditure dbExpenditureState = null;
        dbExpenditureState = this.expenditureRepository.findTypeStateExpenditure(subdivisionName);
        
        List<Title> listCategoryType = this.categoryTypeRepository.findAll();
        
        // создаем и заполняем правильную Map в зависимости от type
        switch (type) {
            case ("add") : {
                if (dbExpenditureState != null) {
                    // значит такой расход уже есть и мы должны перенаправить на страницу ошибки
                    // нужно отправить modelAndView на другой controller и он возвратит httpStatus ошибки
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=У этого подразделения уже есть расход по штату");
                    return modelAndView;
                }

                modelAndView = new ModelAndView();
                modelAndView.setViewName("expenditure_state");
                modelAndView.addObject("subdivisionName", subdivisionName);
                modelAndView.addObject("type", type);

                for (Title categoryType : listCategoryType) {
                    Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
                    for (Category category : categoryType.getListCategory()) {
                        mapCategoryAndCountByExpenditure.put(category, null);
                    }
                    categoryType.setMapCategoryAndCountByExpenditure(mapCategoryAndCountByExpenditure);
                }
                modelAndView.addObject("listCategoryType", listCategoryType);

                break;
            }
            case ("edit") : {
                if (dbExpenditureState == null) {
                    // значит такого расхода нет и мы должны перенаправить на страницу ошибки
                    // нечего редактировать
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=У вас нет расхода тип штат. Сначала создайте его");
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


                modelAndView = new ModelAndView();
                modelAndView.setViewName("expenditure_state");
                modelAndView.addObject("subdivisionName", subdivisionName);
                modelAndView.addObject("type", type);

                // правильно заполняем Map<Category, int[]>
                for (Title categoryType : listCategoryType) {
                    Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
                    for (Category category : categoryType.getListCategory()) {
                        int[] arrayCountByExpenditure = new int[1];
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureState.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                arrayCountByExpenditure[0] = expenditureCategoryCount.getCountCategory();
                                mapCategoryAndCountByExpenditure.put(category, arrayCountByExpenditure);
                                break;
                            }
                        }
                    }
                    categoryType.setMapCategoryAndCountByExpenditure(mapCategoryAndCountByExpenditure);
                }

                modelAndView.addObject("listCategoryType", listCategoryType);

                break;
            }
            case ("delete") : {
                if (dbExpenditureState == null) {
                    // значит такого расхода нет и мы должны перенаправить на страницу ошибки
                    // нечего удалять
                    modelAndView = new ModelAndView("forward:/error/code/bad_request?descriptionError=У вас нет расхода тип штат. Сначала создайте его");
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
                
                modelAndView = new ModelAndView();
                modelAndView.setViewName("expenditure_state");
                modelAndView.addObject("subdivisionName", subdivisionName);
                modelAndView.addObject("type", type);

                // правильно заполняем Map<Category, int[]>
                for (Title categoryType : listCategoryType) {
                    Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
                    for (Category category : categoryType.getListCategory()) {
                        int[] arrayCountByExpenditure = new int[1];
                        for (ExpenditureCategoryCount expenditureCategoryCount : dbExpenditureState.getListExpenditureCategoryCount()) {
                            if (category == expenditureCategoryCount.getCategory()) {
                                arrayCountByExpenditure[0] = expenditureCategoryCount.getCountCategory();
                                mapCategoryAndCountByExpenditure.put(category, arrayCountByExpenditure);
                                break;
                            }
                        }
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
    @RequestMapping(value = "/do/expenditure/state/create", method = RequestMethod.POST, consumes = "application/json"/*consumes = MediaType.APPLICATION_JSON_VALUE  headers="content-type = application/x-www-form-urlencoded"*/)
    public ResponseEntity createExpenditureState(@RequestBody(required = true) Expenditure expenditurePrototype,
                                                     HttpServletRequest req) 
    {
        ResponseEntity responseEntity = null;
        try {
            // мы принимаем от сервера "прототип" расхода по штату
            // необходимо на его основе построить "настоящий" расход по штату
            // и записать его в базу данных
            Expenditure expenditure = new Expenditure();
            expenditure.setDateAndTime(null);
            expenditure.setNameSubdivision(this.subdivisionRepository.findByName(expenditurePrototype.getNameSubdivision()).getName());
            expenditure.setTypeExpenditure("state");
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
    @RequestMapping(value = "/do/expenditure/state/delete", method = RequestMethod.POST)
    public ResponseEntity deleteExpenditureState(@RequestParam(value = "subdivisionName", required = true) String subdivisionName) {
        ResponseEntity responseEntity = null;

        Expenditure dbExpenditureState = null;
        try {
            dbExpenditureState = this.expenditureRepository.findTypeStateExpenditure(subdivisionName);
            Object[] arrayExpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForStateByIdExpenditure(dbExpenditureState.getIdExpenditure()).toArray();
            for (int i = 0; i < arrayExpenditureCategoryCount.length; i++) {
                this.expenditureCategoryCountRepository.delete((ExpenditureCategoryCount) arrayExpenditureCategoryCount[i]);
            }
            this.expenditureRepository.delete(dbExpenditureState);
        } catch (Exception exception) {
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
        }
        responseEntity = new ResponseEntity(HttpStatus.OK);
        return responseEntity;
    }

    @Transactional()
    @RequestMapping(value = "/do/expenditure/state/update", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity updateExpenditureState(@RequestBody(required = true) Expenditure expenditurePrototype) {
        ResponseEntity responseEntity = null;
        try {
            // мы принимаем от сервера "прототип" расхода по штату
            // необходимо на его основе обновить "настоящий" расход по штату
            // и записать его в базу данных
            // достаем обьект Expenditure типа state из БД
            Expenditure expenditureStateDB = this.expenditureRepository.findTypeStateExpenditure(expenditurePrototype.getNameSubdivision());
            if (expenditureStateDB != null) {
                Object[] arrayExpenditureCategoryCount = this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForStateByIdExpenditure(expenditureStateDB.getIdExpenditure()).toArray();
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
