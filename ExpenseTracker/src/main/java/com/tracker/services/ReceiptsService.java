package com.tracker.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.tracker.domain.receipt.Item;
import com.tracker.domain.receipt.Receipt;
import com.tracker.repositories.receipts.MongoReceiptRepository;

@Service
public class ReceiptsService {
	@Autowired
	private MongoReceiptRepository receiptRepository;
	
	public Page<Receipt> findByOwnerId( String id, Pageable pageable ) {
		Direction direction = Direction.DESC;
		Query query = new Query().with(new Sort( direction,"time"));
		return receiptRepository.findByOwnerId( id, pageable, query );
	}
	
	public Receipt findOne( String id ) {
		return receiptRepository.findOne(id);
	}
	public Page<Receipt> searchReceipt( String id, String place, String project, String total, String category, Pageable pageable ) {
		
		return receiptRepository
//					.findByPlaceContainingAndTotalContainingAndCategoryContaining( place, total, category, pageable );
//				.findByOwnerIdAndPlaceContainingAndCategoryContaining( id, place,category, pageable );
				.findByOwnerIdAndCategoryContaining( id,category, pageable );
	}
	
	public boolean save( Receipt receipt ) {
		receiptRepository.save( receipt );
		return true;
	}
	
	public boolean update( Receipt receipt ) {
		receiptRepository.update(receipt);
		return true;
	}
	
	public void delete( String id ){
//		receiptRepository.delete(id);
		receiptRepository.deleteAll();
	}
	
	
	private static String[] notes = {"Note 1", "Note 2", "Note 3", "Note 4", "Note 4"};
	private static String[] dates = {"...", "...", "..." };
	
	@PostConstruct
	public void initDatabase() throws ParseException {
//		System.out.println( "Hello from receipt service. " );
//		delete("1");
		Receipt r1 = new Receipt();
		r1.setNote("This is a receipt of Bilbo 2014-11-20 09:32:11.");
		r1.setPlace("Kwik Trip");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date =  sdf.parse("2015-09-20 09:32:11");
		r1.setTime( date );
		r1.setCategory( new String[] {"Grocery","Fruit"} );
		r1.setOwnerId( "0" );
		Item i = new Item();
		i.setName("Apple");
		i.setPrice( 2f );
		i.setQuantity( 1 );
		List< Item > list_of_items = new ArrayList<Item>();
		list_of_items.add(i);
		r1.setList_of_items(list_of_items);
		r1.setTotal( 2 );
//		save( r1 );
		

	}
}
