package com.tracker.repositories.users;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tracker.domain.users.User;

public interface MongoUserRepository extends MongoRepository<User, String>, UpdateableUserRepository {
	public User findByEmail(String email);
	public User findByUsername(String username);
}
