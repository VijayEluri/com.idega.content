package com.idega.content.presentation;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

/**
 * @author gimmi
 */
public class ContentViewerTag extends UIComponentTag {

	private String rootFolder;
	private String startFolder;
	private boolean useUserHomeFolder;
	private String iconTheme;
	private boolean showFolders = true;
	private String columnsToHide;
	private boolean useVersionControl = true;
	private boolean showPermissionTab = true;
	private boolean showUploadComponent = true;
	
	public void setRootPath(String root) {
		rootFolder = root;
	}
	
	public String getRootPath() {
		return rootFolder;
	}
	
	public void setStartPath(String start) {
		this.startFolder = start;
	}
	
	public String getStartPath() {
		return startFolder;
	}
	
	public void setUseUserHomeFolder(boolean useUserFolder) {
		useUserHomeFolder = useUserFolder;
	}
	
	public void setIconTheme(String themeName) {
		iconTheme = themeName;
	}
	
	public String getIconTheme() {
		return iconTheme;
	}
	
	public void setShowFolders(boolean showFolders) {
		this.showFolders = showFolders;
	}
	
	public void setColumnsToHide(String columns) {
		this.columnsToHide = columns;
	}
	
	public void setUseVersionControl(boolean useVersionControl) {
		this.useVersionControl = useVersionControl;
	}
	
	/**
	 * @return Returns the showPermissionTab.
	 */
	public boolean getShowPermissionTab() {
		return showPermissionTab;
	}
	/**
	 * @param showPermissionTab The showPermissionTab to set.
	 */
	public void setShowPermissionTab(boolean showPermissionTab) {
		this.showPermissionTab = showPermissionTab;
	}	
	
	/**
	 * @return Returns the showUploadComponent.
	 */
	public boolean getShowUploadComponent() {
		return showUploadComponent;
	}
	/**
	 * @param showUploadComponent The showUploadComponent to set.
	 */
	public void setShowUploadComponent(boolean showUploadComponent) {
		this.showUploadComponent = showUploadComponent;
	}
	
	public void release() {      
		super.release();      
		rootFolder = null ;
		startFolder = null;
		useUserHomeFolder = false;
		showFolders = true;
		iconTheme = null;
		columnsToHide = null;
		useVersionControl = true;
		showPermissionTab = true;
		showUploadComponent = true;
	}

	protected void setProperties(UIComponent component) {
		if (component != null) {
			ContentViewer viewer = (ContentViewer) component;
			super.setProperties(component);

			viewer.setRootFolder(rootFolder);
			viewer.setStartFolder(startFolder);
			viewer.setUseUserHomeFolder(useUserHomeFolder);
			viewer.setIconTheme(iconTheme);
			viewer.setShowFolders(showFolders);
			viewer.setColumnsToHide(columnsToHide);
			viewer.setUseVersionControl(useVersionControl);
			viewer.setShowPermissionTab(showPermissionTab);
			viewer.setShowUploadComponent(showUploadComponent);
		}
	}

	
	public String getComponentType() {
		return "ContentViewer";
	}

	public String getRendererType() {
		return null;
	}

}
