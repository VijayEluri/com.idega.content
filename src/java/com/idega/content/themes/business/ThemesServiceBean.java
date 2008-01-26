package com.idega.content.themes.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.FinderException;

import org.apache.slide.event.ContentEvent;

import com.idega.business.IBOServiceBean;
import com.idega.business.SpringBeanLookup;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentItemChecker;
import com.idega.content.themes.helpers.bean.Theme;
import com.idega.content.themes.helpers.business.ThemesConstants;
import com.idega.content.themes.helpers.business.ThemesHelper;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.builder.business.BuilderServiceFactory;
import com.idega.core.builder.data.ICDomain;
import com.idega.core.builder.data.ICPage;
import com.idega.core.builder.data.ICPageHome;
import com.idega.idegaweb.IWApplicationContextFactory;
import com.idega.presentation.IWContext;
import com.idega.servlet.filter.IWWelcomeFilter;
import com.idega.slide.business.IWContentEvent;
import com.idega.slide.business.IWSlideChangeListener;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.StringHandler;

public class ThemesServiceBean extends IBOServiceBean implements ThemesService, IWSlideChangeListener{

	private static final long serialVersionUID = -1765120426660957585L;
	
	private volatile BuilderService builder = null;

	public void onSlideChange(IWContentEvent idegaWebContentEvent) {
		String uri = idegaWebContentEvent.getContentEvent().getUri();
		if (uri.indexOf(ThemesConstants.THEMES_PATH) == -1) {	// If not processing theme
			return;
		}
		if (ContentEvent.REMOVE.equals(idegaWebContentEvent.getMethod())) {
			if (ThemesHelper.getInstance(false).isCorrectFile(uri, ThemesConstants.THEME_SKELETONS_FILTER)) {
				List<Theme> themes = ThemesHelper.getInstance(false).getAvailableThemes();
				if (themes == null) {
					return;
				}
				boolean foundTheme = false;
				Theme theme = null;
				for (int i = 0; (i < themes.size() && !foundTheme); i++) {
					theme = themes.get(i);
					if (uri.equals(ThemesHelper.getInstance(false).decodeUrl(theme.getLinkToSkeleton()))) {
						foundTheme = true;
					}
				}
				if (foundTheme && !theme.isLocked()) {
					ThemesHelper.getInstance(false).removeLastUsedTheme(String.valueOf(theme.getIBPageID()));
					int pageId = theme.getIBPageID();
					
					String themeID = theme.getId();
					ThemesHelper.getInstance(false).removeTheme(uri, themeID);
					
					deleteIBPage(pageId);
				}
			}
		}
		else {
			if (!ThemesHelper.getInstance().isCreatedManually(uri) && ThemesHelper.getInstance().isCorrectFile(uri, ThemesConstants.THEME_SKELETONS_FILTER) && isNewTheme(uri)) {
				try {
					ThemesHelper.getInstance().getThemesLoader().loadTheme(uri, ThemesHelper.getInstance().urlEncode(uri), true, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean deleteIBPage(int pageId) {
		if (pageId < 0) {
			return false;
		}
		
		return deletePage(String.valueOf(pageId), false, true, false);
	}
	
	public boolean deleteIBPage(String pageID, boolean deleteChildren) {
		return deletePage(pageID, deleteChildren, false, true);
	}
	
	@SuppressWarnings("unchecked")
	private boolean deletePage(String pageKey, boolean deleteChildren, boolean canUseDefaultUser, boolean clearCache) {
		if (pageKey == null) {
			return false;
		}

		IWContext iwc = CoreUtil.getIWContext();
		
		getBuilderService();
		
		Map tree = null;
		ICDomain domain = null;
		int userId = 1;
		
		if (iwc == null && canUseDefaultUser) {
			try {
				userId = Integer.valueOf(getAccessController().getAdministratorUser().getId());	//	Using default user
			} catch(Exception e) {
				e.printStackTrace();
			}
			try {
				tree = builder.getTree(getIWApplicationContext());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			userId = iwc.getCurrentUserId();
			tree = builder.getTree(iwc);
			domain = iwc.getDomain();
			
			if (pageKey.equals(ThemesHelper.getInstance().getLastVisitedPage())) {
				ThemesHelper.getInstance().setLastVisitedPage(null);
			}
		}
		
		deleteArticlesInPagesBeingDeleted(pageKey, deleteChildren);
		boolean result = builder.deletePage(pageKey, deleteChildren, tree, userId, domain);
		
		if (domain != null) {
			try {
				if (Integer.valueOf(pageKey).intValue() == domain.getStartPageID()) {
					domain.setIBPage(null);
					domain.store();
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			
			if (clearCache) {
				builder.clearAllCachedPages();
			}
		}
		
		IWWelcomeFilter.reInitializeCachedDomainOnNextRequest();
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private boolean deleteArticlesInPagesBeingDeleted(String parentPageKey, boolean deleteChildren) {
		boolean result = deleteArticlesInThisPage(parentPageKey);
		
		if (deleteChildren) {
			ICPage parentPage = getICPage(parentPageKey);
			
			if (parentPage != null) {
				Collection children = parentPage.getChildren();
				if (children != null) {
					Object o = null;
					for (Iterator it = children.iterator(); it.hasNext();) {
						o = it.next();
						if (o instanceof ICPage) {
							result = deleteArticlesInPagesBeingDeleted(((ICPage) o).getId(), deleteChildren);
						}
					}
				}
			}
		}
	
		return result;
	}
	
	private boolean deleteArticlesInThisPage(String pageKey) {
		if (pageKey == null) {
			return false;
		}
		
		BuilderService builder = getBuilderService();
		if (builder == null) {
			return false;
		}
		
		Class<?> articleClass = CoreConstants.getArticleItemViewerClass();
		if (articleClass == null) {
			return false;
		}
		
		List<String> ids = builder.getModuleId(pageKey, articleClass.getName());
		if (ids == null) {
			return true;
		}
		
		List<String> paths = new ArrayList<String>();
		String path = null;
		for (int i = 0; i < ids.size(); i++) {
			path = builder.getProperty(pageKey, ids.get(i), CoreConstants.ARTICLE_RESOURCE_PATH_PROPERTY_NAME);
			if (path != null) {
				paths.add(path);
			}
		}
		
		if (paths.size() == 0) {
			return true;
		}
		
		ContentItemChecker checker = SpringBeanLookup.getInstance().getSpringBean(getIWApplicationContext(), ContentItemChecker.class);
		if (checker == null) {
			return false;
		}
		return checker.deleteDummyArticles(paths);
	}
	
	public boolean createIBPage(Theme theme) {
		if (theme == null) {
			return false;
		}
		
		if (theme.getIBPageID() != -1) {
			return true;	//	IBPage (template) for this theme already exists
		}
		
		int id = -1;
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return false;
		}
		int domainID = -1;
		ICDomain domain = iwc.getDomain();
		if (domain == null) {
			return false;
		}
		domainID = domain.getID();
		
		getBuilderService();
		
		//	Creating IBPage (template) for theme
		String parentId = builder.getTopLevelTemplateId(builder.getTopLevelTemplates(iwc));
		if (parentId == null || ThemesConstants.MINUS_ONE.equals(parentId)) {
			//	No Top Level Template
			parentId = ThemesHelper.getInstance().getThemesEngine().createRootTemplate(domain, builder, domainID, builder.getIBXMLFormat());
			ThemesHelper.getInstance().getThemesEngine().initializeCachedDomain(ThemesConstants.DEFAULT_DOMAIN_NAME, domain);
		}
		String name = StringHandler.removeCharacters(theme.getName(), ContentConstants.SPACE, ContentConstants.UNDER);
		id = createIBPage(parentId, theme.getName(), builder.getTemplateKey(), null, ThemesConstants.THEMES + name +
				ContentConstants.SLASH, null, domainID, builder.getHTMLTemplateKey(), null);
		if (id == -1) {
			return false;
		}
		theme.setIBPageID(id);
		
		return updatePageWebDav(theme.getIBPageID(), CoreConstants.WEBDAV_SERVLET_URI + theme.getLinkToSkeleton());
	}
	
	public boolean updatePageWebDav(int id, String uri) {
		return updatePageWebDav(id, uri, true);
	}
	
	public boolean updatePageWebDav(int id, String uri, boolean clearCache) {
		ICPage page = getICPage(id);
		if (page == null) {
			return false;
		}
		
		page.setWebDavUri(uri);
		page.store();
		
		if (clearCache) {
			getBuilderService().clearAllCachedPages();
		}
		return true;
	}
	
	public int createIBPage(String parentId, String name, String type, String templateId, String pageUri, String subType, int domainId, String format, String sourceMarkup){
		return createIBPage(parentId, name, type, templateId, pageUri, subType, domainId, format, sourceMarkup, null);
	}
	
	@SuppressWarnings("unchecked")
	public int createIBPage(String parentId, String name, String type, String templateId, String pageUri, String subType, int domainId, String format, String sourceMarkup, String treeOrder) {
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return -1;
		}
		
		getBuilderService();
		
		Map tree = builder.getTree(iwc);
		if (tree == null) {
			return -1;
		}
		
		if (builder.getPageKey().equals(type)) {
			if (templateId == null) {
				templateId = ThemesHelper.getInstance().getLastUsedTheme();
			}
		}
		
		if (parentId == null && domainId == -1) { // Creating top level page
			ICDomain domain = null;
			domain = iwc.getDomain();
			if (domain != null) {
				domainId = domain.getID();
			}
		}
		
		return builder.createNewPage(parentId, name, type, templateId, pageUri, tree, iwc, subType, domainId, format, sourceMarkup, treeOrder);
	}
	
	private boolean isNewTheme(String uri) {
		if (ThemesHelper.getInstance().existTheme(uri)) {
			return false;
		}
		return true;
	}
	
	public ICPageHome getICPageHome() throws RemoteException {
		ICPageHome sHome = (ICPageHome) getIDOHome(ICPage.class);
		return sHome;
	}
	
	public BuilderService getBuilderService() {
		if (builder == null) {
			synchronized (ThemesServiceBean.class) {
				if (builder == null) {
					try {
						builder = BuilderServiceFactory.getBuilderService(getIWApplicationContext());
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return builder;
	}
	
	public ICPage getICPage(String pageKey) {
		if (pageKey == null) {
			return null;
		}
		int id = -1;
		try {
			id = Integer.valueOf(pageKey);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
		return getICPage(id);
	}
	
	public ICPage getICPage(int id) {
		ICPage page = null;
		try {
			page = getICPageHome().findByPrimaryKey(id);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		} catch (FinderException e) {
			e.printStackTrace();
			return null;
		}
		return page;
	}
	
	public ICDomain getDomain() {
		/*ICDomainHome domainHome = null;
		try {
			domainHome = (ICDomainHome) IDOLookup.getHome(ICDomain.class);
		} catch (IDOLookupException e) {
			log.error(e);
			return null;
		}
		try {
			return domainHome.findFirstDomain();
		} catch (FinderException e) {
			log.error(e);
			return null;
		}*/
		return IWApplicationContextFactory.getCurrentIWApplicationContext().getDomain();
	}
}