/*
 * Created on 14.12.2004
 */
package com.idega.content.presentation;

import javax.faces.context.FacesContext;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.webface.WFFrame;
import com.idega.webface.WFToolbar;

/**
 * A "preview" for a webdav resource, adds an iframe to the page or possible a plugin...
 * @author <a href="mailto:eiki@idega.is">Eirikur S. Hrafnsson</a>
 */
public class WebDAVFilePreview extends ContentBlock {
	
	public static final String DEFAULT_STYLE_CLASS = "content_file_preview";

	protected void initializeComponent(FacesContext context) {
		WebdavExtendedResource resource = getWebdavExtendedResource();
		
		String filePath = resource.getPath();
		if (resource != null) {
			String resourceName = resource.getDisplayName();
			WFFrame frame = new WFFrame(resourceName,filePath);
			frame.setStyleClass(DEFAULT_STYLE_CLASS);
			frame.setToolbar(new WFToolbar());
			this.getChildren().add(frame);
		}
	}
}