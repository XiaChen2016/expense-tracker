package com.tracker.services;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
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
	
	public User loadUserByEmail(String email) throws UsernameNotFoundException {	
		System.out.println("Hello from loadUserByEmail.");	
		User user = userRepository.findByEmail(email);		
		if(user == null) throw new UsernameNotFoundException("email");
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
		User exists = userRepository.findByEmail(user.getEmail());
		if( exists != null) return false;
		
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
	//if(true) return;
	List<Role> roles = Arrays.asList( new Role[] { new Role("ROLE_ADMIN") ,new Role("ROLE_USER") } );
	System.out.println("Creating user called \"bilbo\"~");
//	String password= BCrypt.hashpw("123", BCrypt.gensalt()); 
	User user = new User.Builder()
			.email("admin")
			.username("bilbo")
			.password( "123" )
			.id("0")
			.isAdmin(true)
			.roles( roles)
			.build();
	
		User temp = userRepository.findByEmail( "admin") ;
//		System.out.println("\nTemp's ID: "+temp.getId()+"\nEmail:"+ temp.getEmail() 
//							+"\nPassword:"+temp.getPassword() + "\nUsername: "+temp.getUsername() 
//							+"\nRoles: "+ temp.getRoles().get(0).getName() + ", " + temp.getRoles().get(1).getName()+"\n"
//				);
		if( userRepository.findByEmail("admin") == null ){
			System.out.println("Saving bilbo");
			userRepository.save(user);
		}
		if(  !temp.getPassword().equals("123") ||  !temp.getId().equals("0")) {
			userRepository.delete(temp.getId());
			userRepository.save(user);
		} 
		List<User> tempList = userRepository.findAll();
		System.out.println("Number of users in DB: "+tempList.size());
		Iterator<User> iter = tempList.iterator();
		while( iter.hasNext() ){
			temp = (User)iter.next();
			System.out.println("Name: "+temp.getEmail()+"\tUsername: "+temp.getUsername()+"\tPassword: "+temp.getPassword());
		}
	}
}