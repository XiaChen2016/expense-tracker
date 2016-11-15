package com.tracker.services;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tracker.domain.receipt.Item;
import com.tracker.domain.receipt.Receipt;
import com.tracker.repositories.receipts.MongoReceiptRepository;

@Service
public class ReceiptsService {
	@Autowired
	private MongoReceiptRepository receiptRepository;
	
	public Page<Receipt> findByOwnerId( String id, Pageable pageable ) {
		return receiptRepository.findByOwnerId(id, pageable);
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
		receiptRepository.delete(id);
	}
	
	@PostConstruct
	public void initDatabase() {
		Receipt r1 = new Receipt();
		r1.setNote("This is a receipt of LeBron James(user2).");
		r1.setPlace("Kwik Trip");
		r1.setTime("10/28/2016 10:11am");
		r1.setTags( new String[] {"Food"} );
		r1.setOwnerId("58064ab0d72e4e3fe20c2744");
		Item i = new Item();
		i.setName("Pear");
		i.setName("Pine apple");
		i.setName("Watermelon");
		i.setPrice( 1f);
		i.setQuantity(2);
		List< Item > list_of_items = new ArrayList<Item>();
		list_of_items.add(i);
		r1.setList_of_items(list_of_items);
		r1.setTotal(6.0f);
//		save( r1 );
		
		r1.setNote("This is a receipt of Bilbo.");
		r1.setPlace("Mall of America");
		r1.setTime("11/27/2001 05:32pm");
		r1.setTags( new String[] {"make up"} );
		r1.setOwnerId("0");
		
//		save( r1 );
		

	}
}
