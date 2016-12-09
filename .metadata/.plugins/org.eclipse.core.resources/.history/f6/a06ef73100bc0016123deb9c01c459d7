package com.tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.filter.HttpPutFormContentFilter;

@EnableMongoRepositories({"com.tracker.repositories.users"})
@SpringBootApplication
public class ExpenseTrackerApplication {
	

	public static void main(String[] args) {
		SpringApplication.run(ExpenseTrackerApplication.class, args);
	}
	
	@Autowired
	public HttpPutFormContentFilter formFilter() {
		HttpPutFormContentFilter f = new HttpPutFormContentFilter();
		return f;
	}
}
