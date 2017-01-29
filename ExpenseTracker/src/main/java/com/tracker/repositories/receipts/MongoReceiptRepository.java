package com.tracker.repositories.receipts;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
//import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import com.tracker.repositories.receipts.UpdateableReceiptRepository;
import com.tracker.domain.receipt.Receipt;

public interface MongoReceiptRepository  extends UpdateableReceiptRepository,
												PagingAndSortingRepository< Receipt,String > {
	
//	query.;
	public Page<Receipt> findByOwnerId( String id , Pageable pageable );
	public Page<Receipt> findByPlaceRegexAndProjectId( String place, String projectId, Pageable pageable );
//	public Page<Receipt> find( Query query, Pageable pageable );
	
	
	//, 'place':{$regex:?1,$options:'i'}, 'projectId':?2, 'total':?3, 'category': ?4
//	@Query("{ 'ownerId':?0, 'place':{$regex:?1,$options:'i'}, 'projectId':?2, 'total':?3, 'category': ?4 }")
	public Page<Receipt> find(	@Param("ownerId") String ownerId,
								@Param("place") String place,
								@Param("projectId") String projectId,
								@Param("total") String total, 
								@Param("category") String category,
								Pageable pageable );
//	@Query( value = "SELECT * FROM receipt r WHERE" 
//				+ "LOWER(r.place) LIKE LOWER(CONCAT('%',:place, '%')) AND " 
//				+ "r.projectId LIKE CONCAT('%',:projectId, '%') AND "
//				+ "r.total LIKE CONCAT('%',:total, '%') "
//				)
//	public Page<Receipt> searchReceipts( 	@Param("place") String place,
//											@Param("projectId") String projectId,
//											@Param("total") String total, 
//											Pageable pageable );
	
	public Page<Receipt> 
				findByPlaceContainingAndTotalContainingAndCategoryContaining
					( String place, String total, String category, Pageable pageable);
	public Page<Receipt> findByOwnerIdAndCategoryContaining( String id, String category, Pageable pageable );
}
