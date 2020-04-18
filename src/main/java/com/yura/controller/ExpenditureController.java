/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yura.controller;

import com.yura.entity.Expenditure;
import com.yura.repository.ExpenditureRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author shyv
 */
@RestController
public class ExpenditureController {
    @Autowired private ExpenditureRepository expenditureRepository;
    @Autowired private SubdivisionController subdivisionController;
    
    @RequestMapping(value = "/do/expenditure/form", method = RequestMethod.POST)
	public ModelAndView form() {
		ModelAndView modelAndView = new ModelAndView();
		
		Iterable<Expenditure> listExpenditure = this.expenditureRepository.findAll();
		modelAndView.addObject("listExpenditure", listExpenditure);
		modelAndView.setViewName("expenditure");
		
		return modelAndView;
	}
    
    @Transactional()
    @RequestMapping(value = "/do/expenditure/delete", method = RequestMethod.POST)
	public ResponseEntity delete(@RequestParam(value = "idExpenditure", required = true) int idExpenditure) {
		ResponseEntity responseEntity = null;
		Expenditure expenditure = null;
        Optional<Expenditure> test = null;
        
		try {
            expenditure = this.expenditureRepository.findById(idExpenditure).get();
			this.subdivisionController.deleteExpenditure(expenditure);
            test = this.expenditureRepository.findById(idExpenditure);
		} catch (Exception exception) {
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
		}
        if (test.isEmpty())
            responseEntity = new ResponseEntity(HttpStatus.OK);
        else
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
        
		return responseEntity;
	}
}
