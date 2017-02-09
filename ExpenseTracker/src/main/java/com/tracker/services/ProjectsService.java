package com.tracker.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tracker.domain.project.Project;
import com.tracker.repositories.Projects.MongoProjectRepository;

@Service
public class ProjectsService {

	@Autowired
	private MongoProjectRepository projectRepository;
	
	public boolean deleteAll(){
		projectRepository.deleteAll();
		return true;
	}
	
	public boolean save( Project project ) {
		projectRepository.save( project );
		return true;
	}
	
	public void update( Project project ) {
		projectRepository.update( project );
	}
	public Project delete( String id ) {
		return projectRepository.delete(id);
	}
	public Project findOne( String id ) {
		return projectRepository.findOne(id);
	}
	
	public List<Project> findByOwnerId( String id ){
		return projectRepository.findByOwnerId(id);
	}
	
	public Project findByOwnerIdAndNameLike( String id, String name ){
		return projectRepository.findByOwnerIdAndNameLike( id, name ); 
	}
	
	public Project findByOwnerIdAndName( String id, String name ){
		return projectRepository.findByOwnerIdAndName( id, name ); 
	}
	
}
