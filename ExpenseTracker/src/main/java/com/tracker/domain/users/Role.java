package com.tracker.domain.users;

import org.springframework.security.core.GrantedAuthority;

public class Role implements GrantedAuthority {

	private String name;
	
	public Role() {
		
	}
	
	public Role( String name ) {
		this.name = name;
	}
	
	public String getName() { 
		return this.name;
	}
	
	public void setName( String name ) {
		this.name = name;
	}
	
	@Override
	public String getAuthority() {
		return this.name;
	}
	
}
