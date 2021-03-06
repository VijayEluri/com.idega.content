if (ThemesSliderHelper == null) var ThemesSliderHelper = {};

ThemesSliderHelper.THEME_ID = null;
ThemesSliderHelper.GET_THEMES_ID = 0;
ThemesSliderHelper.SLIDER_SHOWED_FIRST_TIME = true;
ThemesSliderHelper.SCROLLER_IMAGE_WIDTH = 23;
ThemesSliderHelper.FRAME_CHANGE = 158;

function setThemeForStyle(ID) {
	ThemesSliderHelper.THEME_ID = ID;
}

function getThemeForStyle() {
	return ThemesSliderHelper.THEME_ID;
}

function getScrollerImageWidth() {
	return ThemesSliderHelper.SCROLLER_IMAGE_WIDTH;
}

var SLIDER_IS_IN_MOTION = false;
function showSlider(container) {	
	ThemesSliderHelper.resizeSlider();
	
	container = $(container);
	container.setStyle('position', 'absolute');
	container.setStyle('bottom', '5px');
	container.setStyle('right', '5px');
	
	var showSliderEffect = new Fx.Style(container, 'opacity', {duration: 1000, onComplete: function() {
		setDisplayPropertyToElement(container.id, 'block');
		getThemesSlider();
		SLIDER_IS_IN_MOTION = false;
	}});
	showSliderEffect.start(0, 1);
}

function getThemesSlider() {
	var slideToDefault = ThemesSliderHelper.SLIDER_SHOWED_FIRST_TIME;
	ThemesSliderHelper.SLIDER_SHOWED_FIRST_TIME = false;
	getThemes(null, true, slideToDefault);
	
	if (ThemesSliderHelper.GET_THEMES_ID != null) {
		window.clearTimeout(ThemesSliderHelper.GET_THEMES_ID);
		ThemesSliderHelper.GET_THEMES_ID = null;
	}
}

function manageSlider(buttonID) {
	if (SLIDER_IS_IN_MOTION) {
		return false;
	}
	
	var container = $('themesSliderContainer');
	if (container == null) {
		return;
	}
	
	showLoadingMessage(LOADING_TEXT);
	
	var button = buttonID == null ? null : $(buttonID);
	
	SLIDER_IS_IN_MOTION = true;
	if (container.getStyle('display') == 'none') {
		if (button != null) {
			button.addClass('active');
		}
		
		showSlider(container);
	}
	else {
		if (button != null) {
			button.removeClass('active');
		}
		
		hideThemesSliderInPages(container);
	}
}

function hideThemesSliderInPages(container) {
	if (container == null) {
		return false;
	}
	
	if ($(container).getStyle('display') != 'none') {
		removeStyleOptions(null);
		var hideSlider = new Fx.Style(container, 'opacity', {duration: 500, onComplete: function() {
			SLIDER_IS_IN_MOTION = false;
		}});
		hideSlider.start(1, 0);
		window.setTimeout("setDisplayPropertyToElement('"+container.id+"', 'none', "+ThemesSliderHelper.FRAME_CHANGE+")", 500);
	}
}

function setDisplayPropertyToElement(id, property) {
	if (id == null || property == null) {
		return false;
	}
	var element = $(id);
	if (element == null) {
		return false;;
	}
	
	element.setStyle('display', property);
}

function chooseStyle(themeID) {
	if (themeID == null) {
		return;
	}
	var theme = getTheme(themeID);
	if (theme != null) {
		theme.applyStyle = true;
		applyThemeForPage(themeID);
	}
}

function recallStyle(themeID) {
	if (themeID == null) {
		return;
	}
	var theme = getTheme(themeID);
	if (theme != null) {
		theme.applyStyle = false;
	}
}

function applyThemeForPage(themeID) {
	if (themeID == null) {
		return;
	}
	var theme = getTheme(themeID);
	if (theme != null) {
		if (theme.applyStyle) {
			theme.applyStyle = false;
			chooseOption(themeID);
		}
	}
}

