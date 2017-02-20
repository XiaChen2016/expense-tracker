package com.tracker.domain.pictures;


import java.util.List;

import com.google.api.services.vision.v1.model.EntityAnnotation;


public class Picture {
	private String id;
	private String ownerId;
	private String receiptId;
	private List<EntityAnnotation> textAnnotations;
	
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
	public String getReceiptId() {
		return receiptId;
	}
	public void setReceiptId(String receiptId) {
		this.receiptId = receiptId;
	}
	public List<EntityAnnotation> getTextAnnotations() {
		return textAnnotations;
	}
	public void setTextAnnotations( List<EntityAnnotation> textAnnotations ) {
		this.textAnnotations = textAnnotations ;
	}
}
