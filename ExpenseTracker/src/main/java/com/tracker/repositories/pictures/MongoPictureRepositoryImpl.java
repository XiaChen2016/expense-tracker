package com.tracker.repositories.pictures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.tracker.domain.pictures.Picture;
import com.tracker.domain.pictures.PictureWithPath;

public class MongoPictureRepositoryImpl implements UpdateablePictureRepository {
	@Autowired
	private MongoOperations mongo;

	public Page<PictureWithPath> findByOwnerId( String uid, Pageable pageable ) {
		Query query = new Query();
		query.addCriteria( Criteria.where("ownerId").is( uid ) );
		String pid;
		System.out.println("1 before find");
		List<Picture> pictures = mongo.find( query,  Picture.class );
		System.out.println("2 after find");
		List<PictureWithPath> list = new ArrayList<PictureWithPath>();	
		for( int i = 0; i < pictures.size(); i++ ) {
			pid = pictures.get(i).getId();
			PictureWithPath pictureWithPath = new PictureWithPath();
			pictureWithPath.setFile( new File("../ExpenseTracker/src/main/pictures/" + uid + "/" + pid));
			pictureWithPath.setId(pid);
			list.add( pictureWithPath );
		}
		Page<PictureWithPath> page = new PageImpl<PictureWithPath>( list, pageable, list.size() );
		return page;
	}
	
	@Override
	public void update( Picture picture ) {
		Query query = new Query();
		query.addCriteria( Criteria.where("id").is( picture.getId()) );
		Picture old = mongo.findOne( query,  Picture.class );		
		mongo.updateFirst( query, getUpdate( old, picture ), Picture.class );
	}

	private Update getUpdate( Picture p1, Picture p2 ) {
		Update update = new Update();
		update.set( "ownerId", p2.getOwnerId() );
		update.set( "receiptId", p2.getReceiptId() );
		update.set( "textAnnotations", p2.getTextAnnotations() );
		return update;
	}
}
