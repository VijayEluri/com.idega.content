<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" 
	xmlns:f="http://java.sun.com/jsf/core" 	
	xmlns:h="http://java.sun.com/jsf/html" 	
	xmlns:jsp="http://java.sun.com/JSP/Page" 	
	xmlns:wf="http://xmlns.idega.com/com.idega.webface"
	xmlns:cmf="http://myfaces.sourceforge.net/tld/myfaces_ext_0_9.tld"
	xmlns:co="http://xmlns.idega.com/com.idega.content">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
    		<wf:workspace_page id="1234">
               <h:form id="uploadForm" name="uploadForm" enctype="multipart/form-data">
				<co:ContentViewer id="gt"/>
				<wf:wfblock id="pr332" title="Upload">
               <f:verbatim><br/></f:verbatim>
                    <h:outputText value="Select a file to upload : "/>
                 <cmf:inputFileUpload id="fileupload"
                                       accept="*"
                                       value="#{WebDAVUploadBean.uploadFile}"
                                       storage="file"
                                       styleClass="fileUploadInput"/>
                <f:verbatim><br/></f:verbatim>
                    <h:outputText id ="nametext" value="and give it a name (optional) : "/>
                    <h:inputText id="filename" value="#{WebDAVUploadBean.fileName}"/>
                       <f:verbatim><br/></f:verbatim>
                    <h:outputText id="versiontext" value="and a version comment : "/>
                       <h:inputText id="comment" value="#{WebDAVUploadBean.comment}"/>
                    <f:verbatim><br/></f:verbatim>
                    <h:outputText id ="outtext" value="and select the folder to upload to (optional) : "/>
                     <h:inputText id="uploadPath" value="#{WebDAVListBean.webDAVPath}"/>
                    <h:commandButton value="Upload" action="#{WebDAVUploadBean.upload}"/>
                
                <f:verbatim><br/></f:verbatim>
                
                <h:outputLink id="filelink" value="#{WebDAVUploadBean.downloadPath}" target="_new" rendered="#{WebDAVUploadBean.isUploaded}" >
                <f:verbatim>Click here to get the file</f:verbatim>
                </h:outputLink>
                <f:verbatim><br/></f:verbatim>
                <h:graphicImage id="imagePreview" value="#{WebDAVUploadBean.imagePath}"/>
                
                </wf:wfblock>

			</h:form>
		</wf:workspace_page>
    </f:view>
</jsp:root>

