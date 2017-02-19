package com.tracker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tracker.domain.pictures.Picture;
import com.tracker.repositories.pictures.MongoPictureRepository;

@Service
public class PictureService {
	@Autowired MongoPictureRepository pictureRepository;
	
	public Picture save( Picture picture ) {
		return pictureRepository.save( picture );
	}
	
	public void update( Picture picture ) {
		pictureRepository.update(picture);
	}
	
	public Picture findOne( String id ) {
		return pictureRepository.findOne(id);
	}
	
	public Page<Picture> findByOwnerId( String id, Pageable pageable ){
	 	return pictureRepository.findByOwnerId( id, pageable );
	}
}