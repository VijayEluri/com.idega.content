var PAGE_ID = null;

var TOTAL_WIDTH = 0;
var TOTAL_HEIGHT = 0;

var IS_SITE_MAP = false;
var NEED_RELOAD_BUILDER_PAGE = false;

var SITE_INFO_KEYWORD_FROM_BOX = null;
var APPLICATION_PROPERTY = "application_property";
var OLD_APPLICATION_PROPERTY = null;
var EDIT_BOX_ID = "changeSiteInfoBox";

function isSiteMap() {
	return IS_SITE_MAP;
}

function setIsSiteMap(isSiteMap) {
	IS_SITE_MAP = isSiteMap;
}

function isChangingSiteMap() {
	if (isSiteMap()) {
		return false;
	}
	else {
		getPageInfoValues();
	}
}

function getPageID() {
	return PAGE_ID;
}

function setPageID(ID) {
	PAGE_ID = ID;
	ThemesEngine.setPageId(ID, nothingToDo);
}

function nothingToDo(parameter) {
}

function getGlobalPageId() {
	if (getPageID() == null) {
		ThemesEngine.getPageId(setGlobalPageId);
	}
	else {
		return getPageID();
	}
}

function setGlobalPageId(ID) {
	setPageID(ID);
	getPrewUrl(ID);
}

function changePageTitleCallback(result) {
	if (result == null) {
		return;
	}
	var pageUri = document.getElementById("pageUri");
	if (pageUri != null) {
		pageUri.value = result;
	}
	if (!isSiteMap()) {
		if (getPageID() != null) {
			if (getPageID() != -1) {
				setTimeout("getPrewUrl('"+getPageID()+"')", 1000);
			}
		}
	}
}

function changePageTitleInPageInfo(title) {
	if (title == null) {
		return;
	}
	var element = document.getElementById("pageTitle");
	if (element == null) {
		return;
	}
	element.value = title;
}

function getTotalWidth() {
	if (TOTAL_WIDTH != 0) {
		return TOTAL_WIDTH;
	}
	if(typeof(window.innerWidth) == "number") {
		TOTAL_WIDTH = window.innerWidth; // Non-IE
	} else if(document.documentElement && document.documentElement.clientWidth) {
		TOTAL_WIDTH = document.documentElement.clientWidth; // IE 6+ in 'standards compliant mode'
	} else if(document.body && document.body.clientWidth) {
		TOTAL_WIDTH = document.body.clientWidth; // IE 4 compatible
	}
	return TOTAL_WIDTH;
}

function getTotalHeight() {
	if (TOTAL_HEIGHT != 0) {
		return TOTAL_HEIGHT;
	}
	if(typeof(window.innerHeight) == "number") {
		TOTAL_HEIGHT = window.innerHeight; // Non-IE
	} else if(document.documentElement && document.documentElement.clientHeight) {
		TOTAL_HEIGHT = document.documentElement.clientHeight; // IE 6+ in 'standards compliant mode'
	} else if(document.body && document.body.clientHeight) {
		TOTAL_HEIGHT = document.body.clientHeight; // IE 4 compatible
	}
	return TOTAL_HEIGHT;
}

function getRealContainerByStyle(containerID, styleClass) {
	var container = document.getElementById(containerID);
	if (container == null) {
		return;
	}
	var children = container.childNodes;
	if (children == null) {
		return;
	}
	var realContainer = null;
	var found = false;
	for (var i = 0; (i < children.length && !found); i++) {
		realContainer = children[i];
		if (realContainer != null) {
			if (realContainer.className == styleClass) {
				found = true;
			}
		}
	}
	return realContainer;
}

function resizeContainer(containerID, styleClass, usedSpace, changeHeight) {
	var realContainer = getRealContainerByStyle(containerID, styleClass);
	if (realContainer != null) {
		if (changeHeight) {
			realContainer.style.height = (getTotalHeight() - usedSpace) + "px";
		}
		else {
			realContainer.style.width = (getTotalWidth() - usedSpace) + "px";
		} 
	}
}

function checkIfNotEmptySiteTree(id) {
	if (id == null) {
		return;
	}
	var treeContainer = document.getElementById(id);
	if (treeContainer == null) {
		return;
	}
	if (treeContainer.childNodes != null) {
		if (treeContainer.childNodes.length != 0) {
			return;
		}
	}
	// No pages created
	var button = document.getElementById("makeStartPage");
	if (button != null) {
		button.disabled = true;
		button.value = "No page exist";
	}
	
	var rootUl = document.createElement('ul');
	rootUl.setAttribute('id','rootUl');
	var tempTable = document.createElement('table');
	tempTable.setAttribute('id','temporaryTable');
	tempTable.setAttribute('onmouseover','treeObj.prepareToSetTopPage();');	
	tempTable.setAttribute('onmouseout','treeObj.topPageNotSet();');	
	tempTable.style.border='1px  solid';
	tempTable.style.margin='5px';
	var tr=document.createElement('tr');
  	var td=document.createElement('td');
  	var tdText=document.createTextNode('Drop templates here'); 
  	td.appendChild(tdText);  					// - put the text node in the table cell
  	tr.appendChild(td); 						// - put the cell into the row
  	tempTable.appendChild(tr); 	
  	rootUl.appendChild(tempTable);
	treeContainer.appendChild(rootUl);
}

function setNeedRelaodBuilderPage(needReload) {
	NEED_RELOAD_BUILDER_PAGE = needReload;
}

