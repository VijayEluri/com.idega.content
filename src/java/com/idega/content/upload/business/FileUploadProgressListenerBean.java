package com.idega.content.upload.business;

import java.text.NumberFormat;
import java.util.Locale;


public class FileUploadProgressListenerBean implements FileUploadProgressListener {
	
	private long bytesTransferred = 0;
	private long fileSize = 0;

	public void update(long bytesRead, long contentLength, int items) {
		bytesTransferred = bytesRead;
		fileSize = contentLength;
	}
	
	public String getFileUploadStatus() {
		if (fileSize == 0) {
			return null;
		}
		
		String status = NumberFormat.getPercentInstance(Locale.ENGLISH).format((double) bytesTransferred / (double) fileSize);
		return status.substring(0, status.length() - 1);
	}
	
	public boolean resetFileUploaderCounters() {
		bytesTransferred = 0;
		fileSize = 0;
		
		return true;
	}
	
}