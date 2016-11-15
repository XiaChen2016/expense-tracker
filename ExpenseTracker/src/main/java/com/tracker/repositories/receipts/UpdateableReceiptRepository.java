package com.tracker.repositories.receipts;

import com.tracker.domain.receipt.Receipt;

public interface UpdateableReceiptRepository {
	public void update( Receipt receipt );
}
