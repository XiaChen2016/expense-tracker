package com.tracker.domain.pictures;


import java.util.List;

public class Picture {
	
	private String id;
	private String ownerId;
	private String receiptId;
	private List<DetailBox> detailBoxs;
	
	public List<DetailBox> getDetailBoxs() {
		return detailBoxs;
	}
	public void setDetailUnits(List<DetailBox> detailBoxs) {
		this.detailBoxs = detailBoxs;
	}
	public String getId() {
		return id;
	}
	public void setId( String id ) {
		this.id = id;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public String getReceiptId() {
		return receiptId;
	}
	public void setReceiptId(String receiptId) {
		this.receiptId = receiptId;
	}
}
