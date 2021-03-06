<?xml version="1.0"?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:jsf="http://java.sun.com/jsf/core"
	xmlns:wf="http://xmlns.idega.com/com.idega.webface"
	xmlns:c="http://xmlns.idega.com/com.idega.content"
	xmlns:ic="http://xmlns.idega.com/com.idega.core"
	xmlns:web2="http://xmlns.idega.com/com.idega.block.web2.0"
	xmlns:x="http://myfaces.apache.org/tomahawk" version="1.2">

	<jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" />

	<jsf:view>
		<ic:page styleClass="ws_body" id="createpage" javascripturls="/dwr/engine.js,
        			/dwr/interface/ThemesEngine.js,
					/dwr/interface/BuilderService.js,
					/idegaweb/bundles/com.idega.block.web2.0.bundle/resources/javascript/mootools/1.11/mootools-all-compressed.js,

					/idegaweb/bundles/com.idega.content.bundle/resources/javascript/drag-drop-folder-tree.js,
					/idegaweb/bundles/com.idega.content.bundle/resources/javascript/tree.js,
					/idegaweb/bundles/com.idega.content.bundle/resources/javascript/ThemesHelper.js,
        			/idegaweb/bundles/com.idega.content.bundle/resources/javascript/SiteManagerHelper.js">        			
			<h:form id="createpageform">
				<jsf:verbatim>
                	<script type="text/javascript">
                		window.addEvent('domready', function() {
							var errorHanlder = function() {
								reloadPage();
							}
							dwr.engine.setErrorHandler(errorHanlder);
						});
                		window.addEvent('domready', startBuilderApplication);
                		window.addEvent('domready', getGlobalPageId);
                		window.addEvent('domready', getLocalizedTextForThemes);
                		window.addEvent('domready', initialiazeSiteManager);
                		window.addEvent('domready', getPathToImageFolder);
 	              		window.addEvent('domready', registerSiteActions);                		
                	</script>
                </jsf:verbatim>
			
				<wf:wfblock maximizedVertically="true" id="siteMapBlock" title="#{localizedStrings['com.idega.content']['site_map']}">
                	<x:div id="leftSide" forceId="true" styleClass="accordionInPages">
						<web2:accordion accordionId="myAccordion" includeJavascript="true">
							<jsf:facet name="PANELS">
								<x:div id="structureAccordion" forceId="true">
									<x:div id="siteMapInformation" forceId="true" styleClass="acTogglemyAccordion">
										<h:outputText value="#{localizedStrings['com.idega.content']['current_site_structure']}"/>
									</x:div>
									
									<x:div id="sitemap" forceId="true" styleClass="acStretchmyAccordion">
										<x:div id="site_tree_container" forceId="true">
											<c:block_with_toolbar id="page_tree_div" styleClass="site_tree_container_site_accordion" title="#{localizedStrings['com.idega.content']['current_site_structure']}" collapseAllValue="#{localizedStrings['com.idega.content']['collapse_all']}" expandAllValue="#{localizedStrings['com.idega.content']['expand_all']}" trashCanImage="/idegaweb/bundles/com.idega.content.bundle/resources/images/user-trash.png">
												<wf:iwtree value="#{pageCreationBean.pageSelectorTopNode}" id="current_structure_tree" var="node" varNodeToggler="t" clientSideToggle="true"	showRootNode="false">
													<jsf:facet name="PageTreeNode">
														<h:outputLink styleClass="pageTreeNames">
															<h:outputText value="#{node.description}"/>
														</h:outputLink>
													</jsf:facet>
												</wf:iwtree>
											</c:block_with_toolbar>
										</x:div>										
									</x:div>
																				
									<x:div id="siteinformation" forceId="true" styleClass="acTogglemyAccordion">
										<h:outputText value="#{localizedStrings['com.idega.content']['site_info']}"/>
									</x:div>
									<x:div id="siteInfoToggle" forceId="true" styleClass="acStretchmyAccordion">
										<x:div id="siteInfoContainer" forceId="true">
											<c:SiteInfo id="siteInfo" styleClass="siteInfoStyle_accordion"></c:SiteInfo>
										</x:div>
									</x:div>
								</x:div>
							</jsf:facet>
						</web2:accordion>     
					</x:div>     

					<x:div id="pagesTypesContainer" forceId="true">
						<wf:wfblock id="dhtmlgoodies_tree2" title="#{localizedStrings['com.idega.content']['page_types']}" styleClass="pagesTypesContainer">
							<wf:iwtree value="#{siteTemplateBean.pageTree}" id="page_chooser22"	var="node" varNodeToggler="t" clientSideToggle="true"	sourceTree="true"	showRootNode="false">
								<jsf:facet name="IWTreeNode">
									<h:panelGroup>
										<h:outputLink>
											<h:outputText value="#{node.description}"/>									
										</h:outputLink>
									</h:panelGroup>
								</jsf:facet>
							</wf:iwtree>
						</wf:wfblock>
					</x:div>
					
					<x:div id="siteTemplatesContainer" forceId="true">
						<wf:wfblock id="siteTemplatesBlock" title="#{localizedStrings['com.idega.content']['site_templates']}" styleClass="siteTemplatesContainer">
							<c:siteTemplatesViewer id="siteTemplates"/>
						</wf:wfblock>
					</x:div>

					<jsf:verbatim>
						<script type="text/javascript">	
							window.addEvent('domready', function() {
								appendIdOfTree('page_chooser22');
								appendIdOfAdvancedTree('current_structure_tree');
							});
						</script>
					</jsf:verbatim>		
				</wf:wfblock>
			</h:form>
		</ic:page>
	</jsf:view>
</jsp:root>