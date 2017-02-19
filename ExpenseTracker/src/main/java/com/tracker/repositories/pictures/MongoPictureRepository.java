package com.tracker.repositories.pictures;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.tracker.domain.pictures.Picture;

public interface MongoPictureRepository extends UpdateablePictureRepository ,
											PagingAndSortingRepository	< Picture , String > {
	public Picture findOne( String id );
	public Picture findByReceiptId( String id );
	public Page<Picture> findByOwnerId( String id, Pageable pageable);
}