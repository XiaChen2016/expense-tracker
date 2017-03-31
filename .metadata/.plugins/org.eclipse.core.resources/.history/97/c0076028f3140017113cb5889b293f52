package com.tracker.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tracker.domain.project.Project;
import com.tracker.domain.receipt.Item;
import com.tracker.domain.receipt.Receipt;
import com.tracker.repositories.receipts.MongoReceiptRepository;

@Service
public class ReceiptsService {
	@Autowired
	private MongoReceiptRepository receiptRepository;

	@Autowired
	private ProjectsService projectService;
	
	public Page<Receipt> findByOwnerId( String id, Pageable pageable ) {
		/* Sorting is not available so far. */
//		Direction direction = Direction.DESC;
//		Query query = new Query().with( new Sort( direction,"time") );
		return receiptRepository.findByOwnerId( id, pageable );
	}
	
	public void deleteReceiptsWithOneProject( String projectId ) {
		receiptRepository.findAndRemove( projectId );
	}
	
	public void delete( String id ){
		receiptRepository.delete( id );
	}
	
	public boolean deleteAll(){
		receiptRepository.deleteAll();
		return true;
	}
	public Page<Receipt> findByProject( String pid, Pageable pageable ) {
		return receiptRepository.findByProjectId( pid, pageable );
	}
	public Receipt findOne( String id ) {
		return receiptRepository.findOne( id );
	}
	public Page<Receipt> searchReceipt( String ownerId, String place, String project, 
				String maxTotal, String minTotal, String maxDate, String minDate,  String category, Pageable pageable ) {
		String projectId ="";
		if( project.length()>0 && projectService.findByOwnerIdAndNameLike(ownerId, project) != null ) {
			projectId = projectService.findByOwnerIdAndNameLike(ownerId, project).getId().toString();
		}
		System.out.println( "project id: " + projectId);
		return receiptRepository.find( ownerId, place, projectId, maxTotal, minTotal, maxDate, minDate , category, pageable );
	}
	
	public Receipt save( Receipt receipt ) {
		return receiptRepository.save( receipt );
	}
	
	public boolean update( Receipt receipt ) {
		receiptRepository.update( receipt );
		return true;
	}
	
	public void initDatabase( String uid ) throws ParseException {
		/* For each user, create 30 receipts for him. */

		String[] notes = { "Note 1", "Note 2", "Note 3", "Note 4", "Note 5"};
		String[] itemNames = {  "Chips", "Pepsi", "Apple", "Pears","Avocado",
								"Laptop" ,"Iphone", "Earphone", "Xbox", "Calculator",
								"Backpack","Dress","Skirt","Glasses", "Boots",
								"Lamp","Sofa", "Plates", "Salad Spinner","Pillow"};
		String[] catagoryNames = { "Grocery", "Electronic Devices", "Clothing", "Home" };
		String[] projectNames = { "Thanksgiving", "Christmas", "Octoberfast", "Halloween" };
		String[] places = { "Kwik Trip", "Mall of America", "Outlets", "Airport", "Macy's" ,"JCPenny" };
		Long[] startDates = { 1087840590084L, 1108640590084L, 1128640590084L, 1208640590084L };
		Long[] endDates = { 1287840590084L, 1308640590084L, 1428640590084L, 1448641590184L };
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		for( int i = 0; i < 30; i++ ) {
			Receipt r = new Receipt();
			r.setNote( notes[ (int) ( Math.random() * 5 ) ] );
			r.setPlace( places[ (int) ( Math.random() * 6 ) ] );
			
			/* Generate dates with random numbers, and make them to be two digits' numbers. */
			Date date =  sdf.parse( "201" + (int) ( Math.random() * 6 )
							/* Month */	
							+"-" +  String.format("%02d", (int) ( Math.random() * 12 ))
							/* Day */
							+"-" +  String.format("%02d", (int) ( Math.random() * 30 )) 
							/* Hour */
							+ " " + String.format("%02d",  (int) ( Math.random() * 24 ) )
							/* Minute */
							+":"+ String.format("%02d", (int) ( Math.random() * 60 ) )
							/* Seconds */			
							+ ":" + String.format("%02d", (int) ( Math.random() * 60 ) ));
			r.setTime( date.getTime() );
			r.setOwnerId( uid );
			String[] catagory = new String[2];
			int random =  (int) ( Math.random() * 4);
			catagory[0] = catagoryNames[ random ] ;
			catagory[1] = catagoryNames[ (random + 1) % 4 ] ;
			r.setCategory( catagory );
			
			int total = 0;
			/* Create five items with every receipt. */
			List< Item > list_of_items = new ArrayList<Item>();
			for( int j = 0; j<5; j++ ) {
				int price = (int) ( Math.random() * 600 );
				total += price;
				Item item = new Item();
				item.setName( itemNames[  (int) ( Math.random() * 6 )] );
				item.setPrice( price);
				item.setQuantity( 1 );
				list_of_items.add( item );
			}
			r.setList_of_items(list_of_items);
			r.setTotal(total);
			
			/* Assign project to a receipt */
			String project = projectNames[ (int) ( Math.random() * 4) ];
			Project p = projectService.findByOwnerIdAndName( uid, project );
			if( p!= null ) {
				r.setProjectId( p.getId() );
			} else {
				p = new Project();
				p.setName( project );
				p.setOwnerId( uid );
				p.setStartDate( startDates[(int) ( Math.random() * 4) ] );
				p.setEndDate( endDates[(int) ( Math.random() * 4) ] );
				projectService.save( p );
				p = projectService.findByOwnerIdAndName( uid, project );
				r.setProjectId( p.getId() );
			}
			
			r = save( r );

			List<Receipt> receipts = p.getReceipts();
			if( receipts == null ) {
				receipts = new ArrayList<Receipt>();
			}
			receipts.add(r);
			p.setReceipts(receipts);
			p.setNumOfReceipts( p.getNumOfReceipts() +1 );
			projectService.update(p);
		}
	}
}
