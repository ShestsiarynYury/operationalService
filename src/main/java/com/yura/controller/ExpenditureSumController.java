/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yura.controller;

import com.yura.entity.Category;
import com.yura.entity.CategoryAbsence;
import com.yura.entity.CategoryAbsenceCount;
import com.yura.entity.Expenditure;
import com.yura.entity.ExpenditureCategoryCount;
import com.yura.entity.Title;
import com.yura.repository.AbsenceRepository;
import com.yura.repository.CategoryAbsenceCountRepository;
import com.yura.repository.CategoryAbsenceRepository;
import com.yura.repository.CategoryRepository;
import com.yura.repository.ExpenditureCategoryCountRepository;
import com.yura.repository.ExpenditureRepository;
import com.yura.repository.SubdivisionRepository;
import com.yura.repository.TitleRepository;
import com.yura.repository.UserRepository;
import com.yura.repository.UserSubdivisionRepository;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author shyv
 */
//|----------------------------------------|
//|          суммарный расход              |
//|----------------------------------------|
@RestController
public class ExpenditureSumController {
    
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
    
    @RequestMapping(value = "do/expenditure/sum/form", method = RequestMethod.POST)
    public ModelAndView SumExpenditureCurrent() {
        ModelAndView modelAndView = null;
        
        List<Category> listCategory = this.categoryRepository.findAll(); // достаем все category
        
        /* создаем суммарный расход только для 'state' */
        /* ------------------------------------------- */
        List<Expenditure> listState = this.expenditureRepository.findAllExpenditureTypeState();
        // I state
        // создаем пустой Expenditure sum тип 'state'
        // по количеству category строим -> expenditure_category_counts
        // у каждой category есть список CategoryAbsence и по каждой строим category_absence_counts
        Expenditure sumState = new Expenditure();
        sumState.setDateAndTime(null);
        sumState.setTypeExpenditure("state"); // установить тип 'state'
        
        List<ExpenditureCategoryCount> listExpenditureCategoryCountState = new ArrayList<>();
        for (Category category : listCategory) {
            ExpenditureCategoryCount expenditureCategoryCount = new ExpenditureCategoryCount();
            expenditureCategoryCount.setCategory(category);
            expenditureCategoryCount.setCountCategory(0);
            expenditureCategoryCount.setListCategoryAbsenceCount(null);
            listExpenditureCategoryCountState.add(expenditureCategoryCount);
        }
        sumState.setListExpenditureCategoryCount(listExpenditureCategoryCountState);
        
        // II state
        // пробегаемся по списку 'state' расходов и ложим данные в sumState
        for (Expenditure state : listState) {
            for (ExpenditureCategoryCount eccState : this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForStateByIdExpenditure(state.getIdExpenditure())) {
                for (ExpenditureCategoryCount eccSum : sumState.getListExpenditureCategoryCount()) {
                    if (eccState.getCategory() == eccSum.getCategory()) {
                        eccSum.setCountCategory(eccSum.getCountCategory()/*текущее значение*/ + eccState.getCountCategory());
                        break;
                    }
                }
            }
        }
        
        /* создаем суммарный расход только для 'list' */
        /* ------------------------------------------ */
        List<Expenditure> listList = this.expenditureRepository.findAllExpenditureTypeList();
        // I list
        // создаем пустой Expenditure sum тип 'list'
        // по количеству category строим -> expenditure_category_counts
        // у каждой category есть список CategoryAbsence и по каждой строим category_absence_counts
        Expenditure sumList = new Expenditure();
        sumList.setDateAndTime(null);
        sumList.setTypeExpenditure("list"); // установить тип 'list'

        List<ExpenditureCategoryCount> listExpenditureCategoryCountList = new ArrayList<>();
        for (Category category : listCategory) {
            ExpenditureCategoryCount expenditureCategoryCount = new ExpenditureCategoryCount();
            expenditureCategoryCount.setCategory(category);
            expenditureCategoryCount.setCountCategory(0);
            expenditureCategoryCount.setListCategoryAbsenceCount(null);
            listExpenditureCategoryCountList.add(expenditureCategoryCount);
        }
        sumList.setListExpenditureCategoryCount(listExpenditureCategoryCountList);
        
        // II list
        // пробегаемся по списку 'list' расходов и ложим данные в sumList
        for (Expenditure list : listList) {
            for (ExpenditureCategoryCount eccList : this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForListByIdExpenditure(list.getIdExpenditure())) {
                for (ExpenditureCategoryCount eccSum : sumList.getListExpenditureCategoryCount()) {
                    if (eccList.getCategory() == eccSum.getCategory()) {
                        eccSum.setCountCategory(eccSum.getCountCategory()/*текущее значение*/ + eccList.getCountCategory());
                        break;
                    }
                }
            }
        }
        
        
        /* создаем только суммарный расход для 'current' */
        /* --------------------------------------------- */
        List<Expenditure> listCurrent = this.expenditureRepository.findAllExpenditureTypeCurrentByCurrentDate();
        // I 'current'
        // создаем пустой Expenditure sumCurrent
        // по количеству category строим -> expenditure_category_counts
        // у каждой category есть список CategoryAbsence и по каждой строим category_absence_counts
        Expenditure sumCurrent = new Expenditure();
        Date date = new Date();
        sumCurrent.setDateAndTime(new Timestamp(date.getTime())); // установить текущую дату и время
        sumCurrent.setTypeExpenditure("current"); // установить тип 'current'

        List<ExpenditureCategoryCount> listExpenditureCategoryCount = new ArrayList<>();
        for (Category category : listCategory) {
            ExpenditureCategoryCount expenditureCategoryCount = new ExpenditureCategoryCount();
            expenditureCategoryCount.setCategory(category);
            expenditureCategoryCount.setCountCategory(0);
            List<CategoryAbsenceCount> listCategoryAbsenceCount = new ArrayList<>();
            for (CategoryAbsence categoryAbsence : category.getListCategoryAbsence()) {
                CategoryAbsenceCount categoryAbsenceCount = new CategoryAbsenceCount();
                categoryAbsenceCount.setCategoryAbsence(categoryAbsence);
                categoryAbsenceCount.setCountAbsence(0);
                listCategoryAbsenceCount.add(categoryAbsenceCount);
            }
            expenditureCategoryCount.setListCategoryAbsenceCount(listCategoryAbsenceCount);
            listExpenditureCategoryCount.add(expenditureCategoryCount);
        }
        sumCurrent.setListExpenditureCategoryCount(listExpenditureCategoryCount);
        
        // II 'current'
        // пробегаемся по списку 'current' расходов и ложим данные в sum
        for (Expenditure current : listCurrent) {
            for (ExpenditureCategoryCount eccCurrent : this.expenditureCategoryCountRepository.findListExpenditureCategoryCountForCurrentByIdExpenditure(current.getIdExpenditure())) {
                for (ExpenditureCategoryCount eccSum : sumCurrent.getListExpenditureCategoryCount()) {
                    if (eccCurrent.getCategory() == eccSum.getCategory()) {
                        eccSum.setCountCategory(eccSum.getCountCategory()/*текущее значение*/ + eccCurrent.getCountCategory());
                        // а здесь нам еще надо пробежаться по CategoryAbsenceCount
                        for (CategoryAbsenceCount cacCurrent : eccCurrent.getListCategoryAbsenceCount()) {
                            for (CategoryAbsenceCount cacSum : eccSum.getListCategoryAbsenceCount()) {
                                if (cacCurrent.getCategoryAbsence() == cacSum.getCategoryAbsence()) {
                                    cacSum.setCountAbsence(cacSum.getCountAbsence()/*текущее значение*/ + cacCurrent.getCountAbsence());
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        // у нас есть три суммарных типа расхода
        // sumList
        // sumState
        // sumCurrent
        // дальше что бы сделать смотри метод show current expenditure
        
        List<Title> listCategoryType = this.categoryTypeRepository.findAll();
        modelAndView = new ModelAndView();
        modelAndView.setViewName("expenditure_sum");
        modelAndView.addObject("dateNow", LocalDate.now().toString());

        // правильно заполняем Map<Category, int[]>
        for (Title categoryType : listCategoryType) {
            Map<Category, int[]> mapCategoryAndCountByExpenditure = new HashMap<Category, int[]>();
            for (Category category : categoryType.getListCategory()) {
                int[] arrayCountByExpenditure = new int[3];
                for (ExpenditureCategoryCount expenditureCategoryCount : sumState.getListExpenditureCategoryCount()) {
                    if (category == expenditureCategoryCount.getCategory()) {
                        arrayCountByExpenditure[0] = expenditureCategoryCount.getCountCategory();
                        break;
                    }
                }
                for (ExpenditureCategoryCount expenditureCategoryCount : sumList.getListExpenditureCategoryCount()) {
                    if (category == expenditureCategoryCount.getCategory()) {
                        arrayCountByExpenditure[1] = expenditureCategoryCount.getCountCategory();
                        break;
                    }
                }
                ExpenditureCategoryCount expenditureCategoryCountCach = null;
                for (ExpenditureCategoryCount expenditureCategoryCount : sumCurrent.getListExpenditureCategoryCount()) {
                    if (category == expenditureCategoryCount.getCategory()) {
                        //1 мы извлекакем значение для int[2]
                        arrayCountByExpenditure[2] = expenditureCategoryCount.getCountCategory();
                        expenditureCategoryCountCach = expenditureCategoryCount;
                        break;
                    }
                }
                //2 мы прибавляем массив из причин отсутствия
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

        
        return modelAndView;
    }
}
