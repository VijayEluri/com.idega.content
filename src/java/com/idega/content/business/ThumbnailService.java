package com.idega.content.business;

import java.io.InputStream;

import org.apache.webdav.lib.WebdavResource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import com.idega.business.IBOLookup;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.graphics.image.business.ImageResizer;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

@Repository(ThumbnailService.BEAN_NAME)
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ThumbnailService extends DefaultSpringBean {
	public static final String BEAN_NAME = "thumbnailService";
	
	
	public static final int THUMBNAIL_SMALL = 50;
	public static final int THUMBNAIL_MEDIUM = 100;
	
	private static final String THUMBNAILS_FOLDER_NAME = "idega_thumbnails";
	
	private int getSize(int size){
		if(size > 0){
			return size;
		}
		return THUMBNAIL_MEDIUM;
	}
	
	
	/**
	 * Method that gets thumbnail uri if it exists or creates if it does not exists.
	 * @param filePath - path to file which thumbnail will be displayed
	 * @param thumbnailSize - height in pixels
	 * @param iwc - {@link com.idega.presentation.IWContext}
	 * @return uri to thunbnail or empty string if file does not exists
	 * @throws Exception if something goes wrong
	 */
	public String getThumbnail(String filePath,int thumbnailSize,IWContext iwc) throws Exception{
		if(StringUtil.isEmpty(filePath)){
			return CoreConstants.EMPTY;
		}
		thumbnailSize = getSize(thumbnailSize);
		String mimeType = MimeTypeUtil.resolveMimeTypeFromFileName(filePath);
		if(StringUtil.isEmpty(mimeType)){
			return getUnknownThumbnailUri(thumbnailSize,iwc);
		}
		if(mimeType.toLowerCase().contains("image")){
			String thumbnailPath = getImageThumbnailUri(filePath, thumbnailSize, iwc, mimeType);
			if(!thumbnailPath.startsWith(CoreConstants.SLASH)){
				thumbnailPath = CoreConstants.SLASH + thumbnailPath;
			}
			if(!thumbnailPath.startsWith("/content")){
				thumbnailPath = CoreConstants.WEBDAV_SERVLET_URI + thumbnailPath;
			}
			return thumbnailPath;
		}
		return getUnknownThumbnailUri(thumbnailSize,iwc);
	}
	
	private String getUnknownThumbnailUri(int thumbnailSize,IWContext iwc){
		//TODO: not implemented
		return "";
	}
	private String getImageThumbnailUri(String filePath,int thumbnailSize,IWContext iwc,String mimeType) throws Exception{
		if(iwc == null){
			iwc = CoreUtil.getIWContext();
		}
		String thumbnailPath = getThumBnailPath(filePath, thumbnailSize);
		IWSlideService iwSlideService = IBOLookup.getServiceInstance(iwc, IWSlideService.class);
		boolean exists = iwSlideService.getExistence(thumbnailPath);
		if(exists){
			return thumbnailPath;
		}
		return generateImageThumbnail(filePath, thumbnailPath, thumbnailSize, iwc, iwSlideService, mimeType);
	}
	
	private String generateImageThumbnail(String filePath, String thumbnailPath, int thumbnailSize,IWContext iwc,IWSlideService iwSlideService,String mimeType) throws Exception{
		WebdavResource file = iwSlideService.getWebdavResourceAuthenticatedAsRoot(filePath);
		InputStream input = file.getMethodData();
		InputStream image = ELUtil.getInstance().getBean(ImageResizer.class).getScaledImageIfBigger(thumbnailSize, input, getImageType(mimeType));
		iwSlideService.uploadFile(thumbnailPath.substring(0,thumbnailPath.lastIndexOf(CoreConstants.SLASH)+1), getFileName(thumbnailPath), mimeType, image);
		return thumbnailPath;
	}
	
	private String getImageType(String mimeType){
		return mimeType.substring(mimeType.indexOf(CoreConstants.SLASH)+1, mimeType.length() );
	}
	private String getThumbnailsFolder(String filePath){
		return filePath.substring(0,filePath.lastIndexOf(CoreConstants.SLASH)) + CoreConstants.SLASH + THUMBNAILS_FOLDER_NAME;
	}
	
	private String getThumBnailPath(String filePath,int thumbnailSize){
		return new StringBuilder(getThumbnailsFolder(filePath)).append(CoreConstants.SLASH)
				.append(thumbnailSize).append(CoreConstants.UNDER).append(getFileName(filePath)).toString();
	}
	private String getFileName(String filePath){
		if(!filePath.contains(CoreConstants.SLASH)){
			return filePath;
		}
		return filePath.substring(filePath.lastIndexOf(CoreConstants.SLASH) + 1, filePath.length());
	}
}
