package com.tracker.domain.receipt;

public class Receipt {

	private String id;
	private String ownerId;
	private String time;
	private String place;
	private String[] nameList;
	private double[] singlePriceList;
	private int[] quantityList;
	private double total;
	private String catagoryId;
	private String[] tags;
	private String note;
	
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
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public String[] getNameList() {
		return nameList;
	}
	public void setNameList(String[] nameList) {
		this.nameList = nameList;
	}
	public double[] getSinglePriceList() {
		return singlePriceList;
	}
	public void setSinglePriceList(double[] singlePriceList) {
		this.singlePriceList = singlePriceList;
	}
	public int[] getQuantityList() {
		return quantityList;
	}
	public void setQuantityList(int[] quantityList) {
		this.quantityList = quantityList;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public String getCatagoryId() {
		return catagoryId;
	}
	public void setCatagoryId(String catagoryId) {
		this.catagoryId = catagoryId;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
}
