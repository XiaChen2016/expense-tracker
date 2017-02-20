package com.tracker.domain.pictures;

import java.io.File;

public class PictureWithPath {
	private File file;
	private String id;
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
