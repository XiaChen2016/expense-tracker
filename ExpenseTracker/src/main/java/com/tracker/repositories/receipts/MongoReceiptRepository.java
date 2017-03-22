package com.tracker.repositories.receipts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import com.tracker.repositories.receipts.UpdateableReceiptRepository;
import com.tracker.domain.receipt.Receipt;

public interface MongoReceiptRepository  extends UpdateableReceiptRepository,
												PagingAndSortingRepository< Receipt,String > {
	
	public Page<Receipt> findByOwnerId( String id , Pageable pageable );
	public Page<Receipt> findByPlaceRegexAndProjectId( String place, String projectId, Pageable pageable );
	public Page<Receipt> findByProjectId( String pid, Pageable pageable );
	public Receipt findOne( String id );
	public Page<Receipt> find(	@Param("ownerId") String ownerId,
								@Param("place") String place,
								@Param("projectId") String projectId,
								@Param("maxTotal") String maxTotal, 
								@Param("minTotal") String minTotal,
								@Param("maxDate") String maxDate, 
								@Param("minDate") String minDate,
								@Param("category") String category,
								Pageable pageable );
	public void findAndRemove( String pid );
	public Page<Receipt> 
				findByPlaceContainingAndTotalContainingAndCategoryContaining
					( String place, String total, String category, Pageable pageable);
	public Page<Receipt> findByOwnerIdAndCategoryContaining( String id, String category, Pageable pageable );
}
