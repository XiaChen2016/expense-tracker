package com.tracker.repositories.Projects;

import com.tracker.domain.project.Project;

public interface UpdateableProjectRepository {
	public void update( Project project );
}
