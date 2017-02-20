package com.tracker.repositories.pictures;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.tracker.domain.pictures.Picture;
import com.tracker.domain.pictures.PictureWithPath;

public interface MongoPictureRepository extends UpdateablePictureRepository ,
											PagingAndSortingRepository	< Picture , String > {
	public Picture findOne( String id );
	public Picture findByReceiptId( String id );
	public Page<PictureWithPath> findByOwnerId( String id, Pageable pageable);
}
