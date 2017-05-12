package fr.smile.liferay.elasticsearch.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import fr.smile.liferay.elasticsearch.client.model.Index;
import fr.smile.liferay.elasticsearch.client.model.IndexMappings;
import fr.smile.liferay.elasticsearch.client.model.IndexSettings;
import fr.smile.liferay.elasticsearch.client.service.IndexService;
import fr.smile.liferay.elasticsearch.management.IndexAction;
import fr.smile.liferay.elasticsearch.management.reindex.IndexingStatus;
import fr.smile.liferay.elasticsearch.management.reindex.Reindexer;
import fr.smile.liferay.elasticsearch.management.util.JsonSender;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexNotFoundException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Manages the Elastic Search Index through Liferay Portlet Controller.
 */
@Controller
@RequestMapping("VIEW")
public class IndexController {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(IndexController.class);

    /**
     * Default view name.
     */
    private static final String DEFAULT_VIEW = "view";

    /**
     * Index param name.
     */
    private static final String INDEX_PARAM = "index";

    /**
     * Action param name.
     */
    private static final String ACTION_PARAM = "action";

    /**
     * File param name.
     */
    private static final String FILE_PARAM = "file";

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

    @ResourceMapping
    public final void doAsyncAction(final ResourceRequest resourceRequest, final ResourceResponse resourceResponse)
            throws SystemException, IOException {
        String indexName = ParamUtil.getString(resourceRequest, INDEX_PARAM);
        String actionValue = ParamUtil.getString(resourceRequest, ACTION_PARAM);

        try {
            if (indexService.checkIfIndexExists(indexName)) {
                IndexAction indexAction = IndexAction.get(actionValue);
                if (indexAction != null) {
                    switch (indexAction) {
                        case REINDEX: doReindex(indexName, resourceResponse); break;
                        case GET_MAPPINGS: doGetMappings(indexName, resourceResponse); break;
                        case GET_SETTINGS: doGetSettings(indexName, resourceResponse); break;
                        case CHECK_STATUS: doCheckStatus(indexName, resourceResponse); break;
                    }
                } else {
                    throw new SystemException("Action " + actionValue + " is not correct.");
                }
            }
        } catch (IndexNotFoundException e) {
            LOGGER.error("Index " + indexName + " does not exist.");
        } catch (ElasticsearchException e) {
            LOGGER.error("An error occured while communicating with ElasticSearch", e);
        }
    }

    @ResourceMapping("reindex")
    public final void doReindex(final String indexName, final ResourceResponse resourceResponse) throws IOException {
        Reindexer reindexer = Reindexer.getInstance();
        if (reindexer.getCurrentStatus() == IndexingStatus.AVAILABLE) {
            reindexer.reindexAll();
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("totalItems", reindexer.getTotalDocuments());
        jsonObject.put("processedItems", reindexer.getProcessedDocuments());
        jsonObject.put("status", reindexer.getCurrentStatus().name());
        JsonSender.send(resourceResponse, jsonObject);
    }

    @ResourceMapping("getMappings")
    public final void doGetMappings(final String indexName, final ResourceResponse resourceResponse) throws IOException {
        ImmutableOpenMap<String, MappingMetaData> mappings = indexService.getMappings(indexName);

        if (mappings != null && !mappings.isEmpty()) {
            IndexMappings indexMappings = IndexMappings.from(indexName, mappings);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(indexMappings);
            JsonSender.send(resourceResponse, json);
        } else {
            LOGGER.error("Mappings are empty for index " + indexName);
        }
    }

    @ResourceMapping("getSettings")
    public final void doGetSettings(final String indexName, final ResourceResponse resourceResponse) throws IOException {
       Settings settings = indexService.getSettings(indexName);

        if (settings != null && !settings.isEmpty()) {
            IndexSettings indexSettings = IndexSettings.from(indexName, settings);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(indexSettings);
            JsonSender.send(resourceResponse, json);
        } else {
            LOGGER.error("Settings are empty for index " + indexName);
        }
    }

    @ResourceMapping("checkStatus")
    public final void doCheckStatus(final String indexName, final ResourceResponse resourceResponse) {

    }

    @ActionMapping("updateMappings")
    public final void doUpdateMappings(final ActionRequest actionRequest, final ActionResponse actionResponse) throws IOException {
        UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(actionRequest);
        InputStream in = uploadRequest.getFileAsStream(FILE_PARAM);
        String indexName = ParamUtil.getString(actionRequest, INDEX_PARAM);

        if (in != null) {
            String indexMappings = CharStreams.toString(new InputStreamReader(in));
            if (indexService.updateIndexMappings(indexName, indexMappings)) {
                SessionMessages.add(actionRequest, "update-mappings-success");
            } else {
                SessionErrors.add(actionRequest, "update-mappings-error");
            }
        } else {
            LOGGER.error("No mappings file found.");
            SessionErrors.add(actionRequest, "no-mappings-error");
        }
    }

    @ActionMapping("updateSettings")
    public final void doUpdateSettings(final ActionRequest actionRequest, final ActionResponse actionResponse) throws IOException {
        UploadPortletRequest uploadRequest = PortalUtil.getUploadPortletRequest(actionRequest);
        InputStream in = uploadRequest.getFileAsStream(FILE_PARAM);
        String indexName = ParamUtil.getString(actionRequest, INDEX_PARAM);

        if (in != null) {
            String indexSettings = CharStreams.toString(new InputStreamReader(in));
            if (indexService.updateIndexSettings(indexName, indexSettings)) {
                SessionMessages.add(actionRequest, "update-settings-success");
            } else {
                SessionErrors.add(actionRequest, "update-settings-error");
            }
        } else {
            LOGGER.error("No settings file found.");
            SessionErrors.add(actionRequest, "no-settings-error");
        }
    }

}
