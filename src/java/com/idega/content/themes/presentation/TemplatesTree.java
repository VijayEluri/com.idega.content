package com.idega.content.themes.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import com.idega.content.business.ContentConstants;
import com.idega.core.builder.business.BuilderService;
import com.idega.core.data.ICTreeNode;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading2;
import com.idega.presentation.text.Heading4;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.ui.GenericButton;
import com.idega.util.CoreConstants;

public class TemplatesTree extends Block {

	@SuppressWarnings("unchecked")
	@Override
	public void main(IWContext iwc) {
		Layer container = new Layer();
		container.setId("templatesTreeContainerInLucid");
		container.setStyleClass("templatesTreeContainerInLucidStyle");
		add(container);
		
		IWBundle iwb = getBundle(iwc);
		IWResourceBundle iwrb = iwb.getResourceBundle(iwc);
		
		BuilderService builder = null;
		try {
			builder = getBuilderService(iwc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (builder == null) {
			container.add(new Heading2(iwrb.getLocalizedString("can_not_get_templates_tree", "Error: can not display templates.")));
			return;
		}
		
		Collection topLevelTemplates = builder.getTopLevelTemplates(iwc);
		if (topLevelTemplates == null || topLevelTemplates.size() == 0) {
			container.add(new Heading4(iwrb.getLocalizedString("there_are_no_templates", "There are no templates in system")));
			return;
		}
		
		Lists templates = new Lists();
		container.add(templates);
		
		addTemplatesToTree(templates, topLevelTemplates, iwc.getCurrentLocale(), iwb);
		
		Layer buttons = new Layer();
		buttons.setStyleClass("webfaceButtonLayer");
		container.add(buttons);
		GenericButton createTemplate = new GenericButton("createChildTemplate", iwrb.getLocalizedString("create_child_template", "Create child template"));
		createTemplate.setStyleClass("createChildTemplateForCurrentTemplateButtonInLucidStyle");
		createTemplate.setToolTip(iwrb.getLocalizedString("create_child_template_for_current_template", "Create child template for current template"));
		buttons.add(createTemplate);
	}
	
	@SuppressWarnings("unchecked")
	private void addTemplatesToTree(Lists tree, Collection templates, Locale l, IWBundle iwb) {
		if (templates == null || templates.size() == 0) {
			return;
		}
		
		tree.setStyleClass("templatesTreeInLucidStyle");
		
		Object o = null;
		ICTreeNode template = null;
		String name = null;
		Collection templateChildren = null;
		for (Iterator it = templates.iterator(); it.hasNext();) {
			o = it.next();
			
			if (o instanceof ICTreeNode) {
				name = null;
				templateChildren = null;
				
				template = (ICTreeNode) o;
				
				name = template.getNodeName(l);
				if (name == null || CoreConstants.EMPTY.equals(name)) {
					name = template.getNodeName();
				}
				
				ListItem item = new ListItem();
				String imageUri = iwb.getVirtualPathWithFileNameString("images/template.png");
				Image icon = new Image(imageUri);
				item.add(icon);
				
				Link templateName = new Link(name);
				templateName.setURL("javascript:void(0);");
				templateName.setStyleClass("templateNameInLucidTemplatesTreeStyle");
				templateName.setMarkupAttribute("templateid", template.getId());
				item.add(templateName);
				tree.add(item);
				
				templateChildren = template.getChildren();
				if (templateChildren != null && templateChildren.size() > 0) {
					icon.setURL(iwb.getVirtualPathWithFileNameString("images/folder_template.png"));
					
					Lists newTree = new Lists();
					item.add(newTree);
					
					addTemplatesToTree(newTree, templateChildren, l, iwb);
				}
			}
		}
	}
	
	@Override
	public String getBundleIdentifier() {
		return ContentConstants.IW_BUNDLE_IDENTIFIER;
	}
	
}