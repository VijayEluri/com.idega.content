if (!FileUploadHelper) var FileUploadHelper = {};

FileUploadHelper.allUploadedFiles = [];
FileUploadHelper.uploadedFiles = null;

FileUploadHelper.properties = {
	id: null,
	showProgressBar: true,
	showMessage: true,
	zipFile: false,
	formId: null,
	progressBarId: null,
	localizations: {
		UPLOADING_FILE_PROGRESS_BOX_TEXT: 'Uploading file',
		UPLOADING_FILE_PLEASE_WAIT_PROGRESS_BOX_TEXT: 'completed, please wait...',
		UPLOADING_FILE_PROGRESS_BOX_FILE_UPLOADED_TEXT: 'Upload was successfully finished.',
		UPLOADING_FILE_MESSAGE: 'Uploading...',
		UPLOADING_FILE_INVALID_TYPE_MESSAGE: 'Unsupported file type! Only zip files allowed',
		UPLOADING_FILE_FAILED: 'Sorry, some error occurred - unable to upload file(s). Please, try again'
	},
	actionAfterUpload: null,
	actionAfterCounterReset: null,
	uploadId: null,
	autoUpload: false,
	showUploadedFiles: false,
	fakeFileDeletion: false,
	actionAfterUploadedToRepository: null,
	stripNonRomanLetters: false,
	maxSize: 1073741824						//	1 GB
}

FileUploadHelper.setProperties = function(properties) {
	FileUploadHelper.properties = properties;
	FileUploadHelper.properties.maxSize--;
	FileUploadHelper.properties.maxSize++;
}

FileUploadHelper.uploadFiles = function() {
	var inputs = getInputsForUpload(FileUploadHelper.properties.id);
	var files = getFilesValuesToUpload(inputs, FileUploadHelper.properties.zipFile, FileUploadHelper.properties.localizations.UPLOADING_FILE_INVALID_TYPE_MESSAGE);
	if (files.length == 0) {
		return false;
	}
	
	var form = null;
	if (FileUploadHelper.properties.formId != null) {
		form = document.getElementById(FileUploadHelper.properties.formId);
	}
	if (form == null) {
		form = findElementInPage(document.getElementById(FileUploadHelper.properties.id), 'FORM');
	}
	if (form == null) {
		return false;
	}
	if (!form.id) {
		var tempFormId = 'id' + new Date().getTime() + '_uploadForm';
		form.setAttribute('id', tempFormId);
		FileUploadHelper.properties.formId = tempFormId;
	}
	form.setAttribute('enctype', 'multipart/form-data');
	
	if (FileUploadHelper.properties.showMessage || FileUploadHelper.properties.autoUpload) {
		showLoadingMessage(FileUploadHelper.properties.localizations.UPLOADING_FILE_MESSAGE);
	}
	
	var uploadHandler = {
		upload: function(o) {
			var response = o.responseText;
			if (response == null) {
				humanMsg.displayMsg(FileUploadHelper.properties.localizations.UPLOADING_FILE_FAILED);
				return;
			}
			
			var key = 'web2FilesUploaderFilesListStarts';
			response = response.substring(response.indexOf(key) + key.length);
			response = response.replace('</pre>', '');
			
			FileUploadHelper.uploadedFiles = response.split(',');
			if (FileUploadHelper.uploadedFiles == null) {
				humanMsg.displayMsg(FileUploadHelper.properties.localizations.UPLOADING_FILE_FAILED);
				return;
			} else if (FileUploadHelper.uploadedFiles[0].indexOf('error=') != -1) {
				var firstValue = FileUploadHelper.uploadedFiles[0];
				var customMessage = firstValue.substr('error='.length);
				for (var i = 1; i < FileUploadHelper.uploadedFiles.length; i++) {
					customMessage += FileUploadHelper.uploadedFiles[i];
					if (i + 1 < FileUploadHelper.uploadedFiles.length)
						customMessage += ',';
				}
				closeAllLoadingMessages();
				humanMsg.displayMsg(customMessage);
				return;
			} else {
				for (var i = 0; i < FileUploadHelper.uploadedFiles.length; i++) {
					FileUploadHelper.allUploadedFiles.push(FileUploadHelper.getRealUploadedFile(FileUploadHelper.uploadedFiles[i]));
				}
			}
			
			executeUserDefinedActionsAfterUploadFinished(FileUploadHelper.properties.actionAfterUpload);
			
			FileUploadHelper.executeActionAfterUploadedToRepository(inputs);
		}
	};
	
	var progressBarId = FileUploadHelper.properties.progressBarId;
	FileUploadListener.resetFileUploaderCounters(FileUploadHelper.properties.uploadId, FileUploadHelper.properties.maxSize, {
		callback: function(result) {
			YAHOO.util.Connect.setForm(FileUploadHelper.properties.formId, true);
			YAHOO.util.Connect.asyncRequest('POST', '/servlet/ContentFileUploadServlet', uploadHandler);
			
			if (FileUploadHelper.properties.showProgressBar) {
				jQuery('#' + progressBarId).parent().hide('fast', function() {
					jQuery('#' + progressBarId).progressBar(0, {showText: true});
					jQuery('#' + progressBarId).css('display', 'block');
					showUploadInfoInProgressBar(progressBarId, FileUploadHelper.properties.actionAfterCounterReset);
				});
			}
		}
	});
}

