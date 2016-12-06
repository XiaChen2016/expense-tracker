package com.tracker.repositories.Projects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.tracker.domain.project.Project;
import com.tracker.domain.receipt.Receipt;

public class MongoProjectRepositoryImpl implements UpdateableProjectRepository{
	@Autowired
	private MongoOperations mongo;

	private Update getUpdate( Project p1, Project p2 ) {
		Update update = new Update();
		update.set( "ownerId",  p2.getOwnerId() );
		update.set( "name", p2.getName() );
		return update;
	}
	public boolean deleteAll() {
		mongo.remove( new Query() , Project.class );
		return true;
	}
	public void update( Project project ) {
		Query query = new Query();
		query.addCriteria( Criteria.where("id").is( project.getId()) );
		Project old = mongo.findOne( query,  Project.class );		
		mongo.updateFirst( query, getUpdate( old, project ), Project.class );
		
	}
}
