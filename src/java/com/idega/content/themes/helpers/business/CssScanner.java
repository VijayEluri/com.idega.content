package com.idega.content.themes.helpers.business;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.content.business.ContentConstants;
import com.idega.servlet.filter.IWBundleResourceFilter;
import com.idega.util.ArrayUtil;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringHandler;
import com.idega.util.StringUtil;
import com.idega.util.resources.ResourceScanner;

@Scope("session")
@Service(CssScanner.SPRING_BEAN_IDENTIFIER)
public class CssScanner implements ResourceScanner {
	
	private static final long serialVersionUID = -694282023660051365L;
	private static Logger LOGGER = Logger.getLogger(CssScanner.class.getName());
	
	public static final String SPRING_BEAN_IDENTIFIER = "cssScanner";
	
	private BufferedReader readerBuffer = null;
	private StringBuffer resultBuffer = null;
	
	private String linkToTheme = null;
	
	private boolean needToReplace = false;
	
	private int openers = 0;
	private int closers = 0;
	
	private static final String COLOR_STRING = "color";
	private static final String COMMENT_BEGIN = "/*";
	private static final String COMMENT_END = "*/";
	private static final String DIRECTORY_LEVEL_UP = "../";
	private static final String OPENER = "{";
	private static final String CLOSER = "}";
	private static final String UTF_8_DECLARATION = "@charset \"UTF-8\";";
	
