/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yura.controller;

import java.util.List;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

import com.yura.entity.User;
import com.yura.entity.Subdivision;
import com.yura.entity.UserSubdivision;
import com.yura.repository.UserRepository;
import com.yura.repository.SubdivisionRepository;
import com.yura.repository.UserSubdivisionRepository;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
/**
 *
 * @author shyv
 */
@RestController
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private SubdivisionRepository subdivisionRepository;
	@Autowired
	private UserSubdivisionRepository userSubdivisionRepository;

	@RequestMapping(value = "/do/user/form", method = RequestMethod.POST)
	public ModelAndView form(ModelAndView modelAndView) {
		
		List<User> listUser = this.userRepository.findAll();		
		modelAndView.addObject("listUser", listUser);

		List<Subdivision> listSubdivision = this.subdivisionRepository.findAll();		
		modelAndView.addObject("listSubdivision", listSubdivision);
		
		modelAndView.setViewName("user");
		
		return modelAndView;
	}
	
	@Transactional()
	@RequestMapping(value = "/do/user/add", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity add(@RequestBody User user) {
		
		ResponseEntity responseEntity = null;
        
        // проверка есть ли пользователь с таким именем
        /*if (this.userRepository.findByName(user.getName()) != null) {
            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;
        }*/
        
		try {
			if (user.getListUserSubdivision().size() == 1 && user.getListUserSubdivision().get(0).getSubdivision().getName().equals("все")) {
				this.userRepository.save(user);
				boolean isEdit = user.getListUserSubdivision().get(0).getIsEdit();
				List<Subdivision> listDbSubdivision = this.subdivisionRepository.findAll();
				for (Subdivision subdivision : listDbSubdivision) {
					UserSubdivision userSubdivision = new UserSubdivision();
					userSubdivision.setUser(user);
					userSubdivision.setSubdivision(subdivision);
					userSubdivision.setIsEdit(isEdit);
					user.getListUserSubdivision().add(userSubdivision);
					subdivision.getListUserSubdivision().add(userSubdivision);
					this.userSubdivisionRepository.save(userSubdivision);
				}
			} else {
				this.userRepository.save(user);
				Object[] arrayUserSubdivision = null;
				arrayUserSubdivision = user.getListUserSubdivision().toArray();
				for (int i = 0; i < arrayUserSubdivision.length; i++) {
					Subdivision dbSubdivision = this.subdivisionRepository.findByName(((UserSubdivision) arrayUserSubdivision[i]).getSubdivision().getName());
					((UserSubdivision) arrayUserSubdivision[i]).setSubdivision(dbSubdivision);
					dbSubdivision.getListUserSubdivision().add((UserSubdivision) arrayUserSubdivision[i]);
					((UserSubdivision) arrayUserSubdivision[i]).setUser(user);
					user.getListUserSubdivision().add((UserSubdivision) arrayUserSubdivision[i]);
					this.userSubdivisionRepository.save((UserSubdivision) arrayUserSubdivision[i]);
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
	@RequestMapping(value = "/do/user/delete", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity delete(@RequestBody User user) {
		
		ResponseEntity responseEntity = null;
		try {
			User dbUser = this.userRepository.findByName(user.getName()).get();
			Object[] arrayUserSubdivision = null;
			arrayUserSubdivision = dbUser.getListUserSubdivision().toArray();
			for (int i = 0; i < arrayUserSubdivision.length; i++) {
				dbUser.getListUserSubdivision().remove((UserSubdivision) arrayUserSubdivision[i]);
				Subdivision dbSubdivision = this.subdivisionRepository.findById(((UserSubdivision) arrayUserSubdivision[i]).getSubdivision().getIdSubdivision()).get();
				dbSubdivision.getListUserSubdivision().remove((UserSubdivision) arrayUserSubdivision[i]);
				this.userSubdivisionRepository.delete((UserSubdivision) arrayUserSubdivision[i]);
			}
			this.userRepository.delete(dbUser);
		} catch(Exception exception) {
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;			
		}
		
		responseEntity = new ResponseEntity(HttpStatus.OK);
		return responseEntity;
	}
	
	public void deleteOldUserSubdivision(User user) {
		Object[] arrayUserSubdivision = null;
		arrayUserSubdivision = user.getListUserSubdivision().toArray();
		for (int i = 0; i < arrayUserSubdivision.length; i++) {
			user.getListUserSubdivision().remove((UserSubdivision) arrayUserSubdivision[i]);
			Subdivision dbSubdivision = this.subdivisionRepository.findById(((UserSubdivision) arrayUserSubdivision[i]).getSubdivision().getIdSubdivision()).get();
			dbSubdivision.getListUserSubdivision().remove((UserSubdivision) arrayUserSubdivision[i]);
			this.userSubdivisionRepository.delete((UserSubdivision) arrayUserSubdivision[i]);
		}	
	}
	
	@Transactional()
	public void updateUser(User dbUser, User user) {
		
		if (dbUser.getName().equals(user.getName()) == false) {
			dbUser.setName(user.getName());
		}
		if (dbUser.getPassword().equals(user.getPassword()) == false) {
			dbUser.setPassword(user.getPassword());
		}
		if (dbUser.getRoleSystem().equals(user.getRoleSystem()) == false) {
			dbUser.setRoleSystem(user.getRoleSystem());
		}
		
		this.userRepository.save(dbUser);
		
		if (user.getListUserSubdivision().size() == 1 && user.getListUserSubdivision().get(0).getSubdivision().getName().equals("все")) {
			boolean isEdit = user.getListUserSubdivision().get(0).getIsEdit();
			List<Subdivision> listDbSubdivision = this.subdivisionRepository.findAll();
			for (Subdivision subdivision : listDbSubdivision) {
				UserSubdivision userSubdivision = new UserSubdivision();
				userSubdivision.setUser(dbUser);
				userSubdivision.setSubdivision(subdivision);
				userSubdivision.setIsEdit(isEdit);
				dbUser.getListUserSubdivision().add(userSubdivision);
				subdivision.getListUserSubdivision().add(userSubdivision);
				this.userSubdivisionRepository.save(userSubdivision);
			}
		} else {
			Object[] arrayUserSubdivision = null;
			arrayUserSubdivision = user.getListUserSubdivision().toArray();
			for (int i = 0; i < arrayUserSubdivision.length; i++) {
				Subdivision dbSubdivision = this.subdivisionRepository.findByName(((UserSubdivision) arrayUserSubdivision[i]).getSubdivision().getName());
				((UserSubdivision) arrayUserSubdivision[i]).setSubdivision(dbSubdivision);
				dbSubdivision.getListUserSubdivision().add((UserSubdivision) arrayUserSubdivision[i]);
				((UserSubdivision) arrayUserSubdivision[i]).setUser(dbUser);
				dbUser.getListUserSubdivision().add((UserSubdivision) arrayUserSubdivision[i]);
				this.userSubdivisionRepository.save((UserSubdivision) arrayUserSubdivision[i]);
			}
		}
	}
	
	@RequestMapping(value = "/do/user/update", method = RequestMethod.POST, consumes = "application/json")	
	public ResponseEntity update(@RequestBody User user, 
                                     HttpServletRequest request) {
		ResponseEntity responseEntity = null;
		
		try {
			User dbUser = this.userRepository.findById(user.getIdUser()).get();
			// сначала удаляем все старые записи UserSubdivision
			this.deleteOldUserSubdivision(dbUser);
			// затем пробуем обновить этого пользователя
			this.updateUser(dbUser, user);
            // обновляем сессию
            HttpSession httpSession = request.getSession();
            User userFromSession = (User) httpSession.getAttribute("user");
            if (dbUser.getName().equals(userFromSession.getName())) {
                userFromSession.setListUserSubdivision(this.userRepository.findById(dbUser.getIdUser()).get().getListUserSubdivision());
            }
		} catch (Exception exception) {
			responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);
			return responseEntity;	
		} 
		
		responseEntity = new ResponseEntity(HttpStatus.OK);
		return responseEntity;
	}
	
}