function chooseOption(themeID) {
	var leftPosition = (getAbsoluteLeft(themeID + '_container') + 3);
	if (getTotalWidth() - (leftPosition + getImageWidth()) < 0) {
		return; // There is not enough space
	}
	
	setThemeForStyle(themeID);
	
	var div = $('chooseStyleLayer');
	var pageSpan = null;
	var siteSpan = null;
	var pageAndChildrenSpan = null;
	if (div == null) {
		div = new Element('div');
		div.setStyle('opacity', '0');
		div.setProperty('id', 'chooseStyleLayer');
		div.addClass('themeChooseStyle');
		
		var container = new Element('div');
		container.addClass('themesButtonContainer');
		
		var left = new Element('div');
		left.addClass('left');
		container.appendChild(left);
		
		var right = new Element('div');
		right.addClass('right');
		container.appendChild(right);
		
		var divp = new Element('div');
		divp.addClass('themeChooseStyleText');
		divp.addClass('applyPage');
		divp.setProperty('title', getStyleForCurrentPage());
		divp.setProperty('alt', getStyleForCurrentPage());
		pageSpan = new Element('span');
		pageSpan.setProperty('id', 'pageStyle');
		divp.appendChild(pageSpan);
	
		var divs = new Element('div');
		divs.addClass('themeChooseStyleText');
		divs.addClass('applySite');
		divs.setProperty('title', getStyleForSite());
		divs.setProperty('alt', getStyleForSite());
		siteSpan = new Element('span');
		siteSpan.setProperty('id', 'siteStyle');
		divs.appendChild(siteSpan);
		
		var divd = new Element('div');
		divd.addClass('themeChooseStyleText');
		divd.addClass('applyPageAndChildren');
		divd.setProperty('title', getStyleForPageAndChildren());
		divd.setProperty('alt', getStyleForPageAndChildren());
		pageAndChildrenSpan = new Element('span');
		pageAndChildrenSpan.setProperty('id', 'pageAndChildrenStyle');
		divd.appendChild(pageAndChildrenSpan);
		
		var themeChildrenTemplatesContainer = new Element('div');
		themeChildrenTemplatesContainer.setProperty('id', 'themeTemplateChildrenContainer');
		themeChildrenTemplatesContainer.addClass('themeTemplateChildrenContainerAsStackStyle');
		document.body.appendChild(themeChildrenTemplatesContainer);
		
		right.appendChild(divp);
		right.appendChild(divd);
		right.appendChild(divs);
		
		div.appendChild(container);
		document.body.appendChild(div);
		
		var setStyleForPageFunction = function() {
			TEMPLATE_ID = null;
			setTemplateForPageOrPages(true, 0);
		};
		var setStyleForPageAndChildren = function() {
			TEMPLATE_ID = null;
			setTemplateForPageOrPages(true, 1);
		}
		var setStyleForSiteFunction = function() {
			TEMPLATE_ID = null;
			setTemplateForPageOrPages(false, 2);
		};
		
		pageSpan.addEvent('click', setStyleForPageFunction);
		siteSpan.addEvent('click', setStyleForSiteFunction);
		pageAndChildrenSpan.addEvent('click', setStyleForPageAndChildren);
		div.addEvent('click', function(e) {
			e = new Event(e);
			removeStyleOptions(e);
		});
	}
	
	var topPosition = getAbsoluteTop(themeID + '_container') - 37;
	
	$('themeTemplateChildrenContainer').setStyles({
		opacity: '0',
		left: leftPosition + 'px'
	});
	$('themeTemplateChildrenContainer').setProperty('themeid', themeID);
	$('themeTemplateChildrenContainer').setProperty('initialtopposition', topPosition);
	
	var theme = getTheme(themeID);
	if (theme != null && theme.children != null && theme.children.length > 0) {
		getChildTemplatesForThisTheme();
	}
	
	div.setStyle('left', leftPosition + 'px');
	var showSelectStyle = new Fx.Style(div, 'opacity', {duration: 250});
	showSelectStyle.start(0, 1);
}

function getChildTemplatesForThisTheme() {
	var stackContainer = $('themeTemplateChildrenContainer');
	if (stackContainer == null) {
		return false;
	}
	
	var theme = getTheme(stackContainer.getProperty('themeid'));
	if (theme == null) {
		return false;
	}
	
	stackContainer.empty();
	
	var listInStackContainer = new Element('ul');
	listInStackContainer.addClass('templatesListInStackContainer');
	listInStackContainer.injectInside(stackContainer);
	
	var allChildren = theme.children;
	var templateId = null;
	for (var i = 0; i < allChildren.length; i++) {
		templateId = allChildren[i].id;
		
		var childTemplateContainer = new Element('li');
		childTemplateContainer.addClass('themeChildInStackContainerStyle');
		if (i == 0) {
			childTemplateContainer.addClass('firstChild');
		}
		else if (i + 1 == allChildren.length) {
			childTemplateContainer.addClass('lastChild');
		}
		var span = new Element('span');
		span.appendText(allChildren[i].name);
		span.injectInside(childTemplateContainer);
		
		var container = new Element('div');
		container.addClass('container');
		container.injectInside(childTemplateContainer);
		
		var applyStyleToPageLink = new Element('a');
		applyStyleToPageLink.setProperty('href', 'javascript:void(0)');
		applyStyleToPageLink.setProperty('templateid', templateId);
		applyStyleToPageLink.addClass('applyPage');
		applyStyleToPageLink.setProperty('title', getStyleForCurrentPage());
		applyStyleToPageLink.addEvent('click', function() {
			TEMPLATE_ID = $(this).getProperty('templateid');
			setTemplateForPageOrPages(true, 0);
		});
		applyStyleToPageLink.injectInside(container);
		
		var applyStyleToPageAndChildrenLink = new Element('a');
		applyStyleToPageAndChildrenLink.setProperty('href', 'javascript:void(0)');
		applyStyleToPageAndChildrenLink.setProperty('templateid', templateId);
		applyStyleToPageAndChildrenLink.addClass('applyPageAndChildren');
		applyStyleToPageAndChildrenLink.setProperty('title', getStyleForPageAndChildren());
		applyStyleToPageAndChildrenLink.addEvent('click', function() {
			TEMPLATE_ID = $(this).getProperty('templateid');
			setTemplateForPageOrPages(true, 1);
		});
		applyStyleToPageAndChildrenLink.injectInside(container);
		
		var applyStyleToSiteLink = new Element('a');
		applyStyleToSiteLink.setProperty('href', 'javascript:void(0)');
		applyStyleToSiteLink.setProperty('templateid', templateId);
		applyStyleToSiteLink.addClass('applySite');
		applyStyleToSiteLink.setProperty('title', getStyleForSite());
		applyStyleToSiteLink.addEvent('click', function() {
			TEMPLATE_ID = $(this).getProperty('templateid');
			setTemplateForPageOrPages(false, 2);
		});
		applyStyleToSiteLink.injectInside(container);
		
		childTemplateContainer.injectInside(listInStackContainer);
	}
	
	var initialTopPosition = stackContainer.getProperty('initialtopposition');
	var currentSize = stackContainer.getSize().size.y;
	
	var showStackContainer = new Fx.Style(stackContainer, 'opacity', {duration: 250});
	showStackContainer.start(0, 1);
}

function setTemplateForPageOrPages(isPage, type) {
	removeStyleOptions(null);
	if (getThemeForStyle() == null) {
		//	No theme selected
		return false;
	}

	LucidEngine.getPageIdByUri(window.location.pathname, {
		callback: function(id) {
			setTemplateForPageOrPagesWithPageId(id, type);
		}
	});
}

function setTemplateForPageOrPagesWithPageId(pageId, type) {
	if (type > 0) {
		var confirmed = window.confirm(ARE_YOU_SURE_YOU_WANT_APPLY_THIS_TEMPLATE);
		if (!confirmed) {
			return false;
		}
	}
	
	showLoadingMessage(getApplyingStyleText());
	ThemesEngine.setSelectedStyle(getThemeForStyle(), type > 1 ? null : pageId, type, TEMPLATE_ID, {
		callback: function(result) {
			closeAllLoadingMessages();
			
			//	Error
			if (!result) {
				if (!USER_IS_CONTENT_EDITOR) {
					alert(INSUFFICIENT_RIGHTS_FOR_ACTION_IN_LUCID);
				}
				return false;
			}
			
			//	OK
			setNewStyleToElements('usedThemeName', 'themeName');
			setNewStyleForSelectedElement(getThemeForStyle() + '_themeNameContainer', 'themeName usedThemeName');
			
			WORKING_WITH_TEMPLATE = false;
			reloadPage();
		}
	});
}

ThemesSliderHelper.resizeSlider = function() {
	var themesTicker = $('themesTickerContainer');
	var container = $('themesSliderContainer');
	if (themesTicker == null || container == null) {
		return;
	}

	var available = getTotalWidth() - 14;
	if (available > 0) {
		container.setStyle('width', available + 'px');
		themesTicker.setStyle('width', (available - 50) + 'px');
	}
}