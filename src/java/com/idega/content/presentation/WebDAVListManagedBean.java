package com.idega.content.presentation;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.faces.component.UIColumn;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlGraphicImage;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.DataModel;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResources;
import com.idega.business.IBOLookup;
import com.idega.content.business.WebDAVBeanComparator;
import com.idega.content.data.WebDAVBean;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.WebdavExtendedResource;
import com.idega.webface.WFList;
import com.idega.webface.WFUtil;
import com.idega.webface.bean.WFListBean;
import com.idega.webface.model.WFDataModel;

/**
 * A managed bean for the WebDAVList component
 * @author gimmi
 */
public class WebDAVListManagedBean implements ActionListener, WFListBean {

	private static final String P_ID = "wb_list";
	public static final String PARAMETER_WEB_DAV_URL = "wdurl";
	public static final String PARAMETER_IS_FOLDER = "isf";
	private static final String ACTION_SORT = "wdlmb";
	private static final String SORT_BY_NAME = "name";
	private static final String SORT_BY_SIZE = "size";
	private static final String SORT_BY_MODIFICATION_DATE = "modDate";
	private static final String SORT_BY_NAME_DESC = "name_desc";
	private static final String SORT_BY_SIZE_DESC = "size_desc";
	private static final String SORT_BY_MODIFICATION_DATE_DESC = "modDate_desc";

	private String clickedFilePath;
	private String clickedFileName;
	
	private String webDAVPath = "";
	private String rootPath = null;
	private String startPath = null;
	private String iconTheme = null;
	
	private int startPage = -1;
	private int rows = -1;
	private String sorter = SORT_BY_NAME;

	public WebDAVListManagedBean() {
	}
	
	public void resetSorter() {
		sorter = SORT_BY_NAME;
	}
	
	public void setWebDAVPath(String path) {
		this.webDAVPath = path;
	}
	
	public String getWebDAVPath() {
		return webDAVPath;
	}
	
	public boolean getIsClickedFile() {
		return (getClickedFilePath() != null && !("".equals(getClickedFilePath()))  );
	}
	
	public void setClickedFilePath(String path) {
		this.clickedFilePath = path;
	}
	
	public String getClickedFilePath() {
		return clickedFilePath;
	}
	
	public String getClickedFileName() {
		return clickedFileName;
	}

	public void setStartFolder(String start) {
		if (start != null && "".equals(start)) {
			start = null;
		}
		this.startPath = start;
	}
	
	public void setRootFolder(String root) {
		if (root != null && "".equals(root)) {
			root = null;
		}
		this.rootPath = root;
	}
	
	public void setIconTheme(String theme) {
		if (theme != null && "".equals(theme)) {
			theme = null;
		}
		this.iconTheme = theme;
	}

	public void refresh(UIComponent comp) {
		ContentBlock block = null;
		while (comp != null && block == null) {
			if (comp instanceof ContentBlock) {
				block = (ContentBlock) comp;
				block.getContentViewer().setEventHandled(true);
			} else {
				comp = comp.getParent();
			}
		}
		updateDataModel(new Integer(startPage), new Integer(rows));
		
	}
	
	public void processAction(ActionEvent actionEvent) throws AbortProcessingException {
		UIComponent comp = actionEvent.getComponent();
		
		ContentViewer v = null;
		UIComponent tmp = comp.getParent();
		while ( tmp != null && v == null) {
			if (tmp instanceof ContentViewer) {
				v = (ContentViewer) tmp;
			}
			else {
				tmp = tmp.getParent();
			}
		}
		v.setEventHandled(true);
		
		boolean isFolder = true;
		
		String sortBy = (String) comp.getAttributes().get(ACTION_SORT);
		if (sortBy != null) {
			sorter = sortBy;
		}
		else {
			if (comp instanceof UICommand) {
				List children = comp.getChildren();
				Iterator iter = children.iterator();
				UIComponent child;
				UIParameter par;
				while (iter.hasNext()) {
					child = (UIComponent) iter.next();
					if (child instanceof UIParameter) {
						par = (UIParameter) child;
						if (PARAMETER_WEB_DAV_URL.equals(par.getName()) ) {
							webDAVPath = (String) par.getValue();
						} else if (PARAMETER_IS_FOLDER.equals(par.getName())) {
							isFolder = ((Boolean) par.getValue()).booleanValue();
						}
					}
						
				}
	
			}
			
			WFList parent = getWFListParent(comp);
			
			if (webDAVPath != null && parent != null) {
	//			WFList parentList = (WFList) parent;
				if (isFolder) {
					this.setClickedFilePath(null);
	//				this.updateDataModel(new Integer(parentList.getFirst()), new Integer(parentList.getRows()));
				} else {
					this.setClickedFilePath(webDAVPath);
					int index = webDAVPath.lastIndexOf("/");
					if (index > -1) {
						clickedFileName = webDAVPath.substring(index+1);
						webDAVPath = webDAVPath.substring(0, index);
					}
				}
			}
			
			v.setCurrentFolderPath(webDAVPath);
			v.setCurrentFileName(getClickedFileName());
		}
	}

