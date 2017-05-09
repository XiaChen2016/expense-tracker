package com.tracker.repositories.users;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.tracker.domain.receipt.Receipt;
import com.tracker.domain.users.User;

public class MongoUserRepositoryImpl implements UpdateableUserRepository {
	@Autowired
	private MongoOperations mongo;
	
	public Page<User> find( String email, 
							String username, 
							String isAdmin, 
							Pageable pageable) {
		Query query = new Query();
		if( email.length() > 0 ) {
			query.addCriteria( Criteria.where("email").regex( email, "i" ));
		}
		if( username.length() > 0 ) {
			query.addCriteria( Criteria.where("username").regex( username, "i" ));
		}
		if( isAdmin.length() > 0 ) {
			query.addCriteria( Criteria.where("isAdmin").is( Boolean.valueOf(isAdmin) ));
		}
		List<User> result = mongo.find( query, User.class );
		List<User> subListOfResult = subList( result, pageable.getOffset(), pageable.getPageSize() );
		Page<User> page = new PageImpl<User>( subListOfResult, pageable, result.size() );
	
		return page;
	}
	
	private List<User> subList(List<User> list, int offset, int limit) {
	    if (offset<0) throw new IllegalArgumentException("Offset must be >=0 but was "+offset+"!");
	    if (limit<-1) throw new IllegalArgumentException("Limit must be >=-1 but was "+limit+"!");

	    if (offset>0) {
	        if (offset >= list.size()) {
	            return list.subList(0, 0); //return empty.
	        }
	        if (limit >-1) {
	            //apply offset and limit
	            return list.subList(offset, Math.min(offset+limit, list.size()));
	        } else {
	            //apply just offset
	            return list.subList(offset, list.size());
	        }
	    } else if (limit >-1) {
	        //apply just limit
	        return list.subList(0, Math.min(limit, list.size()));
	    } else {
	        return list.subList(0, list.size());
	    }
	}
	
	private Update getUpdate(User x, User y) {
		Update update = new Update();
		update.set( "password", y.getPassword() );
		update.set( "name", y.getUsername() );
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
