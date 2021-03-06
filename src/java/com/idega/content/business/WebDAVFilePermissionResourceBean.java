/*
 * $Id: WebDAVFilePermissionResourceBean.java,v 1.7 2006/04/09 12:01:55 laddi Exp $
 * Created on 30.12.2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.content.business;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.httpclient.HttpException;
import com.idega.business.IBOLookupException;
import com.idega.business.IBOSessionBean;
import com.idega.content.data.ACEBean;
import com.idega.core.accesscontrol.data.ICRole;
import com.idega.slide.authentication.AuthenticationBusiness;
import com.idega.slide.business.IWSlideSession;
import com.idega.slide.util.AccessControlEntry;
import com.idega.slide.util.AccessControlList;
import com.idega.slide.util.IWSlideConstants;


/**
 * 
 *  Last modified: $Date: 2006/04/09 12:01:55 $ by $Author: laddi $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.7 $
 */
public class WebDAVFilePermissionResourceBean extends IBOSessionBean implements WebDAVFilePermissionResource{

	private AccessControlList currentList = null;
	private Collection allACEBeans = null;
	private Collection aceBeansForGroups = null;
	private Collection aceBeansForRoles = null;
	private Collection aceBeansForUsers = null;
	private Collection aceBeansForStandard = null;
	private Collection aceBeansForOthers = null;
	
	private Comparator aceBeanComparator = null;
	
	private final static int BEANTYPE_ALL = -1;
	private final static int BEANTYPE_STANDARD = 0;
	private final static int BEANTYPE_ROLES = 1;
	private final static int BEANTYPE_GROUPS = 2;
	private final static int BEANTYPE_USERS = 3;
	/**
	 * 
	 */
	public WebDAVFilePermissionResourceBean() {
		super();
	}
	
	
	
	public void clear(){
		this.currentList = null;
		this.allACEBeans = null;
		this.aceBeansForGroups = null;
		this.aceBeansForRoles = null;
		this.aceBeansForUsers = null;
		this.aceBeansForStandard = null;
		this.aceBeansForOthers = null;
	}
	
	/**
	 * @param path
	 */
	protected void updateACEValues() {
		updateACEValues(this.allACEBeans);	
		updateACEValues(this.aceBeansForGroups);
		updateACEValues(this.aceBeansForRoles);
		updateACEValues(this.aceBeansForUsers);
		updateACEValues(this.aceBeansForStandard);
		updateACEValues(this.aceBeansForOthers);
	}
	
	
	
	/**
	 * @param aceBeanCollection
	 */
	protected void updateACEValues(Collection aceBeanCollection) {
		if(aceBeanCollection!=null){
			for (Iterator iter = aceBeanCollection.iterator(); iter.hasNext();) {
				ACEBean bean = (ACEBean) iter.next();
				bean.updateACEs();
			}
		}
	}



	protected AccessControlList getACL(String path) throws HttpException, RemoteException, IOException{
		if(this.currentList==null){
			IWSlideSession slideSession = (IWSlideSession)getSessionInstance(IWSlideSession.class);
			this.currentList = slideSession.getAccessControlList(path);
		}
		checkPath(path);
		return this.currentList;
	}
	
	protected void checkPath(String path){
		if(this.currentList!=null){ 
			String currentListPath = this.currentList.getResourcePath();
			if(currentListPath==null && path==null){
				return;
			}
			if(( ((currentListPath==null || path==null)&&currentListPath!=path) || !((currentListPath!=null) && currentListPath.equals(path)) )){
				throw new ConcurrentModificationException("Asking for ACL for path '"+path+"' while current ACL is for '"+this.currentList.getResourcePath()+"'. The #clear() method needs to be invoked first.");
			}
		}
	}
	