	private WFList getWFListParent(UIComponent comp) {
		UIComponent parent = (UIComponent) comp.getParent();
		while (parent != null && !(parent instanceof WFList)) {
			parent = parent.getParent();
		}
		if (parent instanceof WFList) {
			return (WFList) parent;
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.idega.webface.bean.WFListBean#createColumns(java.lang.String)
	 */
	public UIColumn[] createColumns(String var) {
		
		String imageSize = "16";
		
		UIColumn col0 = new UIColumn();
		
		HtmlGraphicImage icon = new HtmlGraphicImage();
		icon.setValueBinding("url", WFUtil.createValueBinding("#{"+var+".iconURL}"));
		icon.setId(P_ID+"_I");
		icon.setHeight("16");// sizes that make sense 16/32/64/128
		
		HtmlCommandLink iconLink = new HtmlCommandLink();
		iconLink.setId(P_ID+"_L");
		
		WFUtil.addParameterVB(iconLink, PARAMETER_WEB_DAV_URL, var + ".webDavUrl");
		WFUtil.addParameterVB(iconLink, PARAMETER_IS_FOLDER, var + ".isCollection");
		iconLink.setActionListener(WFUtil.createMethodBinding("#{"+WebDAVList.WEB_DAV_LIST_BEAN_ID+".processAction}", new Class[]{ActionEvent.class}));
		iconLink.getChildren().add(icon);
		col0.getChildren().add(iconLink);
		
		UIColumn col = new UIColumn();
		HtmlCommandLink nameSortLink = new HtmlCommandLink();
		HtmlOutputText nameSortText = ContentBlock.getBundle().getLocalizedText("name");
		if (SORT_BY_NAME.equals(sorter)) {
			nameSortText.setStyleClass("wf_listheaderlink_clicked");
			nameSortLink.getAttributes().put(ACTION_SORT, SORT_BY_NAME_DESC);
		} else if (SORT_BY_NAME_DESC.equals(sorter)) {
			nameSortText.setStyleClass("wf_listheaderlink_clicked_descending");
			nameSortLink.getAttributes().put(ACTION_SORT, SORT_BY_NAME);
		} else {
			nameSortText.setStyleClass("wf_listheaderlink");
			nameSortLink.getAttributes().put(ACTION_SORT, SORT_BY_NAME);
		}
		nameSortLink.getChildren().add(nameSortText);
		nameSortLink.setActionListener(WFUtil.createMethodBinding("#{"+WebDAVList.WEB_DAV_LIST_BEAN_ID+".processAction}", new Class[]{ActionEvent.class}));
		nameSortLink.setId(P_ID+"_sortName");
		col.setHeader(nameSortLink);
		HtmlCommandLink nameLink = new HtmlCommandLink();
		nameLink.setId(P_ID);
		nameLink.setStyleClass("wf_listlink");
		nameLink.setValueBinding("value", WFUtil.createValueBinding("#{"+ var + ".name}"));
		WFUtil.addParameterVB(nameLink, PARAMETER_WEB_DAV_URL, var + ".webDavUrl");
		WFUtil.addParameterVB(nameLink, PARAMETER_IS_FOLDER, var + ".isCollection");
		nameLink.setActionListener(WFUtil.createMethodBinding("#{"+WebDAVList.WEB_DAV_LIST_BEAN_ID+".processAction}", new Class[]{ActionEvent.class}));
		col.getChildren().add(nameLink);
		
		UIColumn col2 = new UIColumn();
		col2.setHeader(ContentBlock.getBundle().getLocalizedText("created"));
		HtmlOutputText creation = WFUtil.getTextVB(var + ".creationDate");
		creation.setStyleClass("wf_listtext");
		col2.getChildren().add(creation);
		
		UIColumn col3 = new UIColumn();
		HtmlCommandLink sizeSortLink = new HtmlCommandLink();
		HtmlOutputText sizeSortText = ContentBlock.getBundle().getLocalizedText("size");
		if (SORT_BY_SIZE.equals(sorter)) {
			sizeSortText.setStyleClass("wf_listheaderlink_clicked");
			sizeSortLink.getAttributes().put(ACTION_SORT, SORT_BY_SIZE_DESC);
		} else if (SORT_BY_SIZE_DESC.equals(sorter)) {
			sizeSortText.setStyleClass("wf_listheaderlink_clicked_descending");
			sizeSortLink.getAttributes().put(ACTION_SORT, SORT_BY_SIZE);
		} else {
			sizeSortText.setStyleClass("wf_listheaderlink");
			sizeSortLink.getAttributes().put(ACTION_SORT, SORT_BY_SIZE_DESC);
		}
		sizeSortLink.getChildren().add(sizeSortText);
		sizeSortLink.setActionListener(WFUtil.createMethodBinding("#{"+WebDAVList.WEB_DAV_LIST_BEAN_ID+".processAction}", new Class[]{ActionEvent.class}));
		sizeSortLink.setId(P_ID+"_sortSize");
		col3.setHeader(sizeSortLink);
		HtmlOutputText size = WFUtil.getTextVB(var + ".length");
		size.setStyleClass("wf_listtext");
		col3.getChildren().add(size);
		
		UIColumn col4 = new UIColumn();
		col4.setHeader(ContentBlock.getBundle().getLocalizedText("mime_type"));
		HtmlOutputText mime = WFUtil.getTextVB(var + ".mime");
		mime.setStyleClass("wf_listtext");
		col4.getChildren().add(mime);
		
		UIColumn col5 = new UIColumn();
		col5.setHeader(ContentBlock.getBundle().getLocalizedText("version"));
		HtmlOutputText version = WFUtil.getTextVB(var + ".version");
		version.setStyleClass("wf_listtext");
		col5.getChildren().add(version);
		
		HtmlGraphicImage lock = new HtmlGraphicImage();
		lock.setValueBinding("rendered", WFUtil.createValueBinding("#{"+var+".isLocked}"));
		lock.setUrl(IWMainApplication.getDefaultIWMainApplication().getURIFromURL(WFUtil.getContentBundle().getResourcesVirtualPath())+"/images/locked.gif");
		lock.setId(P_ID+"_lock");
		lock.setHeight(imageSize);// sizes that make sense 16/32/64/128
		
		UIColumn col6 = new UIColumn();
		col6.setHeader(ContentBlock.getBundle().getLocalizedText("lock"));
		col6.getChildren().add(lock);
		
		UIColumn col7 = new UIColumn();
		col7.setHeader(ContentBlock.getBundle().getLocalizedText("checked_out"));
		HtmlOutputText checkedOut = WFUtil.getTextVB(var + ".comment");
		checkedOut.setValueBinding("rendered", WFUtil.createValueBinding("#{"+var+".checkedOut}"));
		checkedOut.setStyleClass("wf_listtext");
		col7.getChildren().add(checkedOut);
		
		UIColumn col8 = new UIColumn();
		HtmlCommandLink modSortLink = new HtmlCommandLink();
		HtmlOutputText modSortText = ContentBlock.getBundle().getLocalizedText("last_modified");
		if (SORT_BY_MODIFICATION_DATE.equals(sorter)) {
			modSortText.setStyleClass("wf_listheaderlink_clicked");
			modSortLink.getAttributes().put(ACTION_SORT, SORT_BY_MODIFICATION_DATE_DESC);
		} else if (SORT_BY_MODIFICATION_DATE_DESC.equals(sorter)) {
			modSortText.setStyleClass("wf_listheaderlink_clicked_descending");
			modSortLink.getAttributes().put(ACTION_SORT, SORT_BY_MODIFICATION_DATE);
		} else {
			modSortText.setStyleClass("wf_listheaderlink");
			modSortLink.getAttributes().put(ACTION_SORT, SORT_BY_MODIFICATION_DATE_DESC);
		}
		modSortLink.getChildren().add(modSortText);
		modSortLink.setActionListener(WFUtil.createMethodBinding("#{"+WebDAVList.WEB_DAV_LIST_BEAN_ID+".processAction}", new Class[]{ActionEvent.class}));
		modSortLink.setId(P_ID+"_sortMod");
		col8.setHeader(modSortLink);
		HtmlOutputText modifiedDate = WFUtil.getTextVB(var + ".modifiedDate");
		modifiedDate.setStyleClass("wf_listtext");
		col8.getChildren().add(modifiedDate);
		
		UIColumn del = new UIColumn();
		del.setHeader(ContentBlock.getBundle().getLocalizedText("delete"));
		HtmlCommandLink delLink = new HtmlCommandLink();
		delLink.setValueBinding("rendered", WFUtil.createValueBinding("#{"+var+".isReal}"));
		delLink.getAttributes().put(ContentViewer.PARAMETER_ACTION, ContentViewer.DELETE);
		WFUtil.addParameterVB(delLink, ContentViewer.PATH_TO_DELETE, var+".webDavUrl");
		delLink.setActionListener(WFUtil.createMethodBinding("#{contentviewerbean.processAction}", new Class[]{ActionEvent.class}));
		delLink.setId(P_ID+"_delLink");
		HtmlGraphicImage delete = new HtmlGraphicImage();
		delete.setUrl(IWMainApplication.getDefaultIWMainApplication().getURIFromURL(WFUtil.getContentBundle().getResourcesVirtualPath())+"/images/delete.gif");
		delete.setId(P_ID+"_delete");
		delete.setHeight(imageSize);// sizes that make sense 16/32/64/128
		delLink.getChildren().add(delete);
		
		del.getChildren().add(delLink);
		
		
		return new UIColumn[] { col0, col, col3, col5, col6 , col7, col8, del};
	}
	
	/* (non-Javadoc)
	 * @see com.idega.webface.bean.WFListBean#updateDataModel(java.lang.Integer, java.lang.Integer)
	 */
	/**
	 * Updates the datamodel, definded by WFList
	 * @param first Number of first element
	 * @param rows Total number of rows
	 */
	public void updateDataModel(Integer start, Integer rows) {
		if (dataModel == null) {
			dataModel = new WFDataModel();
		}
		
//		this.startPage = start.intValue();
//		this.rows = rows.intValue();
		
		WebDAVBean[] beans = getDavData();
		
		int availableRows = beans.length;
		
		int nrOfRows = rows.intValue();
		if (nrOfRows == 0) {
			nrOfRows = availableRows;
		}
		int maxRow = Math.min(start.intValue() + nrOfRows,availableRows);
		for (int i = start.intValue(); i < maxRow; i++) {
			dataModel.set(beans[i], i);
		}
		
		dataModel.setRowCount(availableRows);
	}
	
	
	private WebDAVBean[] getDavData() {
		WebDAVBean[] data;
		try {

			
			IWUserContext iwuc = IWContext.getInstance();			
			IWSlideSession ss = (IWSlideSession) IBOLookup.getSessionInstance(iwuc, IWSlideSession.class);
			if (startPath != null && startPath.equals("/")) {
				startPath = "";
			}
			if (rootPath != null && rootPath.equals("/")) {
				rootPath = "";
			}
			
			
			if (startPath != null) {
				webDAVPath = startPath;
				startPath = null;
			} else if(webDAVPath == null){
				webDAVPath = "";
			}
			
			if (rootPath != null && webDAVPath.indexOf(rootPath) == -1) {
				webDAVPath = rootPath;
			}
			
			if (ss.getExistence(webDAVPath)) {
				data = getDirectoryListing(ss.getWebdavResource(webDAVPath), ss.getWebdavServerURI());
			} else {
				data = new WebDAVBean[] { new WebDAVBean("Resource does not exist") };
			}
		} catch (HttpException ex) {
			System.out.println("[HTTPException]:"+ex.getMessage());
			System.out.println("[HTTPException]:"+ex.getReason());
			System.out.println("[HTTPException]:"+ex.getReasonCode());
			ex.printStackTrace();
			data = new WebDAVBean[] { new WebDAVBean("Caught HttpException") };
		} catch (IOException ex) {
			ex.printStackTrace();
			data = new WebDAVBean[] { new WebDAVBean("Caught IOException") };
		} catch (NullPointerException ex) {
			StackTraceElement[] trace = ex.getStackTrace();
			String traceString = null;
			for (int i = 0; i < trace.length; i++) {
				traceString = traceString + trace[i].toString() + "    \n\r";
			}
			data = new WebDAVBean[] { new WebDAVBean("Nullpointer: " + traceString) };
		}
		return data;
	}
	
	private WebDAVBean[] getDirectoryListing(WebdavExtendedResource headResource, String webDAVServletURL)	throws IOException, HttpException {
		WebdavResources resources = headResource.listWithDeltaV();//headResource.getChildResources();
		Enumeration enumer = resources.getResources();
		List v = new Vector();
		WebDAVBean bean;
		WebDAVBean upBean = null;
		WebdavExtendedResource resource;
		String url;
		if (webDAVPath != null && !"".equals(webDAVPath) && !webDAVPath.equals(rootPath)) {
			upBean = new WebDAVBean();
			upBean.setIconTheme(iconTheme);
			int lastIndex = webDAVPath.lastIndexOf("/");
			String upTo = ContentBlock.getBundle().getLocalizedString("up_to_parent_folder");
			if (lastIndex > 0) {
				String dotdot = webDAVPath.substring(0, lastIndex);
				int lastIndex2 = dotdot.lastIndexOf("/");
				if (lastIndex2 > -1) {
					
					upBean.setName(upTo+" ("+dotdot.substring(lastIndex2+1)+")");
				} else {
					upBean.setName(upTo+" ("+dotdot+")");
				}
				upBean.setWebDavHttpURL(dotdot);
			} else {
				upBean.setName(upTo);
				upBean.setWebDavHttpURL("");
			}
			upBean.setIsReal(false);
			upBean.setIsCollection(true);
		}
		
		while (enumer.hasMoreElements()) {
			resource = (WebdavExtendedResource) enumer.nextElement();
			if (!resource.getDisplayName().startsWith(".")) {
				bean = new WebDAVBean(resource);
				url = resource.getPath();
				url = url.replaceFirst(webDAVServletURL, "");
				bean.setWebDavHttpURL(url);
				bean.setIconTheme(iconTheme);
				v.add(bean);
			}
		}
		
		sortResources(v);
		if (upBean != null) {
			v.add(0,upBean);
		}

		return (WebDAVBean[]) v.toArray(new WebDAVBean[]{});
	}
	
	private void sortResources(List v) {
		int sortMethod = 1;
		boolean desc = false;
		
		if (SORT_BY_NAME.equals(sorter)) {
			sortMethod = WebDAVBeanComparator.SORT_BY_NAME;
			desc = false;
		} else if (SORT_BY_NAME_DESC.equals(sorter)) {
			sortMethod = WebDAVBeanComparator.SORT_BY_NAME;
			desc = true;
		} else if (SORT_BY_SIZE.equals(sorter)) {
			sortMethod = WebDAVBeanComparator.SORT_BY_SIZE;
			desc = false;
		} else if (SORT_BY_SIZE_DESC.equals(sorter)) {
			sortMethod = WebDAVBeanComparator.SORT_BY_SIZE;
			desc = true;
		} else if (SORT_BY_MODIFICATION_DATE.equals(sorter)) {
			sortMethod = WebDAVBeanComparator.SORT_BY_MODIFICATION_DATE;
			desc = false;
		} else if (SORT_BY_MODIFICATION_DATE_DESC.equals(sorter)) {
			sortMethod = WebDAVBeanComparator.SORT_BY_MODIFICATION_DATE;
			desc = true;
		}
		
		Collections.sort(v, new WebDAVBeanComparator(IWContext.getInstance().getCurrentLocale(), sortMethod, desc));
	}
	
	/* (non-Javadoc)
	 * @see com.idega.webface.bean.WFListBean#getDataModel()
	 */
	private WFDataModel dataModel = new WFDataModel();
	
	public DataModel getDataModel() {
		return dataModel;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.webface.bean.WFListBean#setDataModel(javax.faces.model.DataModel)
	 */
	public void setDataModel(DataModel model) {
		this.dataModel = (WFDataModel) model;
	}

}
