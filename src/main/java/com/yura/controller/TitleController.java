package com.yura.controller;

import com.yura.entity.Title;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.yura.repository.ExpenditureCategoryCountRepository;
import com.yura.entity.Category;
import com.yura.entity.ExpenditureCategoryCount;
import com.yura.repository.TitleRepository;
import org.springframework.stereotype.Controller;

//@RestController
@Controller
public class TitleController {
	
	@Autowired
	private TitleRepository categoryTypeRepository;
    @Autowired
    private ExpenditureCategoryCountRepository expenditureCategoryCountRepository;
	
	@RequestMapping(value = "/do/title/form", method = RequestMethod.POST)
	public ModelAndView form() {  
        ModelAndView modelAndView = new ModelAndView();
		
		Iterable<Title> listTitle = this.categoryTypeRepository.findAll();
		modelAndView.addObject("listTitle", listTitle);
		modelAndView.setViewName("title");


		return modelAndView;
	}
	
	@RequestMapping(value = "/do/title/add", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity add(@RequestBody(required = true) Title categoryType) {
		ResponseEntity responseEntity = null;
        ModelAndView modelAndView = new ModelAndView();
		try {
			this.categoryTypeRepository.save(categoryType);
		} catch (Exception eexception) {
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
		}
        Iterable<Title> listTitle = this.categoryTypeRepository.findAll();
		modelAndView.addObject("listTitle", listTitle);
		modelAndView.setViewName("title");
        responseEntity = ResponseEntity.ok().body(modelAndView);
		//responseEntity = new ResponseEntity(HttpStatus.OK);
		return responseEntity;
	}
	
	@RequestMapping(value = "/do/title/update", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity update(@RequestBody(required = true) Title categoryType) {
		ResponseEntity responseEntity = null;
		try {
			this.categoryTypeRepository.save(categoryType);
		} catch (Exception exception) {
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
		}
		responseEntity = new ResponseEntity(HttpStatus.OK);

		return responseEntity;
	}
	
	@RequestMapping(value = "/do/title/delete", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity delete(@RequestBody(required = true) Title title) {
		ResponseEntity responseEntity = null;
		
		try {
            //сначала необходимо удалить ExpenditureCategoryCount связанные с Category
            Title dbCategoryType = this.categoryTypeRepository.findByName(title.getName()).get();
            Object[] arrayCategory = null;
            arrayCategory = dbCategoryType.getListCategory().toArray();
            for (int i1 = 0; i1 < arrayCategory.length; i1++) {
                Object[] arrayExpenditureCategoryCount = null;
                arrayExpenditureCategoryCount = ((Category) arrayCategory[i1]).getListExpenditureCategoryCount().toArray();
                for (int i = 0; i < arrayExpenditureCategoryCount.length; i++) {
                    this.expenditureCategoryCountRepository.delete((ExpenditureCategoryCount) arrayExpenditureCategoryCount[i]);
                }                
            }
            
			this.categoryTypeRepository.deleteById(title.getId());
		} catch (Exception exception) {
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity; 
		}
		responseEntity = new ResponseEntity(HttpStatus.OK);
		
		return responseEntity;
	}
}
