package com.tracker.api;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tracker.domain.users.User;


@Controller
@RequestMapping("/user")
@Secured("ROLE_USER")
public class UserAPI {

	@RequestMapping( value="", method=RequestMethod.GET )
	public String getAdminHome(@AuthenticationPrincipal User user, Model model) {
		System.out.println("Returning the admin home page");
		return "index.html";
	}
}
