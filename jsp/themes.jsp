<?xml version="1.0"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
        xmlns:h="http://java.sun.com/jsf/html"
        xmlns:jsf="http://java.sun.com/jsf/core"
        xmlns:wf="http://xmlns.idega.com/com.idega.webface"
        xmlns:ws="http://xmlns.idega.com/com.idega.workspace"
        xmlns:c="http://xmlns.idega.com/com.idega.content"
        xmlns:t="http://myfaces.apache.org/tomahawk"
version="1.2">
<jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
	<jsf:view>
        <ws:page id="themeManager" javascripturls="
        				/idegaweb/bundles/com.idega.block.web2.0.bundle/resources/javascript/mootools/1.11/mootools-all-compressed.js,
						
						/idegaweb/bundles/com.idega.block.web2.0.bundle/resources/javascript/reflection/for_mootools/1.2/reflection.js,
						
						/dwr/engine.js,
        				/dwr/interface/ThemesEngine.js,
						
        				/idegaweb/bundles/com.idega.content.bundle/resources/javascript/ThemesManagerHelper.js,
        				/idegaweb/bundles/com.idega.content.bundle/resources/javascript/PageInfoHelper.js,
        				/idegaweb/bundles/com.idega.content.bundle/resources/javascript/ThemesHelper.js">
			<h:form id="uploadForm" enctype="multipart/form-data" onsubmit="showLoadingMessage(getUploadingThemeText());">
				<jsf:verbatim>
					<script type="text/javascript">
						window.addEvent('domready', function() {
							var errorHanlder = function() {
								reloadPage();
							}
							DWREngine.setErrorHandler(errorHanlder);
						});
						window.addEvent('domready', getLocalizedTextForThemes);
						window.addEvent('domready', initializeThemes);
						window.addEvent('domready', roundThemesSliderCorners);
					</script>
				</jsf:verbatim>

				<t:div id="mainThemesContainer" forceId="true">
					<t:div id="mainThemesContentContainer" forceId="true" styleClass="themesContentContainerStyleClass">
						<t:div id="themePreviewAndSliderContainer" forceId="true" styleClass="themePreviewAndSliderContainerStyle">
							<t:div id="themePreviewContainer" forceId="true" styleClass="themePreviewContainerStyle">
								<jsf:verbatim>
									<img id="themePreview" />
								</jsf:verbatim>
							</t:div>
							
							<t:div style="width: 100%; height: 1px;"></t:div>
							
							<c:ThemesSliderViewer />
						</t:div>
						
						<t:div id="themeVariationsContainer" forceId="true" styleClass="theme_container">
							<h:outputText styleClass="variationHeading" value="#{localizedStrings['com.idega.content']['theme_variations']}"></h:outputText>
							<t:div id="themeStyleVariationsBlock" forceId="true">
								<t:div id="themeUsability" forceId="true" styleClass="themeUsabilityStyle"></t:div>
								<t:inputHidden id="defaultThemeLabel" forceId="true" value="#{localizedStrings['com.idega.content']['theme_is_default']}"></t:inputHidden>
								<t:inputHidden id="notDefaultThemeLabel" forceId="true" value="#{localizedStrings['com.idega.content']['theme_is_not_default']}"></t:inputHidden>
								<t:div id="themeStyleVariations" forceId="true"></t:div>
							</t:div>
							<t:div id="themeSaveArea" forceId="true">
								<h:outputText styleClass="variationHeading" value="#{localizedStrings['com.idega.content']['save_theme']}"></h:outputText>
								<t:div styleClass="wf_webdav_upload">
									<h:outputText value="#{localizedStrings['com.idega.content']['theme_name']}"></h:outputText>
									<t:inputText id="theme_name" forceId="true"></t:inputText>
									<t:div style="padding-top: 5px;">
										<t:commandButton id="changeVariationsButton" type="button" forceId="true" onclick="changeVariations()" title="#{localizedStrings['com.idega.content']['change_variations']}" value="#{localizedStrings['com.idega.content']['change']}"></t:commandButton>
										<t:commandButton id="themeSaveButton" type="button" forceId="true" onclick="saveTheme()" title="#{localizedStrings['com.idega.content']['save']}" value="#{localizedStrings['com.idega.content']['save']}"></t:commandButton>
										<t:commandButton id="themeRestoreButton" type="button" forceId="true" onclick="restoreTheme()" title="#{localizedStrings['com.idega.content']['restore_theme']}" value="#{localizedStrings['com.idega.content']['restore_theme']}"></t:commandButton>
									</t:div>
								</t:div>
							</t:div>
							<c:ThemesManager id="uploadBlock"></c:ThemesManager>
						</t:div>
					</t:div>
               	</t:div>
			</h:form>
        </ws:page>
	</jsf:view>
</jsp:root>