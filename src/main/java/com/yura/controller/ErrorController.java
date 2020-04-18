/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
/**
 *
 * @author shyv
 */
@RestController
public class ErrorController {
    // если мы вводим неверный адрес то должно быть перенаправление на страницу ошибки.
    // почему то она перенапраляет на error.html
    // 1 - необходимо написать логику обработки таких ситуаций
    // 2 - хорошо стилизовать эту страницу

    @RequestMapping(value = "/error", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView getWorstAddress() {
        ModelAndView modelAndView = null;
        modelAndView.setViewName("404");
        return modelAndView;
    }
    
    // отдает на сервер http-status - ы
    @RequestMapping(value = "/error/code/bad_request", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity getCodeErrorBadRequest(@RequestParam(value = "descriptionError", required = false) String descriptionError) {
        return ResponseEntity.badRequest().body(descriptionError); 
    }
}
