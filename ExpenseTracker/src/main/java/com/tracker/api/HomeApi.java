package com.tracker.api;

import java.text.ParseException;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tracker.domain.users.User;
import com.tracker.services.UsersService;

@Controller
public class HomeApi {
	
	@Autowired
	private UsersService userService;
	
	@ResponseBody
	@RequestMapping( value="/home", method=RequestMethod.GET )
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	public User home( @AuthenticationPrincipal User user ) {
		if( user.getStatus().equals("disabled")){
			return null;
		}
			
		User result =  new User.Builder()
				.email( user.getEmail())
				.username( user.getUsername() )
				.id( user.getId())
				.isAdmin(user.isAdmin())
				.roles( user.getRoles())
				.phone( user.getPhone() )
				.build();
		
		return result;
	}

	@RequestMapping( value="/initDatabase", method=RequestMethod.GET )
	public String deleteAllAndInitDatabase( HttpSession session ) throws ParseException {
		userService.initDatabase();
		return "index.html";
	}
	
	@RequestMapping( value="/logoutSuccess", method=RequestMethod.GET )
	public String logout( HttpSession session ) {
		session.invalidate();
		return "redirect:#/";
	}
	
	@RequestMapping( value="/", method=RequestMethod.GET )
	public String welcome(@RequestParam(required=false, defaultValue="false") Boolean error, Model model) {
		model.addAttribute("error", error);
		return "index.html";
	}
	
}
