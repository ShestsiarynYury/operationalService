package com.yura.controller;

import com.yura.ListUserNameAndIdSession;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yura.entity.User;
import com.yura.entity.Title;
import com.yura.repository.SubdivisionRepository;
import com.yura.repository.TitleRepository;
import com.yura.repository.UserRepository;
import java.util.Map.Entry;

@RestController
class HomeController {
	
    @Autowired private UserRepository userRepository;
	@Autowired private SubdivisionRepository subdivisionRepository;
	@Autowired private TitleRepository categoryTypeRepository;
    @Autowired private ListUserNameAndIdSession mapUserNameAndIdSession;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ModelAndView authenticate(HttpServletRequest request, 
                                         HttpServletResponse response,
                                             @RequestParam(value="username", required = true) String username, 
                                                 @RequestParam(value="password", required = true) String password) {	
        ModelAndView modelAndView = null;
        User user = null;

        if (request.getSession() == null) {
            modelAndView = new ModelAndView();
            modelAndView.setViewName("login");
            return modelAndView;
        }
        
        if (request.getSession().getId() != null) {
            
        } else {
            modelAndView = new ModelAndView();
            modelAndView.setViewName("login");
            return modelAndView;
        }
        
        System.out.println(request.getSession().getId());
        
        try {
            user = this.userRepository.findByName(username).get();
            
            boolean isBusy = false;
            for (Entry<String, String> entry : this.mapUserNameAndIdSession.getMap().entrySet()) {
                // если есть пользователь с уже имеющейся сессией
                if (entry.getValue().equals(request.getSession().getId())) {
                    if (entry.getKey() != null) {
                        isBusy = true;
                        break;
                    }
                }
                // если пользователь есть с таким именем
                if (entry.getKey().equals(user.getName())) {
                    isBusy = true;
                    break;
                }
            }
            
            if (isBusy) {
                modelAndView = new ModelAndView();
                modelAndView.setViewName("login");
                return modelAndView;
            }
            
            if (this.mapUserNameAndIdSession.getMap().containsKey(user.getName()) && this.mapUserNameAndIdSession.getMap().containsValue(request.getSession().getId())) {
                modelAndView = new ModelAndView();
                modelAndView.setViewName("login");
                return modelAndView;
            } else {
                this.mapUserNameAndIdSession.getMap().put(user.getName(), request.getSession().getId());
            }
        } catch (Exception e) {
            modelAndView = new ModelAndView();
            modelAndView.setViewName("login");
            return modelAndView;
        }

        if (user != null && user.getPassword().equals(password)) {
            HttpSession httpSession = request.getSession(true);
            httpSession.setAttribute("user", user);
            httpSession.setAttribute("success", true);
            modelAndView = new ModelAndView("redirect:main");
        } else {
            modelAndView = new ModelAndView();
            modelAndView.setViewName("login");
        }
        return modelAndView;
    }
    
	@RequestMapping(value = "/main", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView main(HttpServletRequest request, 
                                 HttpServletResponse response) {
		ModelAndView modelAndView = null;
		HttpSession httpSession = request.getSession();
		User user = (User) httpSession.getAttribute("user");
		if (user == null) {
			modelAndView = new ModelAndView("redirect:/");
		} else {
			modelAndView = new ModelAndView("forward:gui");
			modelAndView.addObject("user", user);
		}
		return modelAndView;
	}
	
	@RequestMapping(value = "/gui", method = RequestMethod.GET)
	public ModelAndView getGUI(HttpServletRequest request, 
                                   HttpServletResponse response) {
		ModelAndView modelAndView = null;
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			modelAndView = new ModelAndView("redirect:/");
		} else {
			if (user.getRoleSystem().equals("admin")) {			
				//for admin
			}
			if (user.getRoleSystem().equals("user")) {
				//for user
			}
			modelAndView = new ModelAndView();
			Iterable<Title> listGroup = this.categoryTypeRepository.findAll();
			modelAndView.addObject("listGroupp", listGroup);
			modelAndView.setViewName("gui");
		}
		return modelAndView;
	}
	
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView login() {
    	ModelAndView modelAndView = new ModelAndView();
    	modelAndView.setViewName("login");
        return modelAndView;
    }
	
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public ResponseEntity logout(HttpServletRequest request, 
                                     HttpServletResponse response) {	
        ResponseEntity responseEntity = null;
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
        if (user != null)
            this.mapUserNameAndIdSession.getMap().remove(user.getName(), request.getSession().getId());
		session.invalidate();
        
        responseEntity = new ResponseEntity(HttpStatus.OK);
        return responseEntity;
	}
}