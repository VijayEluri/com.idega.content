/*
 * $Id: ContentItemFieldBean.java,v 1.1 2005/02/07 10:59:41 gummi Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.content.bean;

import java.io.Serializable;

/**
 * Bean for idegaWeb content item fields.   
 * <p>
 * Last modified: $Date: 2005/02/07 10:59:41 $ by $Author: gummi $
 *
 * @author Anders Lindman
 * @version $Revision: 1.1 $
 */

public class ContentItemFieldBean implements Serializable, ContentItemField {
	
	private int _contentItemFieldId = 0;
	private int _versionId = 0;
	private String _key = null;
	private Object _value = null;
	private byte[] _binaryValue = null;
	private int _orderNo = 0;
	private String _fieldType = null;
	
	private String _name = null; // Transient, not stored in database 

	/**
	 * Default constructor.
	 */
	public ContentItemFieldBean() {}
	
	/**
	 * Constructs a new content item field bean with the specified parameters. 
	 */
	public ContentItemFieldBean(
			int contentItemFieldId,
			int versionId,
			String key,
			Object value,
			int orderNo,
			String fieldType) {
		_contentItemFieldId = contentItemFieldId;
		_versionId = versionId;
		_key = key;
		if("create_date".equals(key)){
			System.out.println(this.toString());
			System.out.println(key+": "+value);
		}
		_value = value;
		_orderNo = orderNo;
		_fieldType = fieldType;
	}
		
	public int getContentItemFieldId() { return _contentItemFieldId; }
	public int getVersionId() { return _versionId; }
	public String getKey() { return _key; }
	public Object getValue() {
		return _value; 
	}
	public byte[] getBinaryValue() { return _binaryValue; }
	public int getOrderNo() { return _orderNo; }
	public String getOrderNoString() { return String.valueOf(_orderNo); }
	public String getFieldType() { return _fieldType; }

	public void setContentItemFieldId(int id) { _contentItemFieldId = id; } 
	public void setVersionId(int id) { _versionId = id; }
	public void setKey(String s) { _key = s; }
	public void setValue(Object obj) {
		_value = obj; 
	}
	public void setBinaryValue(byte[] binaryValue) { _binaryValue = binaryValue; _fieldType = FIELD_TYPE_BINARY; } 
	public void setOrderNo(int n) { _orderNo = n; }
	public void setFieldType(String s) { _fieldType = s; }
	
	public String getImageURI() { return "showimg.jsp?image_number=" + _orderNo; }
	public String getName() { return _name; }
	public void setName(String s) { _name = s; } 
}
