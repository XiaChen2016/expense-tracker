package com.tracker.domain.pictures;


import com.google.api.services.vision.v1.model.AnnotateImageResponse;


public class Picture {
	private String id;
	private String ownerId;
	private String receiptId;
	private AnnotateImageResponse textAnnotation;
	
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
	public AnnotateImageResponse getTextAnnotation() {
		return textAnnotation;
	}
	public void setTextAnnotation( AnnotateImageResponse textAnnotation ) {
		this.textAnnotation = textAnnotation ;
	}
}