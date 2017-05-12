package fr.smile.liferay.elasticsearch.client.service;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import fr.smile.liferay.elasticsearch.client.model.ElasticSearchJsonDocument;
import fr.smile.liferay.elasticsearch.client.model.Index;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This service gives access to the index through useful methods.
 */
@Service
public class IndexService {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(IndexService.class);

    /**
     * Elastic search uid field.
     */
    public static final String ELASTIC_SEARCH_UID = "uid";

    /**
     * Elasticserch query uid.
     */
    public static final String ELASTIC_SEARCH_QUERY_UID = ELASTIC_SEARCH_UID + StringPool.COLON;

    /** The client. */
    @Autowired
    private Client client;

    /**
     * Checks if index exists.
     *
     * @param index index
     *
     * @return true, if liferay index exists in Elasticsearch server
     */
    public final boolean checkIfIndexExists(final String index) {
        IndicesExistsResponse existsResponse = client.admin().indices()
                .exists(new IndicesExistsRequest(index))
                .actionGet();

        return existsResponse.isExists();
    }

    /**
     * Creates the liferay index in Elasticsearch server with default dynamic mapping template.
     * @param index index
     */
    public final void createIndex(final Index index) {
        try {
            CreateIndexRequestBuilder indexBuilder = client.admin().indices().prepareCreate(index.getName());

            String indexMappings = index.getIndexMappings();
            String indexSettings = index.getIndexSettings();
            if (!StringUtils.isEmpty(indexMappings)) {
                JSONObject jsonMappings = new JSONObject(indexMappings);
                JSONArray jsonMappingsJSONArray = jsonMappings.getJSONArray("mappings");
                for (int i = 0; i < jsonMappingsJSONArray.length(); i++) {
                    JSONObject obj = jsonMappingsJSONArray.getJSONObject(i);
                    String type = obj.names().getString(0);
                    String mapping = obj.getJSONObject(type).toString();
                    indexBuilder.addMapping(type, mapping);
                }
            }

            if (indexSettings != null && indexSettings.length() > 0) {
                indexBuilder.setSettings(indexSettings);
            }

            CreateIndexResponse createIndexResponse = indexBuilder.execute().actionGet();

            LOGGER.info("Index created with dynamic template mapping provided, Result:"
                    + createIndexResponse.isAcknowledged());
        } catch (Exception e) {
            LOGGER.error("Failed to load file for elasticsearch mapping settings", e);
        }
    }

    /**
     * Update mappings for a specific Index.
     * @param indexName the index name
     * @param indexMappings the mappings
     * @return <true> if update is successfully done, <false> otherwise
     */
    public final boolean updateIndexMappings(final String indexName, String indexMappings) {
        try {
            if (checkIfIndexExists(indexName) && !StringUtils.isEmpty(indexMappings)) {
                PutMappingRequestBuilder mappingBuilder = client.admin().indices().preparePutMapping(indexName);
                JSONObject jsonMappings = new JSONObject(indexMappings);
                JSONArray jsonMappingsJSONArray = jsonMappings.getJSONArray("mappings");
                for (int i = 0; i < jsonMappingsJSONArray.length(); i++) {
                    JSONObject obj = jsonMappingsJSONArray.getJSONObject(i);
                    String type = obj.names().getString(0);
                    String mapping = obj.getJSONObject(type).toString();
                    mappingBuilder.setType(type).setSource(mapping).get();
                }
                return true;
            }
        } catch (ElasticsearchException | JSONException e) {
            LOGGER.error("Error while updating mappings for index " + indexName + ":", e);
        }
        return false;
    }

    /**
     * Update mappings for a specific Index.
     * @param indexName the index name
     * @param indexSettings the settings
     * @return <true> if update is successfully done, <false> otherwise
     */
    public final boolean updateIndexSettings(final String indexName, String indexSettings) {
        try {
            if (checkIfIndexExists(indexName) && !StringUtils.isEmpty(indexSettings)) {
                IndicesAdminClient indices = client.admin().indices();

                indices.prepareClose(indexName).get();
                indices.prepareUpdateSettings(indexName).setSettings(indexSettings).get();
                indices.prepareOpen(indexName).get();
                indices.prepareRefresh(indexName).get();
                return true;
            }
        } catch (ElasticsearchException e) {
            LOGGER.error("Error while updating settings for index " + indexName + ":", e);
        }
        return false;
    }

