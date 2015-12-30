package fr.smile.liferay.elasticsearch.management.controller;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.comparator.PortletLuceneComparator;
import com.liferay.util.bridges.mvc.MVCPortlet;

import javax.portlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author marem
 * @since 16/11/15.
 */
public class IndexController extends MVCPortlet {


    @Override
    public void doView(final RenderRequest renderRequest, final RenderResponse renderResponse) throws IOException, PortletException {
        long companyId = PortalUtil.getCompanyId(renderRequest);
        renderRequest.setAttribute("indexingPortlets", this.getIndexingPortlets(companyId));
        super.doView(renderRequest, renderResponse);
    }

    @Override
    public void serveResource(final ResourceRequest resourceRequest, final ResourceResponse resourceResponse)
            throws IOException, PortletException {
        super.serveResource(resourceRequest, resourceResponse);
    }

    /**
     * Get list of portlets that reindex contents.
     * @param companyId company id
     * @return list
     */
    private List<Portlet> getIndexingPortlets(final long companyId) {

        List<Portlet> indexingPortlets = new ArrayList<Portlet>();
        List<Portlet> portlets;
        try {
            portlets = PortletLocalServiceUtil.getPortlets(companyId);
        } catch (SystemException e) {
            return indexingPortlets;
        }

        portlets = ListUtil.sort(portlets, new PortletLuceneComparator());

        for (Portlet portlet : portlets) {
            if (!portlet.isActive()) {
                continue;
            }

            List<Indexer> indexers = portlet.getIndexerInstances();

            if (indexers == null) {
                continue;
            }

            indexingPortlets.add(portlet);
        }

        return indexingPortlets;
    }
}
