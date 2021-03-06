package com.tracker.domain.receipt;

import java.util.List;

public class Receipt {

	private String id;
	private String ownerId;
	private Long time;
	private String picId;
	private String place;
	private double total;
	private String projectId;
	private String[] category;
	private String note;
	private List< Item > list_of_items;
	
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
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public String getPicId() {
		return picId;
	}
	public void setPicId( String picId ) {
		this.picId = picId;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public List<Item> getList_of_items() {
		return list_of_items;
	}
	public void setList_of_items(List<Item> list_of_items) {
		this.list_of_items = list_of_items;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String[] getCategory() {
		return category;
	}
	public void setCategory(String[] category) {
		this.category = category;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
}