	public Collection getAllAces(String path) throws HttpException, RemoteException, IOException{		
		if(this.allACEBeans == null){
			AccessControlList acl = getACL(path);
			List l = acl.getAccessControlEntries();
			if(l!=null){
				this.allACEBeans = getACEBeans(l,path,BEANTYPE_ALL);
			}
		}
		checkPath(path);
		return this.allACEBeans;
	}
	
	
	public Collection getRoleAces(String path) throws HttpException, RemoteException, IOException{
		if(this.aceBeansForRoles == null){
			AccessControlList acl = getACL(path);
			List l = acl.getAccessControlEntriesForRoles();
			this.aceBeansForRoles = getACEBeans(l,path,BEANTYPE_ROLES);
		}
		checkPath(path);
		return this.aceBeansForRoles;
	}
	
	
	public Collection getStandardAces(String path) throws HttpException, RemoteException, IOException {
		if(this.aceBeansForStandard == null){
			AccessControlList acl = getACL(path);
			List l = acl.getAccessControlEntriesForStandardPrincipals();
			Collection c = new ArrayList();
			c.addAll(getACEBeans(l,path,BEANTYPE_STANDARD));
			
//			List l2 = acl.getAccessControlEntriesForOthers();
//			c.addAll(getACEBeans(l2,pathBEANTYPE_OTHERS));
			
			this.aceBeansForStandard = c;
			
		}
		checkPath(path);
		return this.aceBeansForStandard;
	}


	public Collection getGroupAces(String path) throws HttpException, RemoteException, IOException {
		if(this.aceBeansForGroups == null){
			AccessControlList acl = getACL(path);
			List l = acl.getAccessControlEntriesForGroups();
			this.aceBeansForGroups = getACEBeans(l,path,BEANTYPE_GROUPS);
		}
		checkPath(path);
		return this.aceBeansForGroups;
	}


