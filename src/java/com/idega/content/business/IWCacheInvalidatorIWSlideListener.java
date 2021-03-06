package com.idega.content.business;

import com.idega.idegaweb.IWCacheManager;
import com.idega.idegaweb.IWMainApplication;
import com.idega.slide.business.IWContentEvent;
import com.idega.slide.business.IWSlideChangeListener;

/**
 * A IWSlide listener that listens for a the given starting path and invalidates the given Block cache key when an update to that path happens
 * @author eiki
 *
 */
public class IWCacheInvalidatorIWSlideListener implements IWSlideChangeListener{

	private final String startingPath;
	private final String cacheKey;
	
	public IWCacheInvalidatorIWSlideListener(String startingPath, String cacheKey){
		this.startingPath = (startingPath.startsWith("/"))?startingPath:"/"+startingPath;
		this.cacheKey = cacheKey;
	}
	
	public void onSlideChange(IWContentEvent contentEvent) {
		if(contentEvent.getContentEvent().getUri().startsWith(this.startingPath)){
			IWCacheManager.getInstance(IWMainApplication.getDefaultIWMainApplication()).invalidateCache(cacheKey);
		}		
	}
}
