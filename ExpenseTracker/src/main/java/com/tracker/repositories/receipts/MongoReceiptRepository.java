package com.tracker.repositories.receipts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.tracker.repositories.receipts.UpdateableReceiptRepository;
import com.tracker.domain.receipt.Receipt;

public interface MongoReceiptRepository  extends UpdateableReceiptRepository,
												PagingAndSortingRepository< Receipt,String > {

	public Page<Receipt> findByOwnerId( String id , Pageable pageable);
}
