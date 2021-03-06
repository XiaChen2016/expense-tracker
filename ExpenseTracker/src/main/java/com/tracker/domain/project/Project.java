package com.tracker.domain.project;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.tracker.domain.receipt.Receipt;

public class Project {

	private String id;
	private String ownerId;
	private String name;
	private Long startDate;
	private Long endDate;
	private int numOfReceipts;
	
	@JsonIgnore
	@DBRef( lazy = true )
	private List<Receipt> receipts;

	public int getNumOfReceipts() {
		return numOfReceipts;
	}
	public void setNumOfReceipts(int numOfReceipts) {
		this.numOfReceipts = numOfReceipts;
	}
	public List<Receipt> getReceipts() {
		return receipts;
	}
	public void setReceipts(List<Receipt> receipts) {
		this.receipts = receipts;
	}
	public Long getStartDate() {
		return startDate;
	}
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}
	public Long getEndDate() {
		return endDate;
	}
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
