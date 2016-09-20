package fr.smile.liferay.elasticsearch.management.controller;

import com.liferay.util.bridges.mvc.MVCPortlet;
import fr.smile.liferay.elasticsearch.client.model.Index;
import fr.smile.liferay.elasticsearch.client.service.IndexService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author marem
 * @since 16/11/15.
 */
public class IndexController extends MVCPortlet {

    /**
     * Index service.
     */
    private IndexService indexService;

    @Override
    public final void doView(final RenderRequest renderRequest, final RenderResponse renderResponse)
            throws IOException, PortletException {

        List<Index> indices = getIndexService().listIndices();
        renderRequest.setAttribute("indices", indices);
        super.doView(renderRequest, renderResponse);
    }

    @Override
    public final void serveResource(final ResourceRequest resourceRequest, final ResourceResponse resourceResponse)
            throws IOException, PortletException {
        super.serveResource(resourceRequest, resourceResponse);
    }

    /**
     * Get index service.
     * @return index service
     */
    private IndexService getIndexService() {
        if (indexService == null) {
            ApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
            indexService = (IndexService) context.getBean("indexService");
        }

        return indexService;
    }
}
