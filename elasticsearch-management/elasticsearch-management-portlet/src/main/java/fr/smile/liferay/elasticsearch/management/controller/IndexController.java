package fr.smile.liferay.elasticsearch.management.controller;

import fr.smile.liferay.elasticsearch.client.model.Index;
import fr.smile.liferay.elasticsearch.client.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.List;

/**
 * Manages the Elastic Search Index through Liferay Portlet Controller.
 */
@Controller
@RequestMapping("VIEW")
public class IndexController {

    /**
     * Default view name.
     */
    private static final String DEFAULT_VIEW = "view";

    /**
     * Index service.
     */
    @Autowired
    private IndexService indexService;

    @RenderMapping
    public final ModelAndView doView(final RenderRequest renderRequest, final RenderResponse renderResponse)
            throws IOException, PortletException {
        List<Index> indices = indexService.listIndices();

        ModelAndView modelAndView = new ModelAndView(DEFAULT_VIEW);
        modelAndView.addObject("indices", indices);
        return modelAndView;
    }

}
