package com.yura.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.ArrayList;

import com.yura.repository.CategoryRepository;
import com.yura.repository.AbsenceRepository;
import com.yura.repository.CategoryAbsenceRepository;
import com.yura.entity.Category;
import com.yura.entity.Absence;
import com.yura.entity.CategoryAbsence;
import com.yura.repository.TitleRepository;

@RestController
public class AbsenceController {
    
    @Autowired private TitleRepository titleRepository;
	@Autowired private CategoryRepository categoryRepository;
	@Autowired private AbsenceRepository absenceRepository;
	@Autowired private CategoryAbsenceRepository categoryAbsenceRepository;

	@RequestMapping(value = "/do/absence/form", method = RequestMethod.POST)
	public ModelAndView form() {
		ModelAndView modelAndView = new ModelAndView();
		
		Iterable<Category> listCategory = this.categoryRepository.findAll();		
		modelAndView.addObject("listCategory", listCategory);
		List<Absence> listAbsence = absenceRepository.findAll();		
		modelAndView.addObject("listAbsence", listAbsence);

		modelAndView.setViewName("absence");
		
		return modelAndView;
	}

	@RequestMapping(value = "/do/absence/add", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity add(@RequestBody Absence absence) {
		ResponseEntity responseEntity = null;

		try {
			List<CategoryAbsence> listCategoryAbsence = new ArrayList<CategoryAbsence>();
			
			for (CategoryAbsence categoryAbsence : absence.getListCategoryAbsence()) {
				//1) мы достаем по имени Category
				Category dbCategory = this.categoryRepository.findByName(categoryAbsence.getCategory().getName()).get();
				//2) теперь создаем настоящий CategoryAbsence для бд
				CategoryAbsence dbCategoryAbsence = new CategoryAbsence(dbCategory, absence);
				listCategoryAbsence.add(dbCategoryAbsence);
			}
			absence.setListCategoryAbsence(listCategoryAbsence);
			this.absenceRepository.save(absence);
		} catch (Exception exception) {
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
		}
		responseEntity = new ResponseEntity(HttpStatus.OK);

		return responseEntity;
	}

	@RequestMapping(value = "/do/absence/delete", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity delete(@RequestBody Absence absence) {
		ResponseEntity responseEntity = null;
		
		try {
			Absence dbAbsence = this.absenceRepository.findByName(absence.getName()).get();
			this.absenceRepository.delete(dbAbsence);
		} catch (Exception exception) {
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
		}
		responseEntity = new ResponseEntity(HttpStatus.OK);
		
		return responseEntity;
	}
}