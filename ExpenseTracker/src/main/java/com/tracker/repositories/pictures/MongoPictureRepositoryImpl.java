package com.tracker.repositories.pictures;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.tracker.domain.pictures.Picture;

public class MongoPictureRepositoryImpl implements UpdateablePictureRepository {
	@Autowired
	private MongoOperations mongo;
	
	@Override
	public void update( Picture picture ) {
		Query query = new Query();
		query.addCriteria( Criteria.where("id").is( picture.getId()) );
		Picture old = mongo.findOne( query,  Picture.class );		
		mongo.updateFirst( query, getUpdate( old, picture ), Picture.class );
	}

	private Update getUpdate( Picture p1, Picture p2 ) {
		Update update = new Update();
		update.set( "ownerId", p2.getOwnerId() );
		update.set( "receiptId", p2.getReceiptId() );
		update.set( "detailBoxs", p2.getDetailBoxs() );
		return update;
	}
}
