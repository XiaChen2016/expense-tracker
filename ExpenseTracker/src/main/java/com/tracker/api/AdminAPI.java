package com.tracker.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tracker.domain.users.User;
import com.tracker.services.UsersService;
import com.tracker.domain.users.Role;

@Controller
@RequestMapping("/admin")
@Secured("ROLE_ADMIN")
public class AdminAPI {
	@Autowired
	private UsersService userService;
		
	
	@RequestMapping( value="", method=RequestMethod.GET )
	public String getAdminHome(@AuthenticationPrincipal User user, Model model) {
		System.out.println("Returning the admin home page");
		return "redirect:#/admin";
	}
	
	// Administrator browse all user
	@RequestMapping( value="/{uid}/users", method=RequestMethod.GET )
	@Secured({"ROLE_ADMIN"})
	@ResponseBody
	public Page<User> getAllUser( @AuthenticationPrincipal User user,
			@RequestParam(required=false, defaultValue="" ) String name,
			@RequestParam(required=false, defaultValue="" ) String email,
			@RequestParam(required=false, defaultValue="" ) String phone,
			@RequestParam(required=false, defaultValue="" ) String isAdmin,
			@RequestParam(required=false, defaultValue="0" ) String page,
			@RequestParam(required=false, defaultValue="10" ) String size ) {
		System.out.println("Browse users...");
		Pageable pageable = new PageRequest(  Integer.valueOf( page ), Integer.valueOf( size ) );

		if( name.length() > 0 ) {
			System.out.println("Browse users by NAME..." +name);
//			Page<User> result = userService.getUsersByName( name, pageable );
//			return result;

			Query query = new Query();
			query.addCriteria(Criteria.where("name").is(name) );
			Page<User> result = userService.getUsersByName( name, pageable );
			return result;
		}
		if( email.length() > 0 ) {
			System.out.println("Browse users EMAIL..."+email);
			Page<User> result = userService.getUsersByEmail( email, pageable );
			return result;	
		}
		if( phone.length() > 0 ) {
			System.out.println("Browse users PHONE..." + phone );
			Page<User> result = userService.getUsersByPhone( phone, pageable );
			return result;
		}
		if( isAdmin.length() > 0 ) {
			System.out.println("Browse users isAdmin..." + isAdmin );
			if( isAdmin.equals("true")){
				Page<User> result = userService.getUsersByRoles( true, pageable );
				return result;
			} else{
				Page<User> result = userService.getUsersByRoles( false, pageable );	
				return result;
			}
		}
		Page<User> result = userService.getUsers(pageable);
		return result;
	}
	
	
	// Administrator create user
	@RequestMapping( value="/{uid}/users", method=RequestMethod.POST )
	@Secured({"ROLE_ADMIN"})
	@ResponseBody
	public User createUser( @AuthenticationPrincipal User user ,
						@PathVariable String uid, Model model,
						@RequestBody MultiValueMap<String, String> userData) {
		System.out.println("Creating user..."+"isAdmin: "+userData.get("isAdmin").get(0)
				+" username: " +userData.get("username").get(0)
				);
		
		List<Role> roles;
		User newUser = new User();
		// Define new user's role
		if( userData.containsKey("isAdmin") && userData.get("isAdmin").get(0).equals("true") ) {
			roles = Arrays.asList( new Role[] { new Role("ROLE_ADMIN"),new Role("ROLE_USER") } );
			newUser.setAdmin(true);
		}
		else{
			roles = Arrays.asList( new Role[] { new Role("ROLE_USER") } );
			newUser.setAdmin(false);
		}
		ArrayList<String> phone = new ArrayList<String>();
		phone.add(userData.get("newPhoneNumber").get(0));
		newUser.setPhone( phone );
		newUser.setEmail(userData.get("email").get(0));
		newUser.setName(userData.get("name").get(0));
		newUser.setUsername(userData.get("username").get(0));
		newUser.setStatus("enabled");
		newUser.setPassword(userData.get("password").get(0));
		
		userService.save(newUser);
		
		// User's password is not visible for, so set it null before returning it.
		newUser.setPassword( null );
		return newUser;
	}
}