	public void scanFile() {
		if (readerBuffer == null) {
			return;
		}
		
		resultBuffer = new StringBuffer();
		String line;
		String changedLine = null;
		try {
			while ((line = readerBuffer.readLine()) != null) {
				changedLine = line;
				changedLine = scanLine(line);
				resultBuffer.append(changedLine).append(ThemesConstants.NEW_LINE);
				
				if (!line.equals(changedLine)) {	// If line was modified
					needToReplace = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public void setReaderBuffer(BufferedReader readerBuffer) {
		this.readerBuffer = readerBuffer;
	}

	public void setLinkToTheme(String linkToTheme) {
		this.linkToTheme = linkToTheme;
	}

	private boolean canUseURL(String line) {
		boolean urlElementExists = true;
		
		String urlStart = "url";
		String[] urls = line.split(urlStart);
		if (urls == null || urls.length < 2) {
			return true;
		}
		
		File f = null;
		String urlValueInCss = null;
		String path = null;
		//	Zero element is not interesting...
		for (int i = 1; (i < urls.length && urlElementExists); i++) {
			urlValueInCss = urls[i];
			f = null;
			
			int start = urlValueInCss.indexOf(ContentConstants.BRACKET_OPENING);
			int end = urlValueInCss.lastIndexOf(ContentConstants.BRACKET_CLOSING);
			urlValueInCss = urlValueInCss.substring(start + 1, end);
			urlValueInCss = StringHandler.replace(urlValueInCss, DIRECTORY_LEVEL_UP, ThemesConstants.EMPTY);
			urlValueInCss = StringHandler.replace(urlValueInCss, "'", CoreConstants.EMPTY);
			urlValueInCss = StringHandler.replace(urlValueInCss, "\"", CoreConstants.EMPTY);
			
			path = new StringBuffer(linkToTheme).append(urlValueInCss).toString();
			try {
				f = ThemesHelper.getInstance().getSlideService().getFile(path);
			} catch (URIException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (f == null) {
				urlElementExists = false;
			}
			else {
				try {
					urlElementExists = f.exists();
				} catch (Exception e) {
					e.printStackTrace();
					urlElementExists = false;
				}
			}
		}
		
		if (!urlElementExists) {
			LOGGER.log(Level.WARNING, "File '" + path + "' does not exist in Theme's pack! Removing CSS expression: " + line);
		}
		return urlElementExists;
	}
	
	private String scanLine(String line) {
		if (line == null) {
			return ThemesConstants.EMPTY;
		}
		
		if (line.indexOf(UTF_8_DECLARATION) != -1) {
			line = StringHandler.replace(line, UTF_8_DECLARATION, CoreConstants.EMPTY);
			return line;
		}
		
		// Checking for incorrect hexidecimal values
		if (line.indexOf(CoreConstants.NUMBER_SIGN) != -1 && line.indexOf(COLOR_STRING) != -1) {
			String[] colorValue = line.split(CoreConstants.NUMBER_SIGN);
			if (colorValue == null) {
				return line;
			}
			if (colorValue.length != 2) {
				return line;
			}
			String color = colorValue[1];
			int index = StringHandler.getNotHexValueIndexInHexValue(color);
			String letterToReplace = null;
			while (index >= 0) {
				letterToReplace = CoreConstants.HEXIDECIMAL_LETTERS.get(ThemesHelper.getInstance().getRandomNumber(CoreConstants.HEXIDECIMAL_LETTERS.size()));
				if (index == color.length() - 1) {
					color = color.replace(color.substring(index), letterToReplace);
				}
				else {
					color = color.replace(color.substring(index, index + 1), letterToReplace);
				}
				index = StringHandler.getNotHexValueIndexInHexValue(color);
			}
			line = new StringBuffer(colorValue[0]).append(CoreConstants.NUMBER_SIGN).append(color).toString();
		}
		
		//	Checking URL stuff
		if (line.indexOf("url(") != -1) {
			if (canUseURL(line)) {
				return line;
			}
			else {
				return ThemesConstants.EMPTY;
			}
		}
		
		//	Checking comments
		if (line.indexOf(OPENER) == -1 && line.indexOf(CLOSER) == -1) {
			return line;
		}
		
		int commentBeginIndex = line.indexOf(COMMENT_BEGIN);
		int commentEndIndex = line.indexOf(COMMENT_END);
		int styleDefinitionBeginIndex = line.indexOf(OPENER);
		int styleDefinitionEndIndex = line.indexOf(CLOSER);
		if (commentBeginIndex != -1 && commentEndIndex != -1) {
			// CSS: /* www.multithemes.com */
			if (styleDefinitionBeginIndex == -1 && styleDefinitionEndIndex == -1) {
				return line;
			}
			
			//	CSS: /*comment*/body { or: body {/*comment*/
			if ((styleDefinitionBeginIndex > commentBeginIndex && styleDefinitionBeginIndex > commentEndIndex) || (styleDefinitionBeginIndex < commentBeginIndex && styleDefinitionBeginIndex < commentEndIndex)) {
				openers++;
				return line;
			}
			
			// CSS: /*comment*/} or }/*comment*/
			if ((styleDefinitionEndIndex > commentBeginIndex && styleDefinitionEndIndex > commentEndIndex) || (styleDefinitionEndIndex < commentBeginIndex && styleDefinitionEndIndex < commentEndIndex)) {
				return finishCssLine(line);
			}
		}
		else {
			// Lines of comment
			if (commentBeginIndex != -1 && commentEndIndex == -1) {
				if (styleDefinitionBeginIndex != -1 && styleDefinitionBeginIndex < commentBeginIndex) {
					openers++;
				}
				if (styleDefinitionEndIndex != -1 && styleDefinitionEndIndex < commentBeginIndex) {
					return finishCssLine(line);
				}
				
				return line;
			}

			if (commentBeginIndex == -1 && commentEndIndex != -1) {
				if (styleDefinitionBeginIndex != -1 && styleDefinitionBeginIndex > commentEndIndex) {
					openers++;
				}
				if (styleDefinitionEndIndex != -1 && styleDefinitionEndIndex > commentEndIndex) {
					return finishCssLine(line);
				}
				
				return line;
			}
		}
		
		if (line.indexOf(OPENER) != -1 && line.indexOf(CLOSER) != -1 && openers == closers) {
			return line;
		}
		
		if (line.indexOf(OPENER) != -1 && line.indexOf(CLOSER) == -1) {
			openers++;
			return line;
		}
		
		if (line.indexOf(OPENER) == -1 && line.indexOf(CLOSER) != -1) {
			return finishCssLine(line);
		}
		
		return line;
	}
	
	private String finishCssLine(String line) {
		closers++;
		if (closers != openers) {
			line = line.replace(CLOSER, ThemesConstants.EMPTY);
		}
		openers = 0;
		closers = 0;
		return line;
	}

	public StringBuffer getResultBuffer() {
		return resultBuffer;
	}

	public boolean isNeedToReplace() {
		return needToReplace;
	}

	public String getParsedContent(List<String> contentLines, String fileUri) {
		if (ListUtil.isEmpty(contentLines) || StringUtil.isEmpty(fileUri)) {
			return null;
		}
		
		String parsedLine = null;
		StringBuilder parsedContent = new StringBuilder();
		for (String line: contentLines) {
			parsedLine = getParsedLine(line, fileUri);
			parsedContent.append("\n").append(parsedLine);
		}
		
		return parsedContent.toString();
	}
	
	private String getParsedLine(String line, String fileUri) {
		String urlExpressionStart = "url(";
		if (line.indexOf(urlExpressionStart) == -1) {
			return line;
		}
		if (line.indexOf(IWBundleResourceFilter.BUNDLES_STANDARD_DIR) != -1) {
			return line;
		}
		
		line = line.replaceAll(CoreConstants.QOUTE_SINGLE_MARK, CoreConstants.EMPTY);
		line = line.replaceAll(CoreConstants.QOUTE_MARK, CoreConstants.EMPTY);
		
		int startIndex = line.indexOf(urlExpressionStart);
		int endIndex = line.indexOf(")");
		String urlExpression = line.substring(startIndex + urlExpressionStart.length(), endIndex);
		String originalExpression = urlExpression;
		
		String urlReplacement = null;
		
		int levelsUp = 0;
		if (urlExpression.startsWith(DIRECTORY_LEVEL_UP)) {
			while (urlExpression.indexOf(DIRECTORY_LEVEL_UP) != -1) {
				urlExpression = urlExpression.replaceFirst(DIRECTORY_LEVEL_UP, CoreConstants.EMPTY);
				levelsUp++;
			}
		}
		
		if (levelsUp == 0) {
			urlReplacement = new StringBuilder(fileUri).append(urlExpression).toString();
		}
		else {
			String[] resourceParts = fileUri.split(CoreConstants.SLASH);
			if (ArrayUtil.isEmpty(resourceParts)) {
				return line;
			}
			
			StringBuilder newUrlExpression = new StringBuilder();
			for (int i = 0; i < (resourceParts.length - levelsUp); i++) {
				if (!StringUtil.isEmpty(resourceParts[i])) {
					newUrlExpression.append(CoreConstants.SLASH).append(resourceParts[i]);
				}
			}
			
			urlReplacement = newUrlExpression.append(CoreConstants.SLASH).append(urlExpression).toString();
		}
		
		if (StringUtil.isEmpty(urlReplacement)) {
			return line;
		}
		
		line = line.replace(originalExpression, urlReplacement);
		return line;
	}

}