    /**
     * Get the index mappings.
     * @param indexName index name
     * @return the mappings
     */
    public ImmutableOpenMap<String, MappingMetaData> getMappings(String indexName) {
        if (indexName != null && !indexName.isEmpty()) {
            GetMappingsResponse response = client.admin().indices().prepareGetMappings(indexName).get();
            Iterator<ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>>> it = response.getMappings().iterator();

            while (it.hasNext()) {
                ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> cursor = it.next();
                if (indexName.equalsIgnoreCase(cursor.key)) {
                    return cursor.value;
                }
            }
        }
        return null;
    }

    /**
     * Get the index settings
     * @param indexName index name
     * @return the settings
     */
    public Settings getSettings(String indexName) {
        if (indexName != null && !indexName.isEmpty()) {
            GetSettingsResponse response = client.admin().indices().prepareGetSettings(indexName).get();
            Iterator<ObjectObjectCursor<String, Settings>> it = response.getIndexToSettings().iterator();

            while (it.hasNext()) {
                ObjectObjectCursor<String, Settings> cursor = it.next();
                if (indexName.equalsIgnoreCase(cursor.key)) {
                    return cursor.value;
                }
            }
        }
        return null;
    }

    /**
     * List indices.
     * @return indices
     */
    public final List<Index> listIndices() {
        List<Index> indices = new ArrayList<>();

        IndicesAdminClient indicesAdminClient = client.admin().indices();
        GetIndexResponse indexResponse = indicesAdminClient.getIndex(new GetIndexRequest()).actionGet();
        String[] esIndices = indexResponse.getIndices();

        if (esIndices != null && esIndices.length > 0) {
            for (String esIndex : esIndices) {
                Index index = new Index();
                SearchResponse response = client.prepareSearch(esIndex).setSize(0).get();
                SearchHits hits = response.getHits();
                index.setName(esIndex);
                index.setTotalHits(hits.getTotalHits());
                indices.add(index);
            }
        }

        return indices;
    }

    /**
     * Get index by name.
     * @param indexName index name
     * @return an instance of {@link Index}
     */
    public final Index getIndex(String indexName) {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        GetIndexResponse indexResponse = indicesAdminClient.getIndex(new GetIndexRequest()).actionGet();
        String[] esIndices = indexResponse.getIndices();

        if (esIndices != null && esIndices.length > 0) {
            for (String esIndex : esIndices) {
                if (esIndex.equalsIgnoreCase(indexName)) {
                    Index index = new Index();
                    SearchResponse response = client.prepareSearch(esIndex).setSize(0).get();
                    SearchHits hits = response.getHits();
                    index.setName(esIndex);
                    index.setTotalHits(hits.getTotalHits());
                    return index;
                }
            }
        }
        return null;
    }

    /**
     * Count number of documents in index.
     * @param indexName index name
     * @return the number of documents in index
     */
    public final long countDocument(String indexName) {
        SearchResponse response = client.prepareSearch(indexName).setSize(0).get();
        SearchHits hits = response.getHits();
        return hits.getTotalHits();
    }

    /**
     * A method to persist Liferay index to Elasticsearch server document.
     *
     * @param index index
     * @param esDocument
     *            the json document
     */
    public final void writeDocument(final Index index, final ElasticSearchJsonDocument esDocument) {

        try {
            if (esDocument.isError()) {
                LOGGER.warn("Coudln't store document in index. Error..." + esDocument.getErrorMessage());
            } else {
                IndexResponse response = client.prepareIndex(
                        index.getName(),
                        esDocument.getIndexType(),
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
     * Remove document from index.
     * @param uid document uid
     * @param index index
     */
    public final void removeDocument(final String uid, final String index) {

        try {
            /* Don't handle plugin deployment documents, skip them */

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
                        index,
                        hit.getType(),
                        hit.getId())
                        .get();

                if (deleteResponse.status() == RestStatus.FOUND) {
                    LOGGER.debug("Document deleted successfully with id : " + hit.getId());
                } else {
                    LOGGER.debug("Document with id : " + hit.getId() + "is not found");
                }
            }
        } catch (NoNodeAvailableException noNodeEx) {
            LOGGER.error("No node available:" + noNodeEx.getDetailedMessage());
        }
    }
}
