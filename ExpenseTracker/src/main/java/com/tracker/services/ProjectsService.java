package com.tracker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tracker.domain.project.Project;
import com.tracker.repositories.projects.MongoProjectRepository;

@Service
public class ProjectsService {

	@Autowired
	private MongoProjectRepository projectRepository;
	
	public boolean deleteAll(){
		projectRepository.deleteAll();
		return true;
	}
	
	public Project save( Project project ) {
		return projectRepository.save( project );
	}
	
	public void update( Project project ) {
		projectRepository.update( project );
	}
	public void delete( String id ) {
		projectRepository.delete(id);
	}
	public Project findOne( String id ) {
		return projectRepository.findOne(id);
	}
	
	public Page<Project> findByOwnerId( String id, Pageable pageable ){
		return projectRepository.findByOwnerId(id, pageable);
	}
	
	public Project findByOwnerIdAndNameLike( String id, String name ){
		return projectRepository.findByOwnerIdAndNameLikeIgnoreCase( id, name ); 
	}
	
	public Project findByOwnerIdAndName( String id, String name ){
		return projectRepository.findByOwnerIdAndName( id, name ); 
	}
	
}
