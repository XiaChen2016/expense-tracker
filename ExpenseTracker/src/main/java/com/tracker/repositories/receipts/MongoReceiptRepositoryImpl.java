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
								String maxTotal, 
								String minTotal,
								String maxDate,
								String minDate,
								String category,
								String item,
								Pageable pageable ){
		Query query = new Query();
		query.addCriteria( Criteria.where("ownerId").is( ownerId ) );
		if( place.length() > 0 ) {
			query.addCriteria( Criteria.where("place").regex( place, "i" ) );
		}
		if( projectId.length() > 0 ) {
			query.addCriteria( Criteria.where("projectId").is(projectId) );
		}
		if( category.length() > 0 ) {
			query.addCriteria( Criteria.where("category").regex( category, "i" ) );
		}
		if( item.length() >0 ) {
			System.out.println("Search receitps by items: " + item);
			query.addCriteria( Criteria.where("list_of_items").elemMatch(Criteria.where("name").regex( item, "i")) );
		}
		if( minTotal.length() > 0 ) {
			if( maxTotal.length() > 0 ) {
				/* Query for a range of total */
				query.addCriteria( Criteria.where("total")
						.gte( Double.valueOf( minTotal ) ).lte(  Double.valueOf( maxTotal ) ) );
				System.out.println( "Query by the range: " + minTotal +"~" + maxTotal );
			} else {
				/* Query by total greater than lowerLimit */
				query.addCriteria( Criteria.where("total").gte( Double.valueOf( minTotal ) ) );
				System.out.println( "Query by total greater than: " + minTotal );
			}
		} else if( maxTotal.length() > 0 ) {
			/* Query by total less than upperLimit */
			query.addCriteria( Criteria.where("total").lte( Double.valueOf( maxTotal ) ) );
			System.out.println( "Query by total less than: " + maxTotal );
		}
		if( minDate.length() > 0 ) {
			if( maxDate.length() > 0 ) {
				/* Query for a period between minDate ~ maxDate */
				query.addCriteria( Criteria.where("time")
						.gte( Long.valueOf( minDate ) ).lte(  Long.valueOf( maxDate ) ) );
				System.out.println("query for time between " + minDate+ " ~ " + maxDate);
			} else {
				/* Query for all receipts after minDate */
				query.addCriteria( Criteria.where("time").gte( Long.valueOf( minDate ) ) );
				System.out.println("query for time after " +minDate );
			}
		} else if( maxDate.length() > 0 ) {
			/* Query for all receipts before maxDate */
			query.addCriteria( Criteria.where("time").lte( Long.valueOf( maxDate ) ) );
			System.out.println("query for time before " + maxDate);
		}
		List<Receipt> result = mongo.find( query, Receipt.class );
		List<Receipt> subListOfResult = subList( result, pageable.getOffset(), pageable.getPageSize() );
		Page<Receipt> page = new PageImpl<Receipt>( subListOfResult, pageable, result.size() );
	
		return page;
	}
	private List<Receipt> subList(List<Receipt> list, int offset, int limit) {
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
	
	public void findAndRemove( String pid ) {
		Query query = new Query();
		query.addCriteria( Criteria.where("projectId").is( pid ) );
		mongo.remove( query, Receipt.class );
	}
	private Update getUpdate( Receipt x, Receipt y) {
		Update update = new Update();
		update.set( "projectId", y.getProjectId() );
		update.set( "picId", y.getPicId() );
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
