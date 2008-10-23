package com.idega.content.upload.presentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import com.idega.block.web2.business.Web2Business;
import com.idega.content.business.ContentConstants;
import com.idega.content.business.ContentUtil;
import com.idega.content.themes.helpers.business.ThemesHelper;
import com.idega.content.upload.business.FileUploader;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.PresentationObjectContainer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;
import com.idega.webface.WFUtil;

public class FileUploadViewer extends IWBaseComponent {
	
	private String actionAfterUpload = null;
	private String actionAfterCounterReset = null;
	private String uploadPath = CoreConstants.PUBLIC_PATH + CoreConstants.SLASH;
	private String formId = null;
	
	private boolean zipFile = false;
	private boolean extractContent = false;
	private boolean themePack = false;
	private boolean showProgressBar = true;
	private boolean showLoadingMessage = false;
	private boolean allowMultipleFiles = false;
	
	@Override
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
		
		this.actionAfterUpload = values[1] == null ? null : values[1].toString();
		this.uploadPath = values[2] == null ? null : values[2].toString();
		
		this.zipFile = values[3] == null ? Boolean.FALSE : (Boolean) values[3];
		this.extractContent = values[4] == null ? Boolean.FALSE : (Boolean) values[4];
		this.themePack = values[5] == null ? Boolean.FALSE : (Boolean) values[5];
		this.showProgressBar = values[6] == null ? Boolean.TRUE : (Boolean) values[6];
		this.showLoadingMessage = values[7] == null ? Boolean.FALSE : (Boolean) values[7];
		this.allowMultipleFiles = values[8] == null ? Boolean.FALSE : (Boolean) values[8];
	}
	
	@Override
	public Object saveState(FacesContext context) {
		Object values[] = new Object[9];
		values[0] = super.saveState(context);
		
		values[1] = this.actionAfterUpload;
		values[2] = this.uploadPath;
		
		values[3] = this.zipFile;
		values[4] = this.extractContent;
		values[5] = this.themePack;
		values[6] = this.showProgressBar;
		values[7] = this.showLoadingMessage;
		values[8] = this.allowMultipleFiles;
		
		return values;
	}
	
	@Override
	public void initializeComponent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);
		FileUploader uploader = null;
		try {
			uploader = WFUtil.getBeanInstance(context, FileUploader.SPRING_BEAN_IDENTIFIER);
			uploader.initializeUploader(iwc);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		if (themePack) {
			ThemesHelper.getInstance(false).getSlideService(iwc);
			zipFile = true;
			extractContent = true;
		}
		
		IWBundle bundle = iwc.getIWMainApplication().getBundle(ContentConstants.IW_BUNDLE_IDENTIFIER);
		IWResourceBundle iwrb = bundle.getResourceBundle(iwc);
		
		Layer container = new Layer();
		add(container);
		container.setStyleClass("fileUploadViewerMainLayerStyle");
		
		PresentationObjectContainer mainContainer = container;
		if (formId == null) {
			Form form = new Form();
			form.setMultiPart();
			form.setAction("/servlet/ContentFileUploadServlet");
			form.setMethod("post");
			container.add(form);
			mainContainer = form;
			formId = form.getId();
		}
		
		HiddenInput path = new HiddenInput(ContentConstants.UPLOADER_PATH, uploadPath);
		mainContainer.add(path);
		HiddenInput zipFileValue = new HiddenInput(ContentConstants.UPLOADER_UPLOAD_ZIP_FILE, String.valueOf(zipFile));
		mainContainer.add(zipFileValue);
		HiddenInput themePackValue = new HiddenInput(ContentConstants.UPLOADER_UPLOAD_THEME_PACK, String.valueOf(themePack));
		mainContainer.add(themePackValue);
		HiddenInput extractContentValue = new HiddenInput(ContentConstants.UPLOADER_UPLOAD_EXTRACT_ARCHIVED_FILE, String.valueOf(extractContent));
		mainContainer.add(extractContentValue);
		
		Layer fileInputs = new Layer();
		String id = fileInputs.getId();
		fileInputs.setStyleClass("fileUploadInputsContainerStyle");
		fileInputs.add(uploader.getFileInput(iwc, false));	//	Not adding 'remove' image - at least one file input should remain
		mainContainer.add(fileInputs);
		
		Layer buttonsContainer = new Layer();
		buttonsContainer.setStyleClass("fileUploadButtonsContainerStyle");
		if (allowMultipleFiles) {
			GenericButton addFileInput = new GenericButton(iwrb.getLocalizedString("add_file", "Add file"));
			StringBuffer action = new StringBuffer("addFileInputForUpload('").append(id).append("', '").append(iwrb.getLocalizedString("loading", "Loading..."));
			action.append("');");
			addFileInput.setOnClick(getActionToLoadFilesAndExecuteCustomAction(action.toString()));
			buttonsContainer.add(addFileInput);
		}
		
		String uploadingFile = iwrb.getLocalizedString("uploading_file", "Uploading file");
		String uploadStatus = iwrb.getLocalizedString("completed_please_wait", "completed, please wait...");
		
		Layer progressBarBox = new Layer();
		progressBarBox.setStyleClass("fileUploaderProgressBarStyle");
		progressBarBox.setStyleAttribute("visibility: hidden;");
		String progressBarId = progressBarBox.getId();
		Span progressText = new Span();
		progressText.add(new Text(new StringBuffer(uploadingFile).append(": 0% ").append(uploadStatus).toString()));
		progressText.setId(progressBarId + "_progressText");
		progressBarBox.add(progressText);
		mainContainer.add(progressBarBox);
		
		List<String> localization = new ArrayList<String>(3);
		localization.add(uploadingFile);
		localization.add(uploadStatus);
		localization.add(iwrb.getLocalizedString("upload_was_successfully_finished", "Upload was successfully finished."));
		
		String inavlidTypeMessage = iwrb.getLocalizedString("incorrect_file_type", "Unsupported file type! Only zip files allowed");
		GenericButton upload = new GenericButton(iwrb.getLocalizedString("upload", "Upload"));
		upload.setOnClick(getAction(id, iwrb.getLocalizedString("uploading", "Uploading..."), inavlidTypeMessage, progressBarId, localization));
		buttonsContainer.add(upload);
		
		mainContainer.add(buttonsContainer);
	}
	
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		super.encodeBegin(context);
	}
	
	private String getAction(String id, String loadingMessage, String invalidTypeMessage, String progressBarId, List<String> localization) {
		StringBuffer action = new StringBuffer("uploadFiles('").append(id).append("', '").append(loadingMessage).append("', ").append(showProgressBar).append(", ");
		action.append(showLoadingMessage).append(", ").append(zipFile).append(", '").append(invalidTypeMessage).append("', '").append(formId).append("', '");
		action.append(progressBarId).append("', ");
		
		action.append("['");
		for (int i = 0; i < localization.size(); i++) {
			action.append(localization.get(i));
			
			if ((i + 1) < localization.size()) {
				action.append("', '");
			}
		}
		action.append("'], ");
		
		action.append(getJavaScriptAction(actionAfterUpload));
		action.append(", ");
		action.append(getJavaScriptAction(actionAfterCounterReset));

		action.append(");");
		
		return getActionToLoadFilesAndExecuteCustomAction(action.toString());
	}
	
	private String getJavaScriptAction(String action) {
		if (action == null) {
			return "null";
		}
		
		StringBuffer script = new StringBuffer();
		if (action.indexOf("\"") != -1) {
			action = action.replaceAll("\"", "'");
		}
		if (action.indexOf("'") != -1) {
			String[] actionParts = action.split("'");
			StringBuffer changedAction = new StringBuffer();
			for (int i = 0; i < actionParts.length; i++) {
				changedAction.append(actionParts[i]);
				if ((i + 1) < actionParts.length) {
					changedAction.append("\\").append("'");
				}
			}
			action = changedAction.toString();
		}
		script.append("'").append(action).append("'");
		
		return script.toString();
	}
	
	private String getActionToLoadFilesAndExecuteCustomAction(String customAction) {
		Web2Business web2 = ELUtil.getInstance().getBean(Web2Business.SPRING_BEAN_IDENTIFIER);
		List<String> scripts = new ArrayList<String>();
		scripts.add(ContentUtil.getBundle().getVirtualPathWithFileNameString("javascript/FileUploadHelper.js"));
		scripts.add(web2.getBundleURIToYUIScript());
		scripts.add(CoreConstants.DWR_ENGINE_SCRIPT);
		scripts.add("/dwr/interface/FileUploader.js");
		scripts.add("/dwr/interface/FileUploadListener.js");
		
		return PresentationUtil.getJavaScriptLinesLoadedLazily(scripts, customAction);
	}

	public String getActionAfterUpload() {
		return actionAfterUpload;
	}

	public void setActionAfterUpload(String actionAfterUpload) {
		this.actionAfterUpload = actionAfterUpload;
	}

	public String getUploadPath() {
		return uploadPath;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}

	public boolean isZipFile() {
		return zipFile;
	}

	public void setZipFile(boolean zipFile) {
		this.zipFile = zipFile;
	}

	public boolean isExtractContent() {
		return extractContent;
	}

	public void setExtractContent(boolean extractContent) {
		this.extractContent = extractContent;
	}

	public boolean isThemePack() {
		return themePack;
	}

	public void setThemePack(boolean themePack) {
		this.themePack = themePack;
	}

	public boolean isShowProgressBar() {
		return showProgressBar;
	}

	public void setShowProgressBar(boolean showProgressBar) {
		this.showProgressBar = showProgressBar;
	}

	public boolean isShowLoadingMessage() {
		return showLoadingMessage;
	}

	public void setShowLoadingMessage(boolean showLoadingMessage) {
		this.showLoadingMessage = showLoadingMessage;
	}

	public boolean isAllowMultipleFiles() {
		return allowMultipleFiles;
	}

	public void setAllowMultipleFiles(boolean allowMultipleFiles) {
		this.allowMultipleFiles = allowMultipleFiles;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getActionAfterCounterReset() {
		return actionAfterCounterReset;
	}

	public void setActionAfterCounterReset(String actionAfterCounterReset) {
		this.actionAfterCounterReset = actionAfterCounterReset;
	}

}
