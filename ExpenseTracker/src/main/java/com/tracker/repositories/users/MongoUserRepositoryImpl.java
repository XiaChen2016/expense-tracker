package com.tracker.repositories.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.tracker.domain.users.User;

public class MongoUserRepositoryImpl implements UpdateableUserRepository {
	@Autowired
//	@Qualifier("defaultMongoTemplate")
	private MongoOperations mongo;
		
	private Update getUpdate(User x, User y) {
		Update update = new Update();
		update.set( "password", y.getPassword() );
		update.set( "name", y.getName() );
		update.set( "status", y.getStatus() );
		update.set( "isAdmin", y.isAdmin() );
		update.set( "phone", y.getPhone() );
		update.set( "roles", y.getRoles() );
		return update;
	}
	
	@Override
	public void update(User user) {
		System.out.println("Hello from MongoUserRepositoryImpl : update, status: "+ user.getStatus());
		Query query = new Query();
		query.addCriteria( Criteria.where("id").is(user.getId()) );
		User old = mongo.findOne(query,  User.class);		
		mongo.updateFirst(query, getUpdate(old, user), User.class);
	}
}
