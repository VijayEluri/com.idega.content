/*
 * $Id: ContentItemViewerRenderer.java,v 1.2 2005/03/05 18:45:56 gummi Exp $
 * Created on 16.2.2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package com.idega.content.renderkit;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import com.idega.content.presentation.ContentItemViewer;
import com.idega.util.RenderUtils;
import com.idega.webface.renderkit.ContainerRenderer;


/**
 * 
 *  Last modified: $Date: 2005/03/05 18:45:56 $ by $Author: gummi $
 * 
 * @author <a href="mailto:gummi@idega.com">Gudmundur Agust Saemundsson</a>
 * @version $Revision: 1.2 $
 */
public class ContentItemViewerRenderer extends ContainerRenderer {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.faces.render.Renderer#encodeChildren(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent)
	 */
	public void encodeChildren(FacesContext ctx, UIComponent comp) throws IOException {
		if (!comp.isRendered()) {
			return;
		}
		
		ContentItemViewer viewer = (ContentItemViewer)comp;
		
		renderHeader(ctx,viewer);

		boolean renderToolbarAbove = false;
		if(renderToolbarAbove){
			renderToolbar(ctx,viewer);
		}
		
		renderFields(ctx, viewer);
		
		renderDetailsCommand(ctx,viewer);
		
		if(!renderToolbarAbove){
			renderToolbar(ctx,viewer);
		}
		
		renderFooter(ctx,viewer);
	}
	
	
	
	

	public void renderFields(FacesContext context, ContentItemViewer viewer) throws IOException {
		String attr[] = viewer.getViewerFieldNames();
		for (int i = 0; i < attr.length; i++) {
			RenderUtils.renderFacet(context,viewer,viewer.getFacetName(attr[i]));
		}
	}

	/**
	 * @param ctx
	 * @param viewer
	 * @throws IOException
	 */
	public void renderFooter(FacesContext ctx, ContentItemViewer viewer) throws IOException {
		RenderUtils.renderChild(ctx,viewer.getViewerFacetWrapper(ContentItemViewer.FACET_ITEM_FOOTER));
	}

	/**
	 * @param ctx
	 * @param viewer
	 * @throws IOException
	 */
	public void renderHeader(FacesContext ctx, ContentItemViewer viewer) throws IOException {
		RenderUtils.renderChild(ctx,viewer.getViewerFacetWrapper(ContentItemViewer.FACET_ITEM_HEADER));
	}

	/**
	 * @param ctx
	 * @param comp
	 * @throws IOException
	 */
	public void renderDetailsCommand(FacesContext ctx, ContentItemViewer viewer) throws IOException {
		RenderUtils.renderChild(ctx,viewer.getViewerFacetWrapper(ContentItemViewer.FACET_ITEM_DETAILS_COMMAND));
	}

	/**
	 * @param ctx
	 * @param comp
	 * @throws IOException
	 */
	public void renderToolbar(FacesContext ctx, ContentItemViewer viewer) throws IOException {
		RenderUtils.renderChild(ctx,viewer.getViewerFacetWrapper(ContentItemViewer.FACET_TOOLBAR));
	}

	
}