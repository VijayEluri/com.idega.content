package com.idega.content.themes.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;

public class ThemeChanger {
	
	private static final Log log = LogFactory.getLog(ThemeChanger.class);
	
	// These are defaults in RapidWeaver, we are using its to generate good preview
	private static final String TOOLBAR_REPLACE_BEGIN = "<ul><li><a href=\"index.html\" rel=\"self\" id=\"current\">";
	private static final String TOOLBAR_REPLACE_END = "</a></li></ul>";
	
	private static final String SIDEBAR_REPLACE_BEGIN = "<div id=\"blog-categories\">";
	private static final String SIDEBAR_REPLACE_ELEMENT_BEGIN = "<div class=\"blog-category-link-disabled\">";
	private static final String SIDEBAR_REPLACE_ELEMENT_END = "</div>";
	
	private static final String BREADCRUMB_REPLACE_BEGIN = "<ul><li><a href=\"index.html\">";
	private static final String BREADCRUMB_REPLACE_END = "</a></li></ul>";
	
	private static final String IMAGE_START = "<img src=";
	private static final String IMAGE_POSITION = "style=\"margin: 2px; float: ";
	private static final String IMAGE_END = " />";
	private static final String CONTENT_PARAGRAPH_TITLE = "<div class=\"blog-entry\"><div class=\"blog-entry-title\">";
	private static final String CONTENT_PARAGRAPH_DATE = "</div><div class=\"blog-entry-date\">";
	private static final String CONTENT_PARAGRAPH_LINK = "<span class=\"blog-entry-permalink\"> | <a>Permalink</a></span></div><div class=\"blog-entry-body\">";
	private static final String CONTENT_PARAGRAPH_START = "<p style=\"font-family: Verdana,Arial,Helvetica,sans-serif\">";
	private static final String CONTENT_PARAGRAPH_END = "</p></div></div>";
	private static final String CONTENT_PARAGRAPH = "Article";
	private static final String[] PARAGRAPHS = new String[] {"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Vestibulum bibendum, ligula ut feugiat rutrum, mauris libero ultricies nulla, at hendrerit lectus dui bibendum metus. Phasellus quis nulla nec mauris sollicitudin ornare. Vivamus faucibus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos hymenaeos. Cras vulputate condimentum ipsum. Duis urna eros, commodo id, sagittis sed, sodales eu, ante. Etiam ante. Cras risus dolor, porta nec, adipiscing eu, scelerisque at, metus. Mauris nunc eros, porttitor nec, tincidunt ut, rutrum eget, massa. In facilisis nisi. Sed non lorem malesuada quam egestas bibendum. Quisque bibendum ullamcorper purus. Integer id diam vel elit adipiscing consectetuer. Phasellus vitae eros. Vivamus laoreet consectetuer tortor. In congue dignissim quam. Suspendisse nec purus vel velit ultricies bibendum."};
	private static final String CONTENT_BEGIN = "<div class=\"contentSpacer\"></div>";
	private static final String CONTENT_END = "<div class=\"clear\"></div>\n<div class=\"clearer\"></div>";
	
	// Default keywords
	private static final String TOOLBAR = "toolbar";
	private static final String SIDEBAR = "sidebar";
	private static final String BREADCRUMB = "breadcrumb";
	private static final String FOOTER = "footer";
	private static final String SITE_TITLE = "site_title";
	private static final String SITE_SLOGAN = "site_slogan";
	private static final String CONTENT = "content";
	
	// These are defaults in RapidWeaver style files, we need to change directories to images
	private static final String CSS_IMAGE_URL = "url(";
	private static final String DIRECTORY = "../";
	private static final String IMAGES = "images";
	private static final String CUSTOM_CSS_REPLACE = CSS_IMAGE_URL + DIRECTORY + DIRECTORY + IMAGES;
	private static final String[] CUSTOM_REPLACE = new String[] {CUSTOM_CSS_REPLACE};

	// Incorrect CSS syntax needs to be replaced
	private static final String[] HREF_REPLACE = new String[] {"href^=", "href$="};
	private static final String HREF_REPLACEMENT = "href~=";
	private static final String[] IMAGE_URL_REPLACE = new String[] {"url(images"};
	
	private static final String HTML_HEAD = "head";
	private static final String HTML_BODY = "body";
	
	// Element's attributes and values
	private static final String TAG_ATTRIBUTE_REL = "rel";
	private static final String TAG_ATTRIBUTE_MEDIA = "media";
	private static final String TAG_ATTRIBUTE_VALUE_STYLESHEET = "stylesheet";
	private static final String TAG_ATTRIBUTE_VALUE_CSS = "text/css";
	private static final String TAG_ATTRIBUTE_VALUE_SCREEN = "screen";
	
	private static final String COPY_AND_SPACE = "&copy;&nbsp;";
	private static final String NEW_LINE = "\n";
	private static final String COMMENT_BEGIN = "/*";
	private static final String COMMENT_END = "*/";
	private static final String OPENER = "{";
	private static final String CLOSER = "}";
	
	private static final int THEME_HEIGHT = 200;
	
	private static final String REGION_TO_EXPAND = "contentContainer";
	
	private ThemesHelper helper = ThemesHelper.getInstance();
	private Namespace namespace = Namespace.getNamespace(ThemesConstants.NAMESPACE);
	private XMLOutputter out = null;
	
	private int openers = 0;
	private int closers = 0;
	
	public ThemeChanger() {
		out = new XMLOutputter();
		out.setFormat(Format.getPrettyFormat());
	}
	
	/**
	 * Prepares importing theme for usage (removes needless content, adds regions, extracts properties)
	 * @param theme
	 * @return boolean
	 */
	public boolean prepareThemeForUsage(Theme theme) {
		if (!theme.isNewTheme()) {
			return true; // Theme allready prepared
		}
		
		String skeleton = theme.getLinkToSkeleton();
		if (skeleton.indexOf(ThemesConstants.SPACE) != -1) {
			skeleton = helper.urlEncode(skeleton);
		}
		Document doc = helper.getXMLDocument(helper.getFullWebRoot() + skeleton);
		if (doc == null) {
			return false;
		}
		Element root = doc.getRootElement();
		Element head = root.getChild(HTML_HEAD, namespace);
		
		// Removing needles content (like "%pathto")
		if (!proceedHeadContent(ThemesConstants.CONTENT + theme.getLinkToBase(), head)) {
			return false;
		}
		
		// Adding IBPage regions
		if (!proceedBodyContent(root.getChild(HTML_BODY, namespace))) {
			return false;
		}
		
		// Finding where to insert element
		int index = getElementIndex(head.getChildren(), ThemesConstants.TAG_ATTRIBUTE_TYPE, TAG_ATTRIBUTE_VALUE_CSS);
		// Adding enabled styles
		List <ThemeStyleGroupMember> members = getEnabledStyles(theme);
		for (int i = 0; i < members.size(); i++) {
			head.addContent(index, getNewStyleElement(ThemesConstants.CONTENT + theme.getLinkToBase(), members.get(i)));
			index++;
		}
		
		if (!uploadDocument(doc, theme.getLinkToBaseAsItIs(), helper.getFileNameWithExtension(theme.getLinkToSkeleton()), theme, true)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param theme
	 * @return
	 */
	public boolean prepareThemeStyleFiles(Theme theme) {
		if (!theme.isNewTheme()) {
			return true; // Theme allready prepared
		}
		
		if (!prepareThemeDefaultStyleFiles(theme)) {
			return false;
		}
		
		Map styles = theme.getStyleGroupsMembers();
		if (styles == null) {
			return false;
		}

		Iterator it = styles.values().iterator();
		ThemeStyleGroupMember member = null;
		List <String> files = null;
		int index = theme.getLinkToBase().indexOf(ThemesConstants.THEMES);
		if (index != -1) {
			index++;
		}
		
		// Constructing correct path to images folder
		StringBuffer replacement = new StringBuffer();
		replacement.append(CSS_IMAGE_URL);
		replacement.append(ThemesConstants.CONTENT);
		replacement.append(theme.getLinkToBase());
		replacement.append(IMAGES);
		
		Replaces[] r = new Replaces[]{getReplace(CUSTOM_REPLACE, replacement.toString())};
		
		while (it.hasNext()) {
			member = (ThemeStyleGroupMember) it.next();
			files = member.getStyleFiles();
			for (index = 0; index < files.size(); index++) {
				if (!proceedStyleFile(theme.getLinkToBase() + files.get(index), r)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private Replaces getReplace(String[] whatToReplace, String replacement) {
		if (whatToReplace == null || replacement == null) {
			return null;
		}
		Replaces replace = new Replaces();
		replace.setReplaces(whatToReplace);
		replace.setReplacement(replacement);
		return replace;
	}
	
	private boolean prepareThemeDefaultStyleFiles(Theme theme) {
		List <String> defaultStyles = ThemesConstants.DEFAULT_STYLE_FILES;
		StringBuffer replacement = new StringBuffer();
		replacement.append(CSS_IMAGE_URL).append(ThemesConstants.CONTENT).append(theme.getLinkToBase()).append(IMAGES);
		Replaces[] r = new Replaces[]{getReplace(HREF_REPLACE, HREF_REPLACEMENT), getReplace(IMAGE_URL_REPLACE, replacement.toString())};	
		for (int i = 0; i < defaultStyles.size(); i++) {
			if (!proceedStyleFile(theme.getLinkToBase() + defaultStyles.get(i), r)) {
				return false;
			}
		}
		return true;
	}
	
	private String scanLine(String line) {
		if (line == null) {
			return ThemesConstants.EMPTY;
		}
		
		if (line.indexOf(OPENER) == -1 && line.indexOf(CLOSER) == -1) {
			return line;
		}
		
		if (line.indexOf(OPENER) != -1 && line.indexOf(CLOSER) != -1 && openers == closers) {
			return line;
		}
		
		if (line.indexOf(OPENER) != -1 && line.indexOf(CLOSER) == -1) {
			openers++;
			return line;
		}
		if (line.indexOf(OPENER) == -1 && line.indexOf(CLOSER) != -1) {
			closers++;
			if (closers != openers) {
				line = line.replace(CLOSER, ThemesConstants.EMPTY);
			}
			openers = 0;
			closers = 0;
			return line;
		}
		
		return line;
	}
	
	private boolean proceedStyleFile(String linkToStyle, Replaces[] replaces) {
		if (linkToStyle == null || replaces == null) {
			return false;
		}
		
		// Getting css file
		InputStream is = helper.getInputStream(helper.getFullWebRoot() + linkToStyle);
		if (is == null) {
			return false;
		}
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader buf = new BufferedReader(isr);
		StringBuffer sb = new StringBuffer();
		String line;
		try {
			while ((line = buf.readLine()) != null) {
				if (line.indexOf(COMMENT_BEGIN) == -1 && line.indexOf(COMMENT_END) == -1) {
					line = scanLine(line);
				}
				sb.append(line).append(NEW_LINE);
			}
		} catch (IOException e) {
			log.error(e);
			return false;
		} finally {
			helper.closeInputStream(is);
			helper.closeInputStreamReader(isr);
			helper.closeBufferedReader(buf);
		}
		// Changing content
		String content = sb.toString();
		boolean needToReplace = false;
		String[] whatToReplace = null;
		String replacement = null;
		for (int i = 0; i < replaces.length; i++) {
			whatToReplace = replaces[i].getReplaces();
			replacement = replaces[i].getReplacement();
			if (whatToReplace != null && replacement != null) {
				for (int j = 0; j < whatToReplace.length; j++) {
					while (content.indexOf(whatToReplace[j]) != -1) {
						content = content.replace(whatToReplace[j], replacement);
						needToReplace = true;
					}
				}
			}
		}

		if (!needToReplace) {
			return true;
		}

		// Uploading modified file
		try {
			if (!helper.getSlideService().uploadFileAndCreateFoldersFromStringAsRoot(helper.getLinkToBase(helper.decodeUrl(linkToStyle)), helper.getFileNameWithExtension(linkToStyle), content, null, true)) {
				return false;
			}
		} catch (RemoteException e) {
			log.error(e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param elements
	 * @param attributeValue
	 * @return int
	 */
	private int getElementIndex(List elements, String attributeType, String attributeValue) {
		int index = 0;
		if (elements == null) {
			return index;
		}
		Element e = null;
		String value = null;
		for (int i = 0; i < elements.size(); i++) {
			e = (Element) elements.get(i);
			value = e.getAttributeValue(attributeType);
			if (attributeValue.equals(value)) {
				index = i;
			}
		}
		index++;
		return index;
	}
	
	/**
	 * Uploads document to slide
	 * @param doc
	 * @param linkToBase
	 * @param fileName
	 * @return boolean
	 */
	public boolean uploadDocument(Document doc, String linkToBase, String fileName, Theme theme, boolean isTheme) {
		String docContent = out.outputString(doc);
		
		if (isTheme) {
			docContent = addRegions(docContent);
			docContent = getFixedDocumentContent(docContent);
		}
		theme.setLocked(true);
		try {
			if (!helper.getSlideService().uploadFileAndCreateFoldersFromStringAsRoot(linkToBase, fileName, docContent, null, true)) {
				return false;
			}
		} catch (RemoteException e) {
			log.error(e);
			return false;
		} finally {
			theme.setLocked(false);
		}
		return true;
	}
	
	/**
	 * Checks if document contains needless info if so, replaces with empty String
	 * @param documentContent
	 * @return String
	 */
	private String getFixedDocumentContent(String documentContent) {
		for (int i = 0; i < ThemesConstants.USELESS_CONTENT.size(); i++) {
			while (documentContent.indexOf(ThemesConstants.USELESS_CONTENT.get(i)) != -1) {
				documentContent = documentContent.replace(ThemesConstants.USELESS_CONTENT.get(i), ThemesConstants.EMPTY);
			}
		}
		return documentContent;
	}
	
	/**
	 * RapidWeaver theme consists inproper data for valid XHTML document, so needs to be fixed
	 * @param head
	 * @return boolean
	 */
	private boolean proceedHeadContent(String linkToBase, Element head) {
		if (linkToBase == null || head == null) {
			return false;
		}
		List headElements = head.getContent();
		if (headElements == null) {
			return false;
		}
		
		Object o = null;
		List <Text> textElements = new ArrayList <Text> ();
		List <Element> elementsNeedsRegions = new ArrayList <Element> ();
		Element e = null;
		Attribute a = null;
		
		for (int i = 0; i < headElements.size(); i++) {
			o = headElements.get(i);
			if (o instanceof Element) {
				e = (Element) o;
				if (!needAddRegion(ThemesConstants.REGIONS, e.getTextNormalize())) {
					e.setText(fixValue(e.getTextNormalize()));
				}
				a = e.getAttribute(ThemesConstants.TAG_ATTRIBUTE_HREF);
				if (a == null) {
					a = e.getAttribute(ThemesConstants.TAG_ATTRIBUTE_SRC);
				}
				if (a != null) {
					a.setValue(linkToBase + fixValue(a.getValue())); // Fixing attribute's value
				}
			}
			else {
				if (o instanceof Text) {
					textElements.add((Text) o);
				}
			}
		}
		
		Iterator <Element> ite = elementsNeedsRegions.iterator();
		while (ite.hasNext()) {
			e = ite.next();
			e.addContent(getCommentsCollection(fixValue(e.getTextNormalize())));
		}

		Iterator <Text> itt = textElements.iterator();
		Text t = null;
		while (itt.hasNext()) {
			t = itt.next();
			if (needAddRegion(ThemesConstants.REGIONS, t.getTextNormalize())) {
				head.addContent(getCommentsCollection(fixValue(t.getTextNormalize())));
			}
			t.detach();
		}
		
		return true;
	}
	
	/**
	 * Creates collection of Comment objets (opening and closing comment)
	 * @param commentValue
	 * @return Collection
	 */
	private Collection <Comment> getCommentsCollection(String commentValue) {
		Collection <Comment> c = new ArrayList <Comment> ();
		c.add(new Comment(ThemesConstants.TEMPLATE_REGION_BEGIN + commentValue + ThemesConstants.TEMPLATE_REGION_MIDDLE));
		c.add(new Comment(ThemesConstants.TEMPLATE_REGION_END));
		return c;
	}
	
	private Collection <Element> getElement(String type, String text, String attribute, String attributeValue) {
		Collection <Element> c = new ArrayList <Element> ();
		Element e = new Element(type, namespace);
		e.setText(text);
		e.setAttribute(attribute, attributeValue);
		c.add(e);
		return c;
	}
	
	/**
	 * Checks if need to add a region to tag
	 * @param regions
	 * @param value
	 * @return boolean
	 */
	private boolean needAddRegion(List <String> regions, String value) {
		if (regions == null) {
			return false;
		}
		if (regions.contains(value)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Adding regions to div tags like this: <!-- TemplateBeginEditable name="MyUniqueRegionId1" -->MyUniqueRegionId1<!-- TemplateEndEditable -->
	 * @param body
	 * @return boolean
	 */
	private boolean proceedBodyContent(Element body) {
		if (body == null) {
			return false;
		}
		List nodes = null;
		JDOMXPath xp = null;
		try {
			xp = new JDOMXPath(ThemesConstants.DIV_TAG_INSTRUCTION);
			xp.addNamespace(ThemesConstants.NAMESPACE_ID, ThemesConstants.NAMESPACE);
			nodes = xp.selectNodes(body);
		} catch (JaxenException e) {
			log.error(e);
		}
		if (nodes == null) {
			return false;
		}
		Iterator it = nodes.iterator();
		while (it.hasNext()) {
			addRegion((Element) it.next());
		}
		return true;
	}
	
	private String getBasicReplace(String begin, String defaultValue, String end) {
		String value = ThemesConstants.EMPTY;
		if (begin != null) {
			value += begin;
		}
		if (defaultValue != null) {
			value += defaultValue;
		}
		if (end != null) {
			value += end;
		}
		return value;
	}
	
	private String getSidebarReplace(String defaultValue) {
		if (defaultValue == null) {
			return ThemesConstants.EMPTY;
		}
		String[] elements = defaultValue.split(ThemesConstants.COMMA);
		StringBuffer sidebar = new StringBuffer();
		sidebar.append(SIDEBAR_REPLACE_BEGIN);
		for (int i = 0; i < elements.length; i++) {
			sidebar.append(SIDEBAR_REPLACE_ELEMENT_BEGIN);
			sidebar.append(elements[i]);
			sidebar.append(SIDEBAR_REPLACE_ELEMENT_END);
		}
		sidebar.append(SIDEBAR_REPLACE_ELEMENT_END);
		return sidebar.toString();
	}
	
	private String getContentReplace(String defaultValue) {
		StringBuffer content = new StringBuffer();
		if (defaultValue != null && !ThemesConstants.EMPTY.equals(defaultValue)) {
			content.append(defaultValue).append(NEW_LINE);
		}
		Date d = new Date();
		content.append(CONTENT_BEGIN).append(NEW_LINE);
		for (int i = 0; i < PARAGRAPHS.length; i++) {
			content.append(CONTENT_PARAGRAPH_TITLE).append(CONTENT_PARAGRAPH);
			if (PARAGRAPHS.length > 1) {
				content.append(ThemesConstants.SPACE).append(i + 1);
			}
			content.append(CONTENT_PARAGRAPH_DATE);
			content.append(d);
			content.append(CONTENT_PARAGRAPH_LINK).append(CONTENT_PARAGRAPH_START);
			content.append(IMAGE_START).append(ThemesConstants.SINGLE_QUOTE);
			content.append(ThemesConstants.BASE_THEME_IMAGES);
			content.append(ThemesConstants.THEME_IMAGES.get(helper.getRandomNumber(ThemesConstants.THEME_IMAGES.size())));
			content.append(ThemesConstants.SINGLE_QUOTE).append(ThemesConstants.SPACE).append(IMAGE_POSITION);
			content.append(ThemesConstants.IMAGE_POSITIONS.get(helper.getRandomNumber(ThemesConstants.IMAGE_POSITIONS.size())));
			content.append(ThemesConstants.SINGLE_QUOTE).append(IMAGE_END);
			content.append(PARAGRAPHS[i]).append(CONTENT_PARAGRAPH_END);
		}
		content.append(CONTENT_END).append(NEW_LINE);
		return content.toString();
	}
	
	/**
	 * Creates String, thats represens Builder's region
	 * @param value
	 * @return String
	 */
	private String getRegion(String value) {
		String region = ThemesConstants.COMMENT_BEGIN + ThemesConstants.TEMPLATE_REGION_BEGIN + value +
			ThemesConstants.TEMPLATE_REGION_MIDDLE + ThemesConstants.COMMENT_END;
		IWMainApplicationSettings settings  = IWMainApplication.getDefaultIWMainApplication().getSettings();
		String propertyValue = settings.getProperty(ThemesConstants.THEMES_PROPERTY_START + value + ThemesConstants.THEMES_PROPERTY_END);
		if (propertyValue != null) {
			if (value.equals(TOOLBAR)) {
				return region + getBasicReplace(TOOLBAR_REPLACE_BEGIN, propertyValue, TOOLBAR_REPLACE_END) +
					ThemesConstants.COMMENT_BEGIN +	ThemesConstants.TEMPLATE_REGION_END + ThemesConstants.COMMENT_END;
			}
			if (value.equals(SIDEBAR)) {
				return region + getSidebarReplace(propertyValue) + ThemesConstants.COMMENT_BEGIN +
					ThemesConstants.TEMPLATE_REGION_END + ThemesConstants.COMMENT_END;
			}
			if (value.equals(BREADCRUMB)) {
				return region + getBasicReplace(BREADCRUMB_REPLACE_BEGIN, propertyValue, BREADCRUMB_REPLACE_END) +
					ThemesConstants.COMMENT_BEGIN +	ThemesConstants.TEMPLATE_REGION_END + ThemesConstants.COMMENT_END;
			}
			if (value.equals(FOOTER)) {
				return region + COPY_AND_SPACE + getBasicReplace(null, propertyValue, null) + ThemesConstants.COMMENT_BEGIN +
					ThemesConstants.TEMPLATE_REGION_END + ThemesConstants.COMMENT_END;
			}
			if (value.equals(SITE_TITLE)) {
				return region + getBasicReplace(null, propertyValue, null) + ThemesConstants.COMMENT_BEGIN +
					ThemesConstants.TEMPLATE_REGION_END + ThemesConstants.COMMENT_END;
			}
			if (value.equals(SITE_SLOGAN)) {
				return region + getBasicReplace(null, propertyValue, null) + ThemesConstants.COMMENT_BEGIN +
					ThemesConstants.TEMPLATE_REGION_END + ThemesConstants.COMMENT_END;
			}
			if (value.equals(CONTENT)) {
				return region + getContentReplace(propertyValue) + ThemesConstants.COMMENT_BEGIN + ThemesConstants.TEMPLATE_REGION_END + ThemesConstants.COMMENT_END;
			}
			region += propertyValue;
		}
		
		return region + ThemesConstants.COMMENT_BEGIN +	ThemesConstants.TEMPLATE_REGION_END + ThemesConstants.COMMENT_END;
	}
	
	/**
	 * Creates Builder's regions in XML () document
	 * @param docContent
	 * @return String
	 */
	private String addRegions(String docContent) {
		String fixedValue = null;
		for (int i = 0; i < ThemesConstants.REGIONS.size(); i++) {
			fixedValue = fixValue(ThemesConstants.REGIONS.get(i));
			while (docContent.indexOf(ThemesConstants.REGIONS.get(i)) != -1) {
				docContent = docContent.replace(ThemesConstants.REGIONS.get(i), getRegion(fixedValue));
			}
		}
		return docContent;
	}
	
	/**
	 * Replacing useless content with empty String
	 * @param value
	 * @return String
	 */
	private String fixValue(String value) {
		for (int i = 0; i < ThemesConstants.USELESS_CONTENT.size(); i++) {
			while (value.indexOf(ThemesConstants.USELESS_CONTENT.get(i)) != -1) {
				value = value.replace(ThemesConstants.USELESS_CONTENT.get(i), ThemesConstants.EMPTY);
			}
		}
		return value;
	}
	
	/**
	 * Adds region to div tag if div tag has id attribute
	 * @param e
	 * @return boolean
	 */
	private boolean addRegion(Element e) {
		if (e == null) {
			return false;
		}
		String regionID = e.getAttributeValue(ThemesConstants.TAG_ATTRIBUTE_ID);
		if (regionID == null) {
			return false;
		}
		
		if (needAddRegion(ThemesConstants.BASIC_IDS_FOR_REGIONS, regionID)) {
			e.addContent(0, getCommentsCollection(regionID));
		}
		if (regionID.equals(REGION_TO_EXPAND)) {
			e.addContent(getElement("div", "&nbsp;", "style", "height:"+THEME_HEIGHT+";visibility:hidden")); // Expanding theme
		}
		
		return true;
	}
	
	/**
	 * Changes theme with new style variation, creates draft and creates new preview image
	 * @param themeID
	 * @param styleGroupName
	 * @param styleMember
	 * @return String
	 */
	public String changeTheme(String themeID, String styleGroupName, String styleMember, String themeName, boolean radio, boolean checked) {
		if (themeID == null || styleGroupName == null || styleMember == null) {
			return null;
		}
		
		Theme theme = helper.getTheme(themeID);
		
		theme.setChangedName(themeName);
		
		String linkToDoc = theme.getLinkToDraft();
		if (linkToDoc == null) {
			linkToDoc = theme.getLinkToSkeleton();
		}
		
		if (linkToDoc.indexOf(ThemesConstants.SPACE) != -1) {
			linkToDoc = helper.urlEncode(linkToDoc);
		}
		Document doc = helper.getXMLDocument(helper.getFullWebRoot() + linkToDoc);
		if (doc == null) {
			return null;
		}
		
		boolean limitedSelection = true;
		
		ThemeStyleGroupMember oldStyle = null;
		ThemeStyleGroupMember newStyle = null;
		ThemeStyleGroupMember styleChanger = null;
		if (radio) { // Simply changing CSS files
			oldStyle = getEnabledStyleMember(theme, styleGroupName);
			newStyle = getStyleMember(theme, styleGroupName, styleMember);
			styleChanger = oldStyle;
		}
		else { //Need to know either add CSS or remove
			limitedSelection = false;
			if (checked) { // Need to add
				newStyle = getStyleMember(theme, styleGroupName, styleMember);
				styleChanger = newStyle;
			}
			else { //Need to remove
				oldStyle = getStyleMember(theme, styleGroupName, styleMember);
				styleChanger = oldStyle;
			}
		}
		
		Element root = doc.getRootElement();
		if (!changeThemeStyle(ThemesConstants.CONTENT + theme.getLinkToBase(), root.getChild(HTML_HEAD, namespace), oldStyle, newStyle)) {
			return null;
		}
		if (oldStyle != null) {
			oldStyle.setEnabled(false);
		}
		if (newStyle != null) {
			newStyle.setEnabled(true);
		}
		
		String draft = helper.getFileName(theme.getLinkToSkeleton()) + ThemesConstants.DRAFT;
		theme.setLinkToDraft(theme.getLinkToBase() + draft);
		if (!uploadDocument(doc, theme.getLinkToBaseAsItIs(), helper.decode(draft, true), theme, true)) {
			return null;
		}

		String uploadDir = helper.getFullWebRoot() + theme.getLinkToDraft();
		String fileName = theme.getName() +	ThemesConstants.DRAFT_PREVIEW;
		boolean result = helper.getImageGenerator().generatePreview(uploadDir, fileName, theme.getLinkToBaseAsItIs(), ThemesConstants.PREVIEW_WIDTH, ThemesConstants.PREVIEW_HEIGHT, true);
		if (!result) {
			return null;
		}
		addThemeChange(theme, styleChanger, limitedSelection);
		theme.setLinkToDraftPreview(fileName + ThemesConstants.DOT + helper.getImageGenerator().getFileExtension());
		helper.createSmallImage(theme, true);
		
		if (result) {
			return themeID;
		}
		return null;
	}

	private void addThemeChange(Theme theme, ThemeStyleGroupMember style, boolean limitedSelection) {
		if (theme == null || style == null) {
			return;
		}
		ThemeChange change = new ThemeChange();
		change.setLimitedSelection(limitedSelection);
		change.setEnabled(style.isEnabled());
		change.setStyleGroupName(style.getGroupName());
		change.setStyleGroupMember(style.getName());
		theme.addThemeChange(change);
	}
	
	/**
	 * Changes theme's old style with new
	 * @param head
	 * @param oldStyle
	 * @param newStyle
	 * @return boolean
	 */
	private boolean changeThemeStyle(String linkToBase, Element head, ThemeStyleGroupMember oldStyle, ThemeStyleGroupMember newStyle) {
		if (head == null) {
			return false;
		}
		
		List styles = head.getChildren(ThemesConstants.ELEMENT_LINK, namespace);
		if (styles == null) {
			return false;
		}
		
		int index = 4;
		List <Element> uselessStyles = new ArrayList <Element> ();
		
		if (oldStyle != null) {
			
			if (oldStyle.getStyleFiles() == null) {
				return false;
			}
			
			Element style = null;
			String attributeValue = null;
			List <String> files = null;
			boolean foundStyle = false;
			for (int i = 0; i < styles.size(); i++) {
				style = (Element) styles.get(i);
				attributeValue = style.getAttributeValue(ThemesConstants.TAG_ATTRIBUTE_HREF);
				files = oldStyle.getStyleFiles();
				foundStyle = false;
				if (files != null) {
					for (int j = 0; (j < files.size() && !foundStyle); j++) {
						if (attributeValue.indexOf(files.get(j)) != -1) {
							foundStyle = true;
							uselessStyles.add(style);
							index = getElementIndex(head.getChildren(), ThemesConstants.TAG_ATTRIBUTE_HREF, attributeValue);
						}
					}
				}
			}
			
		}
		
		if (newStyle != null) {
			head.addContent(index, getNewStyleElement(linkToBase, newStyle));
		}
		
		Iterator <Element> it = uselessStyles.iterator();
		while (it.hasNext()) {
			it.next().detach();
		}
		
		return true;
	}
	
	/**
	 * Creates new style element: <link href="..." ... />
	 * @param newStyle
	 * @return Collection
	 */
	private Collection <Element> getNewStyleElement(String linkToBase, ThemeStyleGroupMember newStyle) {
		Collection <Element> newStyleElements = new ArrayList <Element> ();
		if (linkToBase == null || newStyle == null) {
			return newStyleElements;
		}
		List <Attribute> attributes = null;
		Element newStyleHref = null;
		for (int i = 0; i < newStyle.getStyleFiles().size(); i++) {
			attributes = getBasicAttributesList();			
			attributes.add(new Attribute(ThemesConstants.TAG_ATTRIBUTE_HREF, linkToBase + newStyle.getStyleFiles().get(i)));

			newStyleHref = new Element(ThemesConstants.ELEMENT_LINK, namespace);
			newStyleHref.setAttributes(attributes);
			newStyleElements.add(newStyleHref);
		}
		return newStyleElements;
	}
	
	/**
	 * List of basic attributes for element
	 * @return List
	 */
	private List <Attribute> getBasicAttributesList() {
		List <Attribute> attributes = new ArrayList <Attribute> ();
		attributes.add(new Attribute(TAG_ATTRIBUTE_REL, TAG_ATTRIBUTE_VALUE_STYLESHEET));
		attributes.add(new Attribute(ThemesConstants.TAG_ATTRIBUTE_TYPE, TAG_ATTRIBUTE_VALUE_CSS));
		attributes.add(new Attribute(TAG_ATTRIBUTE_MEDIA, TAG_ATTRIBUTE_VALUE_SCREEN));
		return attributes;
	}
	
	/**
	 * Gets enabled style group member (theme's variation)
	 * @param theme
	 * @param styleGroupName
	 * @return ThemeStyleGroupMember
	 */
	private ThemeStyleGroupMember getEnabledStyleMember(Theme theme, String styleGroupName) {
		Map <String, ThemeStyleGroupMember> styleMembers = theme.getStyleGroupsMembers();
		int i = 0;
		ThemeStyleGroupMember member = styleMembers.get(styleGroupName + ThemesConstants.AT + i);
		while (member != null) {
			if (member.isEnabled()) {
				return member;
			}
			i++;
			member = styleMembers.get(styleGroupName + ThemesConstants.AT + i);
		}
		
		return null;
	}
	
	/**
	 * Gets enabled style groups members (theme's variations)
	 * @param theme
	 * @return List
	 */
	public List <ThemeStyleGroupMember> getEnabledStyles(Theme theme) {
		List <ThemeStyleGroupMember> members = new ArrayList<ThemeStyleGroupMember>();
		List <String> groupNames = theme.getStyleGroupsNames();
		ThemeStyleGroupMember member = null;
		String styleGroupName = null;
		Map <String, ThemeStyleGroupMember> styleMembers = theme.getStyleGroupsMembers();
		for (int i = 0; i < groupNames.size(); i++) {
			styleGroupName = groupNames.get(i);
			int j = 0;
			member = styleMembers.get(styleGroupName + ThemesConstants.AT + j);
			while (member != null) {
				if (member.isEnabled()) {
					members.add(member);
				}
				j++;
				member = styleMembers.get(styleGroupName + ThemesConstants.AT + j);
			}
		}
		return members;
	}
	
	/**
	 * Searches and returns style group member (theme's variation)
	 * @param theme
	 * @param styleGroupName
	 * @param styleVariation
	 * @return ThemeStyleGroupMember
	 */
	public ThemeStyleGroupMember getStyleMember(Theme theme, String styleGroupName, String styleVariation) {
		Map <String, ThemeStyleGroupMember> styleMembers = theme.getStyleGroupsMembers();
		int i = 0;
		ThemeStyleGroupMember member = styleMembers.get(styleGroupName + ThemesConstants.AT + i);
		while (member != null) {
			if (styleVariation.equals(member.getName())) {
				return member;
			}
			i++;
			member = styleMembers.get(styleGroupName + ThemesConstants.AT + i);
		}
		
		return null;
	}
	
	/**
	 * Saves theme
	 * @param themeID
	 * @param themeName
	 * @return boolean
	 */
	public boolean saveTheme(String themeID, String themeName) {
		if (themeID == null || themeName == null) {
			return false;
		}
		
		Theme theme = helper.getTheme(themeID);
		
		if (!theme.getName().equals(themeName)) {
			return createNewTheme(theme, themeName);
		}
		
		if (theme.getLinkToDraft() == null) {
			log.info("No draft for theme: " + themeID);
			return false;
		}

		InputStream is = null;
		is = helper.getInputStream(helper.getFullWebRoot() + theme.getLinkToDraft());
		if (is == null) {
			return false;
		}
		
		String fileName = helper.decode(helper.getFileNameWithExtension(theme.getLinkToSkeleton()), true);
		theme.setLocked(true);
		try {
			if (!helper.getSlideService().uploadFileAndCreateFoldersFromStringAsRoot(theme.getLinkToBaseAsItIs(), fileName, is, null, true)) {
				return false;
			}
		} catch (RemoteException e) {
			log.error(e);
			return false;
		} finally {
			helper.closeInputStream(is);
		}
		theme.setLocked(false);
		
		theme.setLinkToDraft(null);
		theme.setLinkToThemePreview(theme.getLinkToDraftPreview());
		theme.setLinkToDraftPreview(null);
		
		try {
			helper.getThemesService().createIBPage(theme);
		} catch (RemoteException e) {
			log.error(e);
			return false;
		}
		
		return helper.createThemeConfig(theme);
	}
	
	private boolean restoreTheme(Theme theme) {
		if (theme == null) {
			return false;
		}
		List <ThemeChange> changes = theme.getChanges();
		if (changes.size() == 0) {
			return true;
		}
		ThemeChange change = null;
		ThemeStyleGroupMember member = null;
		for (int i = 0; i < changes.size(); i++) {
			change = changes.get(i);
			member = getStyleMember(theme, change.getStyleGroupName(), change.getStyleGroupMember());
			if (member != null) {
				if (change.isLimitedSelection()) {
					disableStyle(theme, member.getGroupName());
				}
				member.setEnabled(!change.isEnabled());
			}
		}
		theme.setChangedName(null);
		theme.setLinkToDraftPreview(null);
		theme.setLinkToDraft(null);
		theme.setChanges(new ArrayList<ThemeChange>());
		
		InputStream is = helper.getInputStream(helper.getFullWebRoot() + theme.getLinkToBase() + helper.encode(theme.getLinkToThemePreview(), true));
		String extension = helper.getFileExtension(theme.getLinkToThemePreview());
		String fileName = theme.getName() + ThemesConstants.THEME_SMALL_PREVIEW + ThemesConstants.DOT + extension;
		helper.getImageGenerator().encodeAndUploadImage(theme.getLinkToBaseAsItIs(), fileName, ThemesConstants.DEFAULT_MIME_TYPE + extension, is, ThemesConstants.SMALL_PREVIEW_WIDTH, ThemesConstants.SMALL_PREVIEW_HEIGHT);
		theme.setLinkToSmallPreview(fileName);
		helper.closeInputStream(is);
		
		return true;
	}
	
	public boolean restoreTheme(String themeID) {
		Theme theme = helper.getTheme(themeID);
		return restoreTheme(theme);
	}
	
	private void disableStyle(Theme theme, String styleGroupName) {
		ThemeStyleGroupMember member = null;
		Iterator <ThemeStyleGroupMember> it = theme.getStyleGroupsMembers().values().iterator();
		while (it.hasNext()) {
			member = it.next();
			if (member.getGroupName().equals(styleGroupName)) {
				member.setEnabled(false);
			}
		}
	}
	
	private boolean createNewTheme(Theme parent, String newName) {
		// Copying Theme skeleton
		String linkToTheme = parent.getLinkToDraft();
		if (linkToTheme == null) {
			linkToTheme = parent.getLinkToSkeleton();
		}
		if (linkToTheme == null) {
			return false;
		}
		InputStream is = helper.getInputStream(helper.getFullWebRoot() + linkToTheme);
		if (is == null) {
			return false;
		}
		String linkToBase = helper.getLinkToBase(linkToTheme);
		if (!linkToBase.endsWith(ThemesConstants.SLASH)) {
			linkToBase += ThemesConstants.SLASH;
		}
		String decodedLinkToBase = helper.decodeUrl(linkToBase);
		if (!decodedLinkToBase.endsWith(ThemesConstants.SLASH)) {
			decodedLinkToBase += ThemesConstants.SLASH;
		}
		String themeName = helper.removeSpaces(newName + ThemesConstants.THEME);
		try {
			if (!helper.getSlideService().uploadFileAndCreateFoldersFromStringAsRoot(decodedLinkToBase,	themeName, is, null, true)) {
				return false;
			}
		} catch (RemoteException e) {
			log.error(e);
			return false;
		} finally {
			helper.closeInputStream(is);
		}
		
		String themeID = helper.getThemesLoader().createNewTheme(decodedLinkToBase + themeName, linkToBase + helper.encode(themeName,
				true), true, true);
		if (themeID == null) {
			return false;
		}
		Theme child = helper.getTheme(themeID);
		child.setName(newName);

		copyTheme(parent, child);
		
		// Copying Theme preview image
		String linkToPreview = parent.getLinkToDraftPreview();
		if (linkToPreview == null) {
			linkToPreview = parent.getLinkToThemePreview();
		}
		String endodedLinkToPreview = helper.encode(linkToPreview, true);
		linkToBase = child.getLinkToBase();
		if (!linkToBase.endsWith(ThemesConstants.SLASH)) {
			linkToBase += ThemesConstants.SLASH;
		}
		is = helper.getInputStream(helper.getFullWebRoot() + linkToBase + endodedLinkToPreview);
		if (is == null) {
			return false;
		}
		String extension = helper.getFileExtension(linkToPreview);
		String fileName = child.getName() + ThemesConstants.THEME_PREVIEW + ThemesConstants.DOT + extension;
		try {
			if (helper.getSlideService().uploadFileAndCreateFoldersFromStringAsRoot(decodedLinkToBase, fileName, is, null, true)) {
				child.setLinkToThemePreview(fileName);
			}
		} catch (RemoteException e) {
			log.error(e);
		} finally {
			helper.closeInputStream(is);
		}
		
		// Setting Theme small preview
		is = helper.getInputStream(helper.getFullWebRoot() + linkToBase + endodedLinkToPreview);
		fileName = child.getName() + ThemesConstants.THEME_SMALL_PREVIEW + ThemesConstants.DOT + extension;
		helper.getImageGenerator().encodeAndUploadImage(decodedLinkToBase, fileName, ThemesConstants.DEFAULT_MIME_TYPE + extension, is, ThemesConstants.SMALL_PREVIEW_WIDTH, ThemesConstants.SMALL_PREVIEW_HEIGHT);
		child.setLinkToSmallPreview(fileName);
		helper.closeInputStream(is);
		
		child.setPropertiesExtracted(true);
		restoreTheme(parent);
		
		// Creating new template
		try {
			helper.getThemesService().createIBPage(child);
		} catch (RemoteException e) {
			log.error(e);
			return false;
		}
		
		return helper.createThemeConfig(child);
	}
	
	/**
	 * Copies parent theme's style groups and variations to child theme
	 * @param parent
	 * @param child
	 */
	private void copyTheme(Theme parent, Theme child) {
		List <String> groupNames = parent.getStyleGroupsNames();
		ThemeStyleGroupMember member = null;
		ThemeStyleGroupMember parentMember = null;
		String styleGroupName = null;
		Map <String, ThemeStyleGroupMember> styleMembers = parent.getStyleGroupsMembers();
		for (int i = 0; i < groupNames.size(); i++) {
			styleGroupName = groupNames.get(i);
			child.addStyleGroupName(styleGroupName);
			int j = 0;
			parentMember = styleMembers.get(styleGroupName + ThemesConstants.AT + j);
			while (parentMember != null) {
				member = new ThemeStyleGroupMember(parentMember);
				child.addStyleGroupMember(styleGroupName + ThemesConstants.AT + j, member);
				j++;
				parentMember = styleMembers.get(styleGroupName + ThemesConstants.AT + j);
			}
		}
	}
	
	protected XMLOutputter getXMLOutputter() {
		return out;
	}

}