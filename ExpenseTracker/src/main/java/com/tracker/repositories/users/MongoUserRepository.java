package com.tracker.repositories.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.tracker.domain.users.User;

public interface MongoUserRepository extends //MongoRepository<User, String>, 
												UpdateableUserRepository,
											PagingAndSortingRepository< User,String > {
	public User findByUsername(String username);
	public User findByEmail(String username);
	
	/* If you extend PagingAndSortingRepository<T,ID> 
	 * and access the list of all entities, you'll get links to the first 20 entities. 
	 * To set the page size to any other number, add a limit parameter:
	 * http://localhost:8080/people/?limit=50*/

	public Page<User> findAll( Pageable pageable);
	public Page<User> findByNameLike( String name, Pageable pageable );
	public Page<User> findByEmailLike( String email,Pageable pageable );
	public Page<User> findByPhoneLike( String phone, Pageable pageable);
	public Page<User> findByIsAdmin( Boolean isAdmin, Pageable pageable );
	
	}