function isNeedRelaodBuilderPage() {
	return NEED_RELOAD_BUILDER_PAGE;
}

function redirectAction(uri, timeOut) {
	if (uri != null) {
		setTimeout("redirectFromSiteMap('"+uri+"')", timeOut);
	}
}

function redirectFromSiteMap(uri) {
	if (uri == null) {
		return;
	}
	var frame = null;
	
	var allFrames = null;
	var parentWindow = window.parent;
	if (parentWindow != null) {
		allFrames = parentWindow.document.getElementsByTagName("iframe");
	}
	else {
		allFrames = document.getElementsByTagName("iframe");
	}
	
	if (allFrames != null) {
		frame = allFrames[0];
	}
	
	if (frame == null) {
		if (parentWindow != null) {
			parentWindow.location.href = uri;
		}
		else {
			closeLoadingMessage();
			return;
		}
	}
	
	frame.src = uri;
}

function isEnterEvent(event) {
	if (event == null) {
		return false;
	}
	var keyCode = event.keyCode ? event.keyCode : event.which ? event.which : event.charCode;
	if (keyCode == 13) {
		return true;
	}
	return false;
}

function savePageInfoWithEnter(event) {
	if (isEnterEvent(event)) {
		savePageInfo();
	}
}

function saveSiteInfoWithEnter(event) {
	if (isEnterEvent(event)) { 
		saveSiteInfo();
	}
}

function applyThemeForSite(themeId) {
	setThemeForStyle(themeId);
	setStyle(false);
}

function insertStyleFile() {
	var style = document.createElement("link");
	style.setAttribute("type","text/css");
	style.setAttribute("href", "/idegaweb/bundles/com.idega.content.bundle/resources/style/themes_manager.css");
	style.setAttribute("rel","stylesheet");
	document.getElementsByTagName("head")[0].appendChild(style); 
}

function changeSiteInfoValue(id) {
	if (id == null) {
		return;
	}
	document.onclick = showSiteInfoValue;
	showSiteInfoValue();
	SITE_INFO_KEYWORD_FROM_BOX = id;
	
	var element = document.getElementById(id);
	if (element == null) {
		return;
	}
	
	var editBox = document.getElementById(EDIT_BOX_ID);
	if (editBox == null) {
		editBox = document.createElement("input");
		editBox.setAttribute("type", "input");
		editBox.setAttribute("id", EDIT_BOX_ID);
		if (typeof element.attachEvent != "undefined") {
        	editBox.attachEvent("onkeypress", function(e){saveSiteInfoValue(e, this.value);});
		} else {
        	editBox.addEventListener("keypress", function(e){saveSiteInfoValue(e, this.value);}, true);
		}
	}
	else {
		editBox.value = "";
		editBox.style.display = "inline";
		var parentNode = editBox.parentNode;
		if (parentNode != null) {
			parentNode.removeChild(editBox);
		}
	}
	
	if (element.getAttribute(APPLICATION_PROPERTY) == null) {
		element.setAttribute(APPLICATION_PROPERTY, true);
	}
	element.style.visibility = "hidden";
	appendEditBoxToExactPlace(element, editBox);
	
	editBox.focus();
}

function appendEditBoxToExactPlace(element, edit) {
	if (element == null || edit == null) {
		return;
	}
	var parentNode = element.parentNode;
	if (parentNode != null) {
		parentNode.insertBefore(edit, element);
	}
	else {
		element.appendChild(edit);
	}
}

function saveSiteInfoValue(event, value) {
	if (event == null) {
		return;
	}
	if (!isEnterEvent(event)) {
		return;
	}
	if (SITE_INFO_KEYWORD_FROM_BOX == null || value == null) {
		return;
	}
	
	var element = document.getElementById(SITE_INFO_KEYWORD_FROM_BOX);
	if (element != null) {
		if (element.value != null) {
			element.value = value;
		}
		else {
			var children = element.childNodes;
			if (children != null) {
				for (var j = 0; j < children.length; j++) {
					element.removeChild(children[j]);
				}				
			}
			element.appendChild(document.createTextNode(value));
		}
	}
	
	showLoadingMessage("Saving...");
	ThemesEngine.saveSiteInfoValue(SITE_INFO_KEYWORD_FROM_BOX, value, saveSiteInfoValueCallback);
}

function saveSiteInfoValueCallback(result) {
	showSiteInfoValue();
	closeLoadingMessage();
}

function showSiteInfoValue() {
	var editBox = document.getElementById(EDIT_BOX_ID);
	if (editBox != null) {
		var container = editBox.parentNode;
		if (container != null) {
			container.removeChild(editBox);
			var children = container.childNodes;
			if (children != null) {
				var notVisible = null;
				for (var i = 0; i < children.length; i++) { // Looking for element that is hidden, need to be visible
					notVisible = children[i];
					if (notVisible != null) {
						if (notVisible.style != null) {
							if (notVisible.style.visibility != null) {
								if (notVisible.style.visibility == "hidden") {
									if (notVisible.getAttribute(APPLICATION_PROPERTY) != null) {
										notVisible.style.visibility = "visible";
									}
								}
							}
						}
					}
				}
			}
		}
		else {
			editBox.style.display = "none";
		}
	}
	if (SITE_INFO_KEYWORD_FROM_BOX == null) {
		return;
	}
	var element = document.getElementById(SITE_INFO_KEYWORD_FROM_BOX);
	if (element != null) {
		element.style.visibility = "visible";
	}
}
	