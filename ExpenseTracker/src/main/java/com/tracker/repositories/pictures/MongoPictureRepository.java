package com.tracker.repositories.pictures;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.tracker.domain.pictures.Picture;

public interface MongoPictureRepository extends UpdateablePictureRepository ,
											PagingAndSortingRepository	< Picture , String > {
	public Picture findOne( String id );
	public Picture findByReceiptId( String id );
}
