package com.tracker.repositories.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.tracker.domain.users.User;

public interface MongoUserRepository extends UpdateableUserRepository,
												PagingAndSortingRepository< User,String > {
	public User findByUsername(String username );
	public User findByEmail(String email );
	public Page<User> findAll( Pageable pageable );
	public Page<User> findByUsernameLike( String username, Pageable pageable );
	public Page<User> findByEmailLike( String email, Pageable pageable );
	public Page<User> findByIsAdmin( Boolean isAdmin, Pageable pageable );
	public Page<User> findByUsernameContainingAndEmailContaining( 
										String username, String email, Pageable pageable );
	public Page<User> find(
			@Param("email") String email,
			@Param("username") String username, 
			@Param("isAdmin") String isAdmin,
			Pageable pageable );
	}