FileUploadHelper.executeActionAfterUploadedToRepository = function(inputs) {
	if (FileUploadHelper.properties.uploadId == null) {
		FileUploadHelper.prepareToShowUploadedFiles(inputs);
		
		closeAllLoadingMessages();
		return;
	}
	
	FileUploadListener.isUploadSuccessful(FileUploadHelper.properties.uploadId, {
		callback: function(result) {
			if (result) {
				FileUploadHelper.prepareToShowUploadedFiles(inputs);
				
				closeAllLoadingMessages();
				executeUserDefinedActionsAfterUploadFinished(FileUploadHelper.properties.actionAfterUploadedToRepository);
			} else if (result == null) {
				window.setTimeout(FileUploadHelper.executeActionAfterUploadedToRepository, 250);
			} else {
				closeAllLoadingMessages();
				humanMsg.displayMsg(FileUploadHelper.properties.localizations.UPLOADING_FILE_FAILED);
			}
		}
	});
}

FileUploadHelper.prepareToShowUploadedFiles = function(inputs) {
	if (FileUploadHelper.properties.showUploadedFiles) {
		FileUploadHelper.showUploadedFiles(FileUploadHelper.properties.fakeFileDeletion);
	}
		
	FileUploadHelper.uploadedFiles = null;
	var inputsToRemove = new Array();
	if (inputs != null) {
		for (var i = 0; i < inputs.length; i++) {
			jQuery(inputs[i]).attr('value', '');
			inputs[i].setAttribute('value', '');
			inputs[i].value = '';
			
			if (i > 0) {
				inputsToRemove.push(inputs[i]);
			}
		}
		jQuery.each(inputsToRemove, function() {
			var input = jQuery(this);
			input.parent().hide('normal', function() {
				input.parent().remove();
			})
		});
	}
}

FileUploadHelper.showUploadedFiles = function(fakeFileDeletion) {
	var filesList = jQuery('div.fileUploadViewerUploadedFilesContainerStyle');
	if (filesList == null || filesList.length == 0) {
		jQuery('div.fileUploadViewerMainLayerStyle').append('<div class=\'spacer\'/><div class=\'fileUploadViewerUploadedFilesContainerStyle\' />');
		filesList = jQuery('div.fileUploadViewerUploadedFilesContainerStyle');
	}
	
	var uploadPath = FileUploadHelper.getUploadPath();
	FileUploader.getUploadedFilesList(FileUploadHelper.allUploadedFiles, uploadPath, fakeFileDeletion, FileUploadHelper.properties.stripNonRomanLetters, {
		callback: function(results) {
			if (results == null) {
				return;
			}
			
			var component = results[results.length - 1];
			filesList.hide('fast', function() {
				filesList.empty().append(component).show('fast');
			});
		}
	});
}

