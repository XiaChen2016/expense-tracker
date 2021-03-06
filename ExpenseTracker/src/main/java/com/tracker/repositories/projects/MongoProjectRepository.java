package com.tracker.repositories.projects;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.tracker.domain.project.Project;


public interface MongoProjectRepository extends UpdateableProjectRepository ,
												PagingAndSortingRepository	< Project , String > {
	public Project findOne( String id );
	public Page<Project> findByOwnerId( String id, Pageable pageable );
	public Project findByOwnerIdAndNameLikeIgnoreCase( String id, String name );
	public Project findByOwnerIdAndName( String id, String name );
}
