package com.tracker.services;


import org.springframework.beans.factory.annotation.Autowired;
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
}