FileUploadHelper.deleteUploadedFile = function(id, file, fakeFileDeletion) {
	FileUploader.deleteFile(file, fakeFileDeletion, {
		callback: function(result) {
			if (result == null) {
				return;
			}
			
			if (result.id == 'false') {
				humanMsg.displayMsg(result.value);
				return;
			}
			
			humanMsg.displayMsg(result.value);
			var container = jQuery('#' + id).parent();
			jQuery('#' + id).hide('normal', function() {
				jQuery('#' + id).remove();
				if (jQuery('li', container).length == 0) {
					container.parent().remove();
				}
			});
			
			removeElementFromArray(FileUploadHelper.allUploadedFiles, file);
		}
	});
}

FileUploadHelper.getUploadPath = function() {
	return jQuery('input.web2FileUploaderPathValue[type=\'hidden\'][name=\'web2FileUploaderPathValue\']').attr('value');
}

FileUploadHelper.getRealUploadedFile = function(file) {
	var uploadPath = FileUploadHelper.getUploadPath();
	if (uploadPath.substring(uploadPath.length - 1) != '/') {
		uploadPath += '/';
	}
	
	var index = file.lastIndexOf('/');
	if (index != -1) {
		file = file.substring(index + 1);
	}
	index = file.lastIndexOf('\\');
	if (index != -1) {
		file = file.substring(index + 1);
	}
	
	return uploadPath + file;
}

FileUploadHelper.removeAllUploadedFiles = function(fakeFileDeletion) {
	if (FileUploadHelper.allUploadedFiles == null || FileUploadHelper.allUploadedFiles.length == 0) {
		return;
	}
	
	LazyLoader.loadMultiple(['/dwr/engine.js', '/dwr/interface/FileUploader.js'], function() {
		FileUploader.deleteFiles(FileUploadHelper.allUploadedFiles, fakeFileDeletion, {
			callback: function(result) {
				if (result == null) {
					return;
				}
				
				if (result.id == 'false') {
					humanMsg.displayMsg(result.value);
					return;
				}
				
				jQuery('div.fileUploadViewerUploadedFilesContainerStyle').hide('fast', function() {
					jQuery('div.fileUploadViewerUploadedFilesContainerStyle').remove();
				});
				FileUploadHelper.allUploadedFiles = [];
			}
		});
	}, null);
}

function showUploadInfoInProgressBar(progressBarId, actionAfterCounterReset) {
	jQuery('#' + progressBarId).parent().show('normal', function() {
		fillProgressBoxWithFileUploadInfo(progressBarId, actionAfterCounterReset);
	});
}

function fillProgressBoxWithFileUploadInfo(progressBarId, actionAfterCounterReset) {
	FileUploadListener.getFileUploadStatus(FileUploadHelper.properties.uploadId, {
		callback: function(status) {
			if (status == null) {
				status = '0';
			}
			
			jQuery('#' + progressBarId).progressBar(status == '-1' ? '0' : status);

			if (status == '100') {
				FileUploadHelper.reportUploadStatus(progressBarId, actionAfterCounterReset,
				FileUploadHelper.properties.localizations.UPLOADING_FILE_PROGRESS_BOX_FILE_UPLOADED_TEXT);
				return false;
			} else if (status == '-1') {
				FileUploadHelper.reportUploadStatus(progressBarId, actionAfterCounterReset, FileUploadHelper.properties.localizations.UPLOADING_FILE_FAILED);
				return false;
			} else {
				var functionWhileUploading = function() {
					fillProgressBoxWithFileUploadInfo(progressBarId, actionAfterCounterReset);
				}
				window.setTimeout(functionWhileUploading, 750);
			}
		}
	});
}

FileUploadHelper.reportUploadStatus = function(progressBarId, actionAfterCounterReset, text) {
	jQuery('#' + progressBarId).html(text);
	var functionAfterCompletedUpload = function() {
		resetFileUploaderCounterAfterTimeOut(progressBarId, actionAfterCounterReset);
	}
	window.setTimeout(functionAfterCompletedUpload, 2000);
}

