package com.tracker.services;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tracker.domain.users.Role;
import com.tracker.domain.users.User;
import com.tracker.repositories.users.MongoUserRepository;

@Service
public class UsersService implements UserDetailsService {
	@Autowired
	private MongoUserRepository userRepository;
		
	public User loadUserByUsername(String username) throws UsernameNotFoundException {	
		System.out.println("Hello from loadUserByUsername.");	
		User user = userRepository.findByUsername(username);
		if(user == null) throw new UsernameNotFoundException("username");
		return user;
	}
	
	public List<User> getUsers() {
		return userRepository.findAll();
	}
	
	public User findOne( String uid){
		System.out.println("Hello from findOne.");
		return userRepository.findOne( uid );
	}
	public boolean save( User user ) {
		// Encrypt password
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword( passwordEncoder.encode( user.getPassword() ) );
		System.out.println("Saving a user with password: "+ user.getPassword());
		// If there exist user with the same user name or email, then return false
		if( userRepository.findByUsername(user.getUsername()) != null 
				|| userRepository.findByEmail(user.getEmail()) != null )
			return false;
		
		userRepository.save( user );
		return true;
	}
	
	public boolean update( User user ) {
		userRepository.update( user );
		return true;
	}
	
	public void delete( String uid  ){
		userRepository.delete(uid);
	}
	
	@PostConstruct
	private void initDatabase() {
		userRepository.deleteAll();
		
	//if(true) return;
	List<Role> roles = Arrays.asList( new Role[] { new Role("ROLE_ADMIN") ,new Role("ROLE_USER") } );
	System.out.println("Creating user called \"bilbo\"~");
	BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	String hashedPassword = passwordEncoder.encode("123");
	User user = new User.Builder()
				.username("bilbo")
				.password( "123" )
				.email("bilbo@uwlax.edu")
				.id("0")
				.status("enabled")
				.isAdmin(true)
				.roles( roles)
				.build();
	save(user);

	roles =  Arrays.asList( new Role[] { new Role("ROLE_USER") });
	 user = new User.Builder()
				.username("user1")
				.password( "123" )
				.email("user@uwlax.edu")
				.id("1")
				.status("enabled")
				.isAdmin(false)
				.roles( roles)
				.build();
		save(user);
//	System.out.println( "findOne" + loadUserByUsername("bilbo") );
//	if( loadUserByUsername("bilbo") != null )
//		delete( loadUserByUsername("bilbo").getId());
	User temp;
	System.out.println( "Load all users: ");
	List<User> tempList = userRepository.findAll();
	System.out.println("Number of users in DB: "+tempList.size());
	Iterator<User> iter = tempList.iterator();
	while( iter.hasNext() ){
		temp = (User)iter.next();
		System.out.println("Name: "+temp.getEmail()+"\tUsername: "+temp.getUsername()+"\tPassword: "+temp.getPassword());
	}
	}
}