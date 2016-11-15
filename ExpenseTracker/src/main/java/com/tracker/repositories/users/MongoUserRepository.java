package com.tracker.repositories.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.tracker.domain.users.User;

public interface MongoUserRepository extends //MongoRepository<User, String>, 
												UpdateableUserRepository,
											PagingAndSortingRepository< User,String > {
	public User findByUsername(String username);
	public User findByEmail(String username);

	public Page<User> findAll( Pageable pageable);
	public Page<User> findByNameLike( String name, Pageable pageable );
	public Page<User> findByEmailLike( String email, Pageable pageable );
	public Page<User> findByIsAdmin( Boolean isAdmin, Pageable pageable );
//	public Page<User> findByEmailOrNameLike( String email, String name, Pageable pageable );
	}
