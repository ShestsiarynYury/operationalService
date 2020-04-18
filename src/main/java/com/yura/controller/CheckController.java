/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yura.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.ArrayList;
import com.yura.entity.CheckExpenditureByPost;
import com.yura.entity.Expenditure;
import com.yura.repository.CheckRepository;
import com.yura.repository.ExpenditureRepository;
import java.time.LocalDate;

/**
 * 
 *
 * @author shyv
 */
@RestController
public class CheckController {
    @Autowired private CheckRepository checkRepository;
    @Autowired private ExpenditureRepository expenditureRepository;
    
    @RequestMapping(value = "/do/check/expenditure/by/post", method = RequestMethod.POST)
    public ModelAndView checkExpenditureByPost() {
        ModelAndView modelAndView = null;
        modelAndView = new ModelAndView();
        List<CheckExpenditureByPost> listCheckExpenditureByPost = this.checkRepository.checkExpenditureByPost();
        modelAndView.addObject("list", listCheckExpenditureByPost);
        modelAndView.setViewName("check_expenditure_post");
        modelAndView.addObject("dateNow", LocalDate.now().toString());
        return modelAndView;
    }
    
    @RequestMapping(value = "/do/check/expenditure/by/info", method = RequestMethod.POST)
    public ModelAndView infoAboutExpenditure(@RequestParam(value = "subdivisionName", required = true) String subdivisionName) {
        ModelAndView modelAndView = null;
        modelAndView = new ModelAndView();
        Expenditure state, list, current = null;
        state = this.expenditureRepository.findTypeStateExpenditure(subdivisionName);
        modelAndView.addObject("state", state);
        list = this.expenditureRepository.findTypeListExpenditure(subdivisionName);
        modelAndView.addObject("list", list);
        current = this.expenditureRepository.findTypeCurrentExpenditure(subdivisionName, LocalDate.now().toString());
        modelAndView.addObject("current", current);
        modelAndView.setViewName("info_expenditure");
        return modelAndView;
    }
}