	public Collection getUserAces(String path) throws HttpException, RemoteException, IOException {
		if(this.aceBeansForUsers == null){
			AccessControlList acl = getACL(path);
			List l = acl.getAccessControlEntriesForUsers();
			this.aceBeansForUsers = getACEBeans(l,path,BEANTYPE_USERS);
		}
		checkPath(path);
		return this.aceBeansForUsers;
	}
	
	
	/**
	 * @param l List of <code>AccessControlEntry</code>s
	 */
	protected Collection getACEBeans(List l,String path,int beantype) {
		Map m = new HashMap();
		if(l!=null){
			for (Iterator iter = l.iterator(); iter.hasNext();) {
				AccessControlEntry ace = (AccessControlEntry) iter.next();
				ACEBean bean = (ACEBean) m.get(ace.getPrincipal());
				if(bean == null){
					bean = new ACEBean(ace,getUserContext().getCurrentLocale());
					m.put(ace.getPrincipal(),bean);
				} else {
					if(ace.isNegative()){
						bean.setNegativeAccessControlEntry(ace);
					} else {
						bean.setPositiveAccessControlEntry(ace);
					}
				}
			}
		}
		
		Collection beanCollection = new TreeSet(getACEBeanComparator());
		beanCollection.addAll(m.values());
		
		//add missing AccesscontrolEntry to ACEBeans
		for (Iterator iter = beanCollection.iterator(); iter.hasNext();) {
			ACEBean aBean = (ACEBean) iter.next();
			if(!aBean.hasNegativeAccessControlEntry()){
				AccessControlEntry theOtherAce = aBean.getPositiveAccessControlEntry();
				AccessControlEntry thisAce = new AccessControlEntry(theOtherAce.getPrincipal(),true,false,false,null,theOtherAce.getPrincipalType());
				this.currentList.add(thisAce);
				aBean.setNegativeAccessControlEntry(thisAce);
			} else if(!aBean.hasPositiveAccessControlEntry()){
				AccessControlEntry theOtherAce = aBean.getNegativeAccessControlEntry();
				AccessControlEntry thisAce = new AccessControlEntry(theOtherAce.getPrincipal(),false,false,false,null,theOtherAce.getPrincipalType());
				this.currentList.add(thisAce);
				aBean.setPositiveAccessControlEntry(thisAce);
			}
		}
		

		//fill with missing aces
		switch (beantype) {
			case BEANTYPE_STANDARD:
//				try {
//					IWSlideSession slideSession = (IWSlideSession)getSessionInstance(IWSlideSession.class);
					List allStandardAces = IWSlideConstants.ALL_STANDARD_SUBJECT_URIS;
					for (Iterator iter = allStandardAces.iterator(); iter.hasNext();) {
						String principal = (String)iter.next();
						if(!m.containsKey(principal) && !principal.equals(IWSlideConstants.SUBJECT_URI_OWNER) && !principal.equals(IWSlideConstants.SUBJECT_URI_SELF)){
							addACEBeanSkippingPathCheck(principal,AccessControlEntry.PRINCIPAL_TYPE_STANDARD,beanCollection);
						}
					}
//				}
//				catch (IBOLookupException e) {
//					e.printStackTrace();
//				}
//				catch (RemoteException e) {
//					e.printStackTrace();
//				}
				break;
			case BEANTYPE_ROLES:
				try {
					AuthenticationBusiness authBean = (AuthenticationBusiness)getServiceInstance(AuthenticationBusiness.class);
					String rootRoleURI = authBean.getRoleURI(IWSlideConstants.ROLENAME_ROOT);
					//Remove root role
					if(m.containsKey(rootRoleURI)){
						for (Iterator iter = beanCollection.iterator(); iter.hasNext();) {
							ACEBean aceBean = (ACEBean) iter.next();
							if(rootRoleURI.equals(aceBean.getPrincipalName())){
								iter.remove();
							}
						}
					}
					
					//add all missing roles
					Collection roles = getIWApplicationContext().getIWMainApplication().getAccessController().getAllRoles();
					for (Iterator iter = roles.iterator(); iter.hasNext();) {
						ICRole role = (ICRole) iter.next();
						String roleURI = authBean.getRoleURI(role.getRoleKey());
						if(!m.containsKey(roleURI)){
							addACEBeanSkippingPathCheck(roleURI,AccessControlEntry.PRINCIPAL_TYPE_ROLE,beanCollection);
						}
					}
					
				}
				catch (IBOLookupException e) {
					e.printStackTrace();
				}
				catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
		
		return beanCollection;
	}
	
	/**
	 * @return
	 */
	private Comparator getACEBeanComparator() {
		if(this.aceBeanComparator == null){
			this.aceBeanComparator = new ACEBeanComparator(getUserContext().getCurrentLocale());
		}
		return this.aceBeanComparator;
	}



	/**
	 * @param principal
	 * @param principal
	 * @param principalType
	 * @return
	 */
	public ACEBean getAdditionalACEBean(String path, String principal, int principalType) {
		checkPath(path);
		Collection l = null;
		switch (principalType) {
			case AccessControlEntry.PRINCIPAL_TYPE_STANDARD:
				l = this.aceBeansForStandard;
				break;
			case AccessControlEntry.PRINCIPAL_TYPE_ROLE:
				l = this.aceBeansForStandard;
				break;
			case AccessControlEntry.PRINCIPAL_TYPE_GROUP:
				l = this.aceBeansForGroups;
				break;
			case AccessControlEntry.PRINCIPAL_TYPE_USER:
				l = this.aceBeansForUsers;
				break;
			case AccessControlEntry.PRINCIPAL_TYPE_OTHER:
				l = this.allACEBeans;
				break;
			default:
//				l = aceBeansForAll;
				break;
		}
		ACEBean aBean = addACEBeanSkippingPathCheck(principal, principalType,l);
		return aBean;
	}



	/**
	 * @param principal
	 * @param principalType
	 * @return
	 */
	private ACEBean addACEBeanSkippingPathCheck(String principal, int principalType,Collection aceBeanList) {
		AccessControlEntry negativeAce = new AccessControlEntry(principal,true,false,false,null,principalType);
		this.currentList.add(negativeAce);
		AccessControlEntry positiveAce = new AccessControlEntry(principal,false,false,false,null,principalType);
		this.currentList.add(positiveAce);
		ACEBean aBean = new ACEBean(positiveAce,negativeAce,getUserContext().getCurrentLocale());
		if(aceBeanList!=null){
			aceBeanList.add(aBean);
		} else {
			System.err.println("[WARNING]["+this.getClass().getName()+"]: current aceBeanList has not been constructed before attempt to add to it.");
		}
		return aBean;
	}



	public void store(String path) throws HttpException, RemoteException, IOException{
		checkPath(path);
		updateACEValues();
		
		AccessControlList acl = getACL(path);
		IWSlideSession slideSession = (IWSlideSession)getSessionInstance(IWSlideSession.class);
		boolean successful = slideSession.storeAccessControlList(acl);
		System.out.println("Save ACL for resource '"+acl.getResourcePath()+((successful)?"' was successful":"' failed"));
		clear();
	}
	
	
}
