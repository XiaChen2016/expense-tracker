package com.tracker.repositories.receipts;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.tracker.domain.receipt.Receipt;

public class MongoReceiptRepositoryImpl implements UpdateableReceiptRepository {
	@Autowired
	private MongoOperations mongo;
	
	
	private Update getUpdate( Receipt x, Receipt y){
		Update update = new Update();
		update.set( "catagoryId", y.getCatagoryId() );
		update.set( "note",  y.getNote() );
		update.set( "total",  y.getTotal() );
		update.set( "place",  y.getPlace() );
		update.set( "list_of_items",  y.getList_of_items() );
		update.set( "tags",  y.getTags() );
		update.set( "time",  y.getTime() );
		return update;
	}
	
	public void update( Receipt receipt ) {
		Query query = new Query();
		query.addCriteria( Criteria.where("id").is( receipt.getId()) );
		Receipt old = mongo.findOne( query,  Receipt.class );		
		mongo.updateFirst( query, getUpdate( old, receipt ), Receipt.class );
	}

}
