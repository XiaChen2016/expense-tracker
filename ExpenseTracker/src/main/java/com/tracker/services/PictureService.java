package com.tracker.services;


import java.io.File;
import java.io.IOException;

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
	public void delete( String uid , String pid ) {
		pictureRepository.delete(pid);
		File directory = new File("../ExpenseTracker/src/main/pictures/"+ uid +"/"+pid);
		try{
			delete(directory);
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
	public void deleteAll() {
		pictureRepository.deleteAll();
		File directory = new File("../ExpenseTracker/src/main/pictures");
		if( directory.exists() ) {
			try{

	               delete(directory);

	           }catch(IOException e){
	               e.printStackTrace();
	           }
		}
	}
	
	public static void delete(File file)
	    	throws IOException{

	    	if(file.isDirectory()){

	    		//directory is empty, then delete it
	    		if(file.list().length==0){

	    		   file.delete();

	    		}else{

	    		   //list all the directory contents
	        	   String files[] = file.list();

	        	   for (String temp : files) {
	        	      //construct the file structure
	        	      File fileDelete = new File(file, temp);

	        	      //recursive delete
	        	     delete(fileDelete);
	        	   }

	        	   //check the directory again, if empty then delete it
	        	   if(file.list().length==0){
	           	     file.delete();
	        	     System.out.println("Directory is deleted : "
	                                                  + file.getAbsolutePath());
	        	   }
	    		}

	    	}else{
	    		//if file, then delete it
	    		file.delete();
	    		System.out.println("File is deleted : " + file.getAbsolutePath());
	    	}
	    }
}