function resetFileUploaderCounterAfterTimeOut(progressBarId, customActionAfterCounterReset) {
	FileUploadListener.resetFileUploaderCounters(FileUploadHelper.properties.uploadId, FileUploadHelper.properties.maxSize, {
		callback:function(result) {
			jQuery('#' + progressBarId).hide('normal', function() {
				var parentContainer = jQuery('#' + progressBarId).parent();
				jQuery('#' + progressBarId).remove();
				
				jQuery(parentContainer).hide('fast', function() {
					jQuery(parentContainer).append('<span id=\''+progressBarId+'\' class=\'progressBar\' style=\'display: none;\'/>');
					jQuery('#' + progressBarId).progressBar(0, { showText: true});
					executeUserDefinedActionsAfterUploadFinished(customActionAfterCounterReset);
				});
			});	
		}
	});
}

function findElementInPage(container, tagName) {
	if (container == null || tagName == null) {
		return null;
	}
	
	if (container.tagName) {
		if (container.tagName == tagName) {
			return container;
		}
	}
	
	var children = container.childNodes;
	if (children != null) {
		var element = null;
		for (var i = 0; i < children.length; i++) {
			element = children[i];
			if (element.tagName) {
				if (element.tagName == tagName) {
					return element;
				}
			}
		}
	}
	
	return findElementInPage(container.parentNode, tagName);
}

function executeUserDefinedActionsAfterUploadFinished(actionAfterUpload) {
	if (actionAfterUpload != null) {
		var customFunction = function() {
			window.eval(actionAfterUpload);
		}
		customFunction();
	}
}

function removeFileInput(id, message) {
	var confirmed = false;
	var value = document.getElementById(id).getValue();
	if (value == null || value == '') {
		confirmed = true;
	}
	
	if (!confirmed) {
		confirmed = window.confirm(message);
	}
	if (confirmed) {
		var container = document.getElementById(id);
		var parentContainer = container.parentNode;
		parentContainer.removeChild(container);
	}
}

function getInputsForUpload(id) {
	var inputs = new Array();
	jQuery.each(jQuery('input.fileUploadInputStyle', jQuery('#' + id)), function() {
		inputs.push(this);
	});
	return inputs;
}

function getFilesValuesToUpload(inputs, zipFile, invalidTypeMessage) {
	var files = new Array();
	for (var i = 0; i < inputs.length; i++) {
		var file = document.getElementById(inputs[i].id);
		var fileValue = file.value;
		if (fileValue != null && fileValue != '') {
			if (zipFile) {
				if (isCorrectFileType(file.id, 'zip', null, invalidTypeMessage)) {
					files.push(fileValue);
				}
			}
			else {
				files.push(fileValue);
			}
		}
	}
	
	return files;
}

function addFileInputForUpload(id, message, className, showProgressBar, addjQuery, autoAddFileInput, autoUpload) {
	var foundEmptyInput = false;
	var currentInputs = $$('input.' + className, id);
	if (currentInputs != null) {
		var valueProperty = null;
		for (var i = 0; (i < currentInputs.length && !foundEmptyInput); i++) {
			valueProperty = $(currentInputs[i]).getProperty('value');
			foundEmptyInput = (valueProperty == null || valueProperty == '');
		}
	}
	if (foundEmptyInput) {
		return;
	}
	
	showLoadingMessage(message);
	
	FileUploader.getRenderedFileInput(id, showProgressBar, addjQuery, autoAddFileInput, autoUpload, {
		callback: function(component) {
			closeAllLoadingMessages();
			
			var container = document.getElementById(id);
			if (component == null || container == null) {
				return false;
			}
			
			insertNodesToContainer(component, container);
		}
	});
}

FileUploadHelper.changeUploadPath = function(newUploadPath, className) {
	jQuery.each(jQuery('input.' + className), function() {
		jQuery(this).attr('value', newUploadPath);
	});
}

FileUploadHelper.reRenderComponent = function(id) {
	FileUploader.getRenderedComponent(id, {
		callback: function(componentHTML) {
			if (componentHTML == null) {
				reloadPage();
				return false;
			}
			
			var componentToReplace = jQuery('#' + id);
			if (componentToReplace == null || componentToReplace.length == 0) {
				reloadPage();
				return false;
			}
			
			componentToReplace.replaceWith(jQuery(componentHTML));
		}
	});
}