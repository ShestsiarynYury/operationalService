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

import java.util.List;

import com.yura.repository.CategoryRepository;
import com.yura.entity.Category;
import com.yura.entity.ExpenditureCategoryCount;
import com.yura.repository.ExpenditureCategoryCountRepository;
import com.yura.repository.TitleRepository;

@RestController
public class CategoryController {

	@Autowired private TitleRepository titleRepository;
	@Autowired private CategoryRepository categoryRepository;
    @Autowired private ExpenditureCategoryCountRepository expenditureCategoryCountRepository;

	@RequestMapping(value = "/do/category/form", method = RequestMethod.POST)
	public ModelAndView form() {
		ModelAndView modelAndView = new ModelAndView();
		
		List<Category> listCategory = this.categoryRepository.findAll();
		modelAndView.addObject("listCategory", listCategory);
		List<Title> listTitle = titleRepository.findAll();
		modelAndView.addObject("listTitle", listTitle);
		modelAndView.setViewName("category");
		
		return modelAndView;
	}

	@RequestMapping(value = "/do/category/add", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity add(@RequestBody(required = true) Category category) {
		ResponseEntity responseEntity = null;
		
		try {
			Title title = this.titleRepository.findByName(category.getTitle().getName()).get();
			category.setTitle(title);
			this.categoryRepository.save(category);
		} catch (Exception exception) {
            exception.getStackTrace();
            //System.out.println(exception.getMessage());
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
		}
		responseEntity = new ResponseEntity(HttpStatus.OK);
		
		return responseEntity;
	}
	
	@RequestMapping(value = "/do/category/update", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity update(@RequestBody(required = true) Category category) {
		ResponseEntity responseEntity = null;
		
		try {
			Category dbCategory = this.categoryRepository.findById(category.getIdCategory()).get();
			dbCategory.setName(category.getName());
			this.categoryRepository.save(dbCategory);
		} catch (Exception exception) {
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
		}
		responseEntity = new ResponseEntity(HttpStatus.OK);
		
		return responseEntity;
	}
	
	@RequestMapping(value = "/do/category/delete", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity delete(@RequestBody(required = true) Category category) {
		ResponseEntity responseEntity = null;
		
		try {
			Category dbCategory = this.categoryRepository.findById(category.getIdCategory()).get();
            
            // сначала необходимо удалить все ExpenditureCategoryCount
            Object[] arrayExpenditureCategoryCount = null;
            arrayExpenditureCategoryCount = dbCategory.getListExpenditureCategoryCount().toArray();
            for (int i = 0; i < arrayExpenditureCategoryCount.length; i++) {
                this.expenditureCategoryCountRepository.delete((ExpenditureCategoryCount) arrayExpenditureCategoryCount[i]);
            }
            
            Title title = dbCategory.getTitle();
            //title = this.titleRepository.findById(title.getId()).get();
            Object[] arrayCategory = title.getListCategory().toArray();
            for (int i = 0; i < arrayCategory.length; i++) {
                if (((Category) arrayCategory[i]).getName().equals(dbCategory.getName()))
                    title.getListCategory().remove(((Category) arrayCategory[i]));
            }
            
            //title.getListCategory().remove(category);
            this.titleRepository.save(title);
            
			this.categoryRepository.delete(dbCategory);
		} catch (Exception exception) {
            exception.printStackTrace();
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity; 
		}
		
		responseEntity = new ResponseEntity(HttpStatus.OK);
		
		return responseEntity;
	}
}