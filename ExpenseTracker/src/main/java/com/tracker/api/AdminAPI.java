package com.tracker.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tracker.domain.users.User;
import com.tracker.services.UsersService;
import com.tracker.domain.users.Role;

@Controller
public class AdminAPI {
	@Autowired
	private UsersService userService;
		
	// Administrator browse all user
	@RequestMapping( value="/admin/{uid}/users", method=RequestMethod.GET )
	@Secured({"ROLE_ADMIN"})
	@ResponseBody
	public List<User> getAllUser( @AuthenticationPrincipal User user ) {
		System.out.println("Browse all user...");
		
		List<User> result = new ArrayList<User>();
		result = userService.getUsers();
		return result;
	}
	
	
	// Administrator create user
	@RequestMapping( value="/admin/{uid}/users", method=RequestMethod.POST )
	@Secured({"ROLE_ADMIN"})
	@ResponseBody
	public User createUser( @AuthenticationPrincipal User user , Model model,
						@RequestBody MultiValueMap<String, String> userData) {
		System.out.println("Creating user...");
		
		List<Role> roles;
		User newUser = new User();
		// Define new user's role
		if( userData.containsKey("admininput") && userData.get("admininput").get(0).equals("on") ) {
			roles = Arrays.asList( new Role[] { new Role("ROLE_ADMIN"),new Role("ROLE_USER") } );
			newUser.setAdmin(true);
		}
		else{
			roles = Arrays.asList( new Role[] { new Role("ROLE_USER") } );
			newUser.setAdmin(false);
		}
		
		newUser.setEmail(userData.get("email").get(0));
		newUser.setName(userData.get("name").get(0));
		newUser.setStatus(userData.get("state").get(0));
		newUser.setUsername(userData.get("username").get(0));
		newUser.setPhone(userData.get("phone"));
		userService.save(newUser);
		
		return newUser;
	}
}
