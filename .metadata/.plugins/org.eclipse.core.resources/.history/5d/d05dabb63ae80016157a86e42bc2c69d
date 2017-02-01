package com.tracker.services;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tracker.domain.users.Role;
import com.tracker.domain.users.User;
import com.tracker.repositories.users.MongoUserRepository;

@Service
public class UsersService implements UserDetailsService {
	@Autowired
	private MongoUserRepository userRepository;
	
	@Autowired
	private ProjectsService projectService;
	
	@Autowired
	private ReceiptsService receiptService;
	
	public User loadUserByUsername(String username) throws UsernameNotFoundException {	
//		System.out.println("UsersService.loadUserByUsername: " + username);
		
		User user = userRepository.findByUsername(username);
		if( user == null || user.getStatus().equals("") ) {
			return null;
		}
//		else {
//			System.out.println("User exists");
//		}
		return user;
	}
	
	public Page<User> getUsers( Pageable pageable ) {
		return  userRepository.findAll( pageable );
	}
	
	public Page<User> getUsersByName( String name, Pageable pageable ) {
		return  userRepository.findByNameLike( name, pageable );
	}
	
	public Page<User> getUsersByNameAndEmail( String name, String email, Pageable pageable ) {
		System.out.println("Search by name or email.");
		return  userRepository.findByNameContainingAndEmailContaining(  name, email, pageable );
	}
	
	public Page<User> getUsersByEmail( String email, Pageable pageable ) {
		return  userRepository.findByEmailLike( email, pageable );
	}
	
	public Page<User> getUsersByRoles( boolean isAdmin, Pageable pageable ) {
		return  userRepository.findByIsAdmin( isAdmin, pageable );
	}
	
	public User findOne( String uid){
		return userRepository.findOne( uid );
	}
	
	public boolean save( User user ) {
		
		/* Encrypt password */
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword( passwordEncoder.encode( user.getPassword() ) );
		
		/* If there exist user with the same user name or email, then return false */
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
	
//	@PostConstruct
	public void initDatabase() throws ParseException {
		System.out.println("Ready to init database.");
		/* Clear all database when first run this program, recommend for better demonstration. */
		userRepository.deleteAll();
		receiptService.deleteAll();
		projectService.deleteAll();
		
		/* we create one, along with 30 other users/administrators, for demonstrating our project. */
		
		/* Create an administrator called bilbo */
		List<Role> roles = Arrays.asList( new Role[] { new Role("ROLE_ADMIN") ,new Role("ROLE_USER") } );
		User user = new User.Builder()
				.roles(roles)
				.username( "bilbo" )
				.name("Bilbo")
				.id("0")
				.password( "123" )
				.email("bilbo@uwlax.edu")
				.status("enabled")
				.build();
		save(user);
		user  = loadUserByUsername( "bilbo" );
		
		/* Create some receipts for Bilbo */
		receiptService.initDatabase( user.getId() );
		
		String[] usernames = { "Anthony","Casey","Arlen","Cadman","Collier", "Curtis", "Grant", "Hartley", "Isaac", "Kody",
								"Kerwin", "Lombard", "Neal", "Oscar", "Owen", "Winston", "Todd", "Troy","Unwin","Seth" ,
								"Phineas","Peter", "Quade", "Renfred", "Ridley", "Godfrey", "Gregory","Halsey","Heathcliff", "Hanley"};
		String[] lastnames = { "Allman", "Arevalo", "Haynes", "Kohl","Karr",
								"Amato", "Gaines", "Wilson" ,"White", "Irish"};
		
		for( int i=0; i < 30; i++ ) {
			int indexOfLastName = (int)( Math.random() * 10 );
			ArrayList<String> phone = new ArrayList<String>();
			phone.add(	"60888" + (int)( Math.random() * 10 ) 
							+ (int)( Math.random() * 10 ) + (int)( Math.random() * 10 )
							+ (int)( Math.random() * 10 ) + (int)( Math.random() * 10 ) );
			user =	 new User.Builder()
					.username( usernames[i] )
					.name( usernames[i] +" " + lastnames[indexOfLastName] )
					.password( "123" )
					.email( usernames[i] + "@uwlax.edu")
					.status("enabled")
					.phone(phone)
					.build();
			
			/* About 40% users would be administrators */
			if( Math.random() > 0.6d ) {
				roles = Arrays.asList( new Role[] { new Role("ROLE_ADMIN") ,new Role("ROLE_USER") } );
				user.setRoles( roles );
				user.setAdmin( true );
			} else {
				roles = Arrays.asList( new Role[] { new Role("ROLE_USER") } );
				user.setRoles( roles );
				user.setAdmin( false );
			}
			save( user );
			
			/* Create some receipts for current user. */
			user  = loadUserByUsername( usernames[i] );
			receiptService.initDatabase( user.getId() );
		}
		System.out.println("Finish initing database.");
	}
}