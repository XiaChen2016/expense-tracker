package com.tracker.api;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tracker.domain.users.User;

@Controller
public class HomeApi {
	@RequestMapping( value="/home", method=RequestMethod.GET )
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	public String home(@AuthenticationPrincipal User user) {
		System.out.println("Redirecting to HOME");
		if( user.hasRole("ROLE_ADMIN") ) {
			return "redirect:/admin";
		} else {
			return "redirect:/users";
		}
	}
	
	@RequestMapping( value="/", method=RequestMethod.GET )
	public String welcome(@RequestParam(required=false, defaultValue="false") Boolean error, Model model) {
		model.addAttribute("error", error);
		System.out.println("Returning the login page");
		return "login.html";
	}
	
//	@ResponseBody
//	@RequestMapping( value="/login", method=RequestMethod.POST )
//	public void login( @RequestParam(required=true, defaultValue="username") String username,
//			@RequestParam(required=true, defaultValue="password") String password) {
//		System.out.println("Login function, username: " + username+ ", password: " + password);
//	}
}
