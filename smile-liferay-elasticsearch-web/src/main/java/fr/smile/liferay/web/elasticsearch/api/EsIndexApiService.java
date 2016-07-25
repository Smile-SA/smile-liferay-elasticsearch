package fr.smile.liferay.web.elasticsearch.api;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import fr.smile.liferay.web.elasticsearch.model.document.ElasticSearchJsonDocument;
import fr.smile.liferay.web.elasticsearch.model.index.LiferayIndex;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;

/**
 * @author marem
 * @since 30/12/15.
 */
@Service
public class EsIndexApiService {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(EsIndexApiService.class);

    /**
     * Elastic search uid field.
     */
    public static final String ELASTIC_SEARCH_UID = "uid";

    /**
     * Elasticserch query uid.
     */
    public static final String ELASTIC_SEARCH_QUERY_UID = ELASTIC_SEARCH_UID + StringPool.COLON;

    /**
     * War type.
     */
    public static final String WAR = "/war";

    /** The client. */
    @Autowired
    private Client client;

    /** Liferay index. */
    @Autowired
    private LiferayIndex index;

    /**
     * Creates the liferay index in Elasticsearch server with default dynamic
     * mapping template.
     * @param index index
     * @param mappings mappings
     * @param settings settings
     */
    public final void createIndex(final String index, final String mappings, final String settings) {
        try {
            CreateIndexRequestBuilder indexBuilder = client.admin().indices().prepareCreate(index);

            if (!StringUtils.isEmpty(mappings)) {
                JSONObject jsonMappings = new JSONObject(mappings);
                JSONArray  jsonMappingsJSONArray = jsonMappings.getJSONArray("mappings");
                for (int i = 0; i < jsonMappingsJSONArray.length(); i++) {
                    JSONObject obj = jsonMappingsJSONArray.getJSONObject(i);
                    String type = obj.names().getString(0);
                    String mapping = obj.getJSONObject(type).toString();
                    indexBuilder.addMapping(type, mapping);
                }
            }

            if (settings != null && settings.length() > 0) {
                indexBuilder.setSettings(settings);
            }

            CreateIndexResponse createIndexResponse = indexBuilder.execute().actionGet();

            LOGGER.info("Index created with dynamic template mapping provided, Result:"
                    + createIndexResponse.isAcknowledged());
        } catch (IndexAlreadyExistsException iaeEx) {
            LOGGER.warn("Index already exists, no need to create again....");
        } catch (Exception e) {
            LOGGER.error("Failed to load file for elasticsearch mapping settings", e);
        }
    }

    /**
     * Delete index.
     * @param uid document uid
     * @param index index
     */
    public final void removeDocument(final String uid, final String index) {

        try {
            /** Don't handle plugin deployment documents, skip them */
            if (!uid.endsWith(WAR)) {
                QueryStringQueryBuilder query = QueryBuilders.queryStringQuery(
                        ELASTIC_SEARCH_QUERY_UID + uid
                );

                SearchResponse scrollResp = client
                        .prepareSearch(index)
                        .setQuery(query)
                        .execute().actionGet();

                SearchHits hits = scrollResp.getHits();

                final int entriesToDelete = hits.getHits().length;

                LOGGER.debug("Prepare to delete: " + entriesToDelete + " entries from index");

                for (SearchHit hit : hits) {
                    LOGGER.debug("Deleting entry with id : " + hit.getId());
                    DeleteResponse deleteResponse = client.prepareDelete(
                            this.index.getName(),
                            "LiferayAssetType",
                            hit.getId())
                            .setRefresh(true)
                            .execute()
                            .actionGet();

                    if (deleteResponse.isFound()) {
                        LOGGER.debug("Document deleted successfully with id : " + hit.getId());
                    } else {
                        LOGGER.debug("Document with id : " + hit.getId() + "is not found");
                    }
                }
            }
        } catch (NoNodeAvailableException noNodeEx) {
            LOGGER.error("No node available:" + noNodeEx.getDetailedMessage());
        }
    }

    /**
     * A method to persist Liferay index to Elasticsearch server document.
     *
     * @param esDocument
     *            the json document
     */
    public final void writeDocument(final ElasticSearchJsonDocument esDocument) {

        try {
            if (esDocument.isError()) {
                LOGGER.warn("Coudln't store document in index. Error..." + esDocument.getErrorMessage());
            } else {
                IndexResponse response = client.prepareIndex(
                        index.getName(),
                        //esDocument.getIndexType(),
                        "LiferayAssetType",
                        esDocument.getId()
                ).setSource(esDocument.getJsonDocument()).execute().actionGet();

                LOGGER.debug("Document indexed successfully with Id: " + esDocument.getId()
                        + " ,Type:" + esDocument.getIndexType()
                        + " ,Updated index version:" + response.getVersion());
            }
        } catch (NoNodeAvailableException noNodeEx) {
            LOGGER.error("No node available:" + noNodeEx.getDetailedMessage());
        }
    }

    /**
     * Checks if Liferay index exists.
     *
     * @param index index
     *
     * @return true, if liferay index exists in Elasticsearch server
     */
    public final boolean isLiferayIndexExists(final String index) {
        IndicesExistsResponse existsResponse = client.admin().indices()
                .exists(new IndicesExistsRequest(index))
                .actionGet();

        return existsResponse.isExists();
    }

    /**
     * The Close is run during destroying the spring context. The client object
     * need to be closed to avoid overlock exceptions
     */
    @PreDestroy
    public final void close() {
        LOGGER.info("About to close Client........");
        if (client != null) {
            client.close();
        }
        LOGGER.info("Successfully closed Client........");
    }
}
