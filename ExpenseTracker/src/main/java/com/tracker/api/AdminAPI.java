package com.tracker.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	@ResponseBody
	public Page<User> getAllUser( @AuthenticationPrincipal User user,
			@RequestParam( required=false, defaultValue="" ) String name,
			@RequestParam( required=false, defaultValue="" ) String email,
			@RequestParam( required=false, defaultValue="" ) String username,
			@RequestParam( required=false, defaultValue="" ) String isAdmin,
			@RequestParam( required=false, defaultValue="0" ) String page,
			@RequestParam( required=false, defaultValue="10" ) String size ) {
		System.out.println("Browse users...");
		Pageable pageable = new PageRequest(  Integer.valueOf( page ), Integer.valueOf( size ) );

		if( name.length() > 0 || email.length() > 0 
				|| username.length() > 0 || isAdmin.length() > 0 ) {
			Page<User> result = userService.searchUsers( name, email, username, isAdmin, pageable );
			return result;
		}
		/* Hide all the passwords. */
		Page<User> result = userService.getUsers(pageable);
//		List<User> users = result.getContent();
//		for( User temp: users ) {
//			temp.setPassword(null);
//		}
//		result = new PageImpl<User>( users, pageable, users.size() );
		return result;
	}
	
	
	// Administrator create user
	@RequestMapping( value="/{uid}/users", method=RequestMethod.POST )
	@ResponseBody
	public User createUser( @AuthenticationPrincipal User user ,
						@PathVariable String uid, 
						@RequestBody User newUser,
						HttpServletResponse response
						) throws IOException {
		if( newUser.isAdmin() ) {
			List<Role> roles = Arrays.asList( new Role[] { new Role("ROLE_ADMIN") ,new Role("ROLE_USER") } );
			newUser.setRoles(roles);
		} else {
			List<Role> roles = Arrays.asList( new Role[] { new Role("ROLE_USER") } );
			newUser.setRoles(roles);
		}
		User result =  userService.save(newUser);
		if( result == null ) {
			response.sendError(400, "Invalid request, make sure you create account with unique email address.");
			return null;
		} else {
			System.out.println("Returning newly created user: " + result.getUsername());
			// User's password is not visible for admin, so set it null before returning it.
			result.setPassword( null );
			return result;
		}
	}
	
	/* Administrator edit user's profile
	 * Admin can eidt user by their name, role, and list of phone numbers
	 *  */
	@RequestMapping( value="/{uid}/users/{userid}", method=RequestMethod.PUT )
	@ResponseBody
	public User editUser(	@AuthenticationPrincipal User user ,
							@PathVariable String uid,
							@PathVariable String userid, 
							@RequestBody User editedUser,
							HttpServletResponse response
							) throws IOException {
		User temp = userService.findOne(userid);
		if( temp.getId().equals(userid) && temp.getEmail().equals( editedUser.getEmail()) ){
			userService.update( editedUser );
			return editedUser;
		} else {
			response.sendError(400, "Invalid request, make sure you don't change username and email address.");
			return null;
		}
	}
	
	@RequestMapping( value="/{uid}/users/{userid}/isAdmin", method=RequestMethod.PUT )
	@ResponseBody
	public User editUserRole(	@AuthenticationPrincipal User user ,
								@PathVariable String uid,
								@PathVariable String userid, 
								Model model,
								@RequestBody boolean isAdmin ){		
		try{
			
			User userToEdit = userService.findOne(userid);
			
//			String isAdmin = userData.get("isAdmin").get(0);
			if( isAdmin ) {
				List<Role> roles = Arrays.asList( new Role[] { new Role("ROLE_ADMIN") ,new Role("ROLE_USER") } );
				userToEdit.setRoles(roles);
				userToEdit.setAdmin(true);
			} else {
				List<Role> roles = Arrays.asList( new Role[] { new Role("ROLE_USER") } );
				userToEdit.setAdmin(false);
				userToEdit.setRoles(roles);
			}
			
			userService.update(userToEdit);
			return userToEdit;
			
		} catch( Exception e ) {
			System.out.println(e);
		}
		
		return null;
	}
	
	@RequestMapping( value="/{uid}/users/{userid}/enable", method=RequestMethod.PUT )
	@ResponseBody
	public User enableUser(	@AuthenticationPrincipal User user ,
								@PathVariable String uid,
								@PathVariable String userid, 
								Model model) {
		try{
			
			User userToEdit = userService.findOne( userid );
			userToEdit.setStatus( "enabled" );
			userService.update( userToEdit );
			return userToEdit;
			
		} catch( Exception e ) {
			System.out.println(e);
		}
		return null;
	}
	
	@RequestMapping( value="/{uid}/users/{userid}/disable", method=RequestMethod.PUT )
	@ResponseBody
	public User disableUser(	@AuthenticationPrincipal User user ,
								@PathVariable String uid,
								@PathVariable String userid, 
								Model model) {
		try{
			
			User userToEdit = userService.findOne( userid );
			userToEdit.setStatus( "disabled" );
			userService.update( userToEdit );
			return userToEdit;
			
		} catch( Exception e ) {
			System.out.println(e);
		}
		return null;
	}
}
