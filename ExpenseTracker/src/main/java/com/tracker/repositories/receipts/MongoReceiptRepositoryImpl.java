package com.tracker.repositories.receipts;

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

public class MongoReceiptRepositoryImpl implements UpdateableReceiptRepository {
	@Autowired
	private MongoOperations mongo;
	
	public Page<Receipt> find(	String ownerId,
								String place,
								String projectId,
								String upperLimit, 
								String lowerLimit,
								String category,
								Pageable pageable ){
		Query query = new Query();
		query.addCriteria( Criteria.where("ownerId").is(ownerId) );
		if( place.length() > 0 ) {
			query.addCriteria( Criteria.where("place").regex( place, "i" ) );
		}
		if( projectId.length() > 0 ) {
			query.addCriteria( Criteria.where("projectId").is(projectId) );
		}
		if( category.length() > 0 ) {
			query.addCriteria( Criteria.where("category").regex( category, "i" ) );
		}
		if( lowerLimit.length() > 0 ) {
			if( upperLimit.length() > 0 ) {
				/* Query for a range of total */
				query.addCriteria( Criteria.where("total")
						.gt( Double.valueOf( lowerLimit ) ).lt(  Double.valueOf(upperLimit ) ) );
				System.out.println( "Query by the range: " + lowerLimit +"~" + upperLimit );
			} else {
				/* Query by total greater than lowerLimit */
				query.addCriteria( Criteria.where("total").gt( Double.valueOf( lowerLimit ) ) );
				System.out.println( "Query by total greater than: " + lowerLimit );
			}
		} else if( upperLimit.length() > 0 ) {
			/* Query by total less than upperLimit */
			query.addCriteria( Criteria.where("total").lt( Double.valueOf( upperLimit ) ) );
			System.out.println( "Query by total less than: " + upperLimit );
		}
		List<Receipt> result = mongo.find( query, Receipt.class );
		Page<Receipt> page = new PageImpl<Receipt>( result, pageable, result.size() );
		return page;
	}
	
	private Update getUpdate( Receipt x, Receipt y){
		Update update = new Update();
		update.set( "projectId", y.getProjectId() );
		update.set( "note",  y.getNote() );
		update.set( "total",  y.getTotal() );
		update.set( "place",  y.getPlace() );
		update.set( "list_of_items",  y.getList_of_items() );
		update.set( "category",  y.getCategory() );
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
