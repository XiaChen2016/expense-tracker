package com.tracker.repositories.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.tracker.domain.users.User;

public interface MongoUserRepository extends //MongoRepository<User, String>, 
												UpdateableUserRepository,
											PagingAndSortingRepository< User,String > {
	public User findByEmail(String email);
	public User findByUsername(String username);
	
	/* If you extend PagingAndSortingRepository<T,ID> 
	 * and access the list of all entities, you'll get links to the first 20 entities. 
	 * To set the page size to any other number, add a limit parameter:
	 * http://localhost:8080/people/?limit=50*/
	
	public Page findAll(Pageable pageable);
	
	}
