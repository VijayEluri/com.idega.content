package com.idega.content.presentation;

import java.io.IOException;
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
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.DataModel;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.WebdavResources;
import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideSession;
import com.idega.webface.WFList;
import com.idega.webface.WFUtil;
import com.idega.webface.bean.WFListBean;
import com.idega.webface.model.WFDataModel;
import documentmanagementprototype2.WebDAVBean;

/**
 * A managed bean for the WebDAVList component
 * @author gimmi
 */
public class WebDAVListManagedBean implements WFListBean, ActionListener {
	
	private static final String P_ID = "wb_list";
	private static final String PARAMETER_WEB_DAV_URL = "wdurl";
	private static final String PARAMETER_IS_FOLDER = "isf";
	
	private String clickedFilePath;
	
	private String webDAVPath = "";
	private ActionListener actionListener;
	
	private int startPage = -1;
	private int rows = -1;

	public WebDAVListManagedBean() {
	}
	
	private WFDataModel dataModel = new WFDataModel();

	public DataModel getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(DataModel model) {
		this.dataModel = (WFDataModel) model;
	}

	public UIColumn[] createColumns(String var) {
				
		UIColumn col0 = new UIColumn();
		HtmlGraphicImage icon = new HtmlGraphicImage();
		icon.setValueBinding("url", WFUtil.createValueBinding("#{"+var+".iconURL}"));
		icon.setHeight("32");// sizes that make sense 16/32/64/128
		HtmlCommandLink iconLink = new HtmlCommandLink();
		iconLink.setId(P_ID+"_L");
		WFUtil.addParameterVB(iconLink, PARAMETER_WEB_DAV_URL, var + ".webDavUrl");
		WFUtil.addParameterVB(iconLink, PARAMETER_IS_FOLDER, var + ".isCollection");
		iconLink.setActionListener(WFUtil.createMethodBinding("#{"+WebDAVList.WEB_DAV_LIST_BEAN_ID+".processAction}", new Class[]{ActionEvent.class}));
		iconLink.getChildren().add(icon);
		col0.getChildren().add(iconLink);
		
		UIColumn col = new UIColumn();
		HtmlCommandLink nameLink = new HtmlCommandLink();
		nameLink.setId(P_ID);
		nameLink.setValueBinding("value", WFUtil.createValueBinding("#{"+ var + ".name}"));
		WFUtil.addParameterVB(nameLink, PARAMETER_WEB_DAV_URL, var + ".webDavUrl");
		WFUtil.addParameterVB(nameLink, PARAMETER_IS_FOLDER, var + ".isCollection");
		nameLink.setActionListener(WFUtil.createMethodBinding("#{"+WebDAVList.WEB_DAV_LIST_BEAN_ID+".processAction}", new Class[]{ActionEvent.class}));
		col.setHeader(WFUtil.getText("Name"));
		col.getChildren().add(nameLink);
		
		UIColumn col2 = new UIColumn();
		col2.setHeader(WFUtil.getText("Created"));
		col2.getChildren().add(WFUtil.getTextVB(var + ".creationDate"));
		
		UIColumn col3 = new UIColumn();
		col3.setHeader(WFUtil.getText("Length"));
		col3.getChildren().add(WFUtil.getTextVB(var + ".length"));
		
		UIColumn col4 = new UIColumn();
		col4.setHeader(WFUtil.getText("Mime type"));
		col4.getChildren().add(WFUtil.getTextVB(var + ".mime"));
		
		UIColumn col5 = new UIColumn();
		col5.setHeader(WFUtil.getText("Folder"));
		col5.getChildren().add(WFUtil.getTextVB(var + ".isCollection"));
		
		UIColumn col6 = new UIColumn();
		col6.setHeader(WFUtil.getText("Last modified"));
		col6.getChildren().add(WFUtil.getTextVB(var + ".modifiedDate"));


		return new UIColumn[] { col0, col, col2, col3, col4, col5, col6 };
	}

	/**
	 * Updates the datamodel, definded by WFList
	 * @param first Number of first element
	 * @param rows Total number of rows
	 */
	public void updateDataModel(Integer start, Integer rows) {
		if (dataModel == null) {
			dataModel = new WFDataModel();
		}
		
		this.startPage = start.intValue();
		this.rows = rows.intValue();
		
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
			if(webDAVPath == null){ 
				webDAVPath = "";
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

	private WebDAVBean[] getDirectoryListing(WebdavResource headResource, String webDAVServletURL)	throws IOException, HttpException {
		WebdavResources resources = headResource.getChildResources();
		Enumeration enumer = resources.getResources();
		Vector v = new Vector();
		WebDAVBean bean;
		WebdavResource resource;
		String url;
		if (webDAVPath != null && !"".equals(webDAVPath)) {
			bean = new WebDAVBean();
			int lastIndex = webDAVPath.lastIndexOf("/");
			if (lastIndex > 0) {
				String dotdot = webDAVPath.substring(0, lastIndex);
				bean.setName("Up to "+dotdot);
				bean.setWebDavHttpURL(dotdot);
			} else {
				bean.setName("Up to /");
				bean.setWebDavHttpURL("");
			}
			bean.setIsCollection(true);
			v.add(bean);
		}
		
		while (enumer.hasMoreElements()) {
			resource = (WebdavResource) enumer.nextElement();
			if (!resource.getDisplayName().startsWith(".")) {
				bean = new WebDAVBean(resource);
				url = resource.getPath();
				url = url.replaceFirst(webDAVServletURL, "");
				bean.setWebDavHttpURL(url);
//				bean.setParentList(this);
				System.out.println("[WebDAVManagerBean] " +url);
				v.add(bean);
			}
		}
		return (WebDAVBean[]) v.toArray(new WebDAVBean[]{});
	}
	
	public void setActionListener(ActionListener listener) {
		System.out.println("Adding actionListener tom WebDAVListManagedBean");
		this.actionListener = listener;
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

	public void refresh() {
		updateDataModel(new Integer(startPage), new Integer(rows));
	}
	
	public void processAction(ActionEvent actionEvent) throws AbortProcessingException {
		UIComponent comp = actionEvent.getComponent();
		
		boolean isFolder = true;
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
		
		if (parent != null) {
			WFList parentList = (WFList) parent;
			if (isFolder) {
				this.setClickedFilePath(null);
				this.updateDataModel(new Integer(parentList.getFirst()), new Integer(parentList.getRows()));
			} else {
				this.setClickedFilePath(webDAVPath);
				int index = webDAVPath.lastIndexOf("/");
				if (index > -1) {
					webDAVPath = webDAVPath.substring(0, index);
				}
			}
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

}
