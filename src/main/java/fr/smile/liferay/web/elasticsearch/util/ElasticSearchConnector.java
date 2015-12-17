package fr.smile.liferay.web.elasticsearch.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexAlreadyExistsException;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author marem
 * @since 29/10/15.
 */
public class ElasticSearchConnector {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(ElasticSearchConnector.class);

    /** The client. */
    private Client client = null;

    /**
     * Inits the transport client.
     * @throws UnknownHostException unknown host exception
     * @throws NumberFormatException number format exception
     */
    public final void connectToServer() throws NumberFormatException, UnknownHostException {

        try {
            String esServerHome = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_HOME_PATH);
            String esClusterName = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_CLUSTERNAME);

            if (Validator.isNull(esServerHome) || esServerHome.isEmpty()) {
                throw new ElasticsearchException("Elasticsearch server home folder is not configured...");
            }

            /** Create a settings object with custom attributes and build */
            Settings.Builder settingsBuilder = Settings.settingsBuilder()
                    .put(ElasticSearchIndexerConstants.ES_SETTING_PATH_HOME, esServerHome)
                    .put(ElasticSearchIndexerConstants.ES_SETTING_CLIENT_SNIFF, true);

            if (Validator.isNotNull(esClusterName) && !esClusterName.isEmpty()
                    && !ElasticSearchIndexerConstants.ELASTIC_SEARCH.equalsIgnoreCase(esClusterName)) {
                settingsBuilder.put(ElasticSearchIndexerConstants.ES_SETTING_CLUSTERNAME, esClusterName);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Elasticsearch cluster name is not configured to default:" + esClusterName);
                }
            }

            String csElasticsearchNodes = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_NODE);
            if (Validator.isNull(csElasticsearchNodes) || csElasticsearchNodes.isEmpty()) {
                throw new ElasticsearchException("Elasticsearch server node is not configured...");
            }

            String[] nodeList = csElasticsearchNodes.split(StringPool.COMMA);
            InetSocketTransportAddress[] transportAddresses = new InetSocketTransportAddress[nodeList.length];

            /** Prepare a list of Hosts */
            for (int i = 0; i < nodeList.length; i++) {
                String[] hostnames = nodeList[i].split(StringPool.COLON);
                InetSocketTransportAddress transportAddress = new InetSocketTransportAddress(
                    InetAddress.getByName(hostnames[0]),
                    Integer.parseInt(hostnames[1])
                );
                transportAddresses[i] = transportAddress;
            }
            client = TransportClient.builder().settings(
                    settingsBuilder.build()
            ).build().addTransportAddresses(transportAddresses);
            LOGGER.info("Successfully created Transport client........");
            /**
             * Check if Liferay index already exists, else create one with
             * default mapping The Index creation is one time setup, so it is
             * important to check if index already exists before creation
             */
            if (!isLiferayIndexExists()) {
                createLiferayIndexInESServer();
            }
        } catch (ElasticsearchException configEx) {
            LOGGER.error("Error while connecting to Elasticsearch server:" + configEx.getMessage());
        }
    }

    /**
     * The Close is run during destroying the spring context. The client object
     * need to be closed to avoid overlock exceptions
     */
    public final void close() {
        LOGGER.info("About to close Client........");
        if (client != null) {
            client.close();
        }
        LOGGER.info("Successfully closed Client........");
    }

    /**
     * Gets the client.
     *
     * @return the client
     */
    public final Client getClient() {
        return client;
    }

    /**
     * Checks if Liferay index exists.
     *
     * @return true, if liferay index exists in Elasticsearch server
     */
    private boolean isLiferayIndexExists() {
        IndicesExistsResponse existsResponse = client.admin().indices()
                .exists(new IndicesExistsRequest(ElasticSearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX))
                .actionGet();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Liferay index exists:" + existsResponse.isExists());
        }

        return existsResponse.isExists();
    }

    /**
     * Creates the liferay index in Elasticsearch server with default dynamic
     * mapping template.
     */
    private void createLiferayIndexInESServer() {
        try {
            CreateIndexResponse createIndexResponse = client.admin().indices()
                    .prepareCreate(ElasticSearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX)
                    .addMapping("_default_", loadMappings())
                    .setSettings(loadSettings())
                    .execute().actionGet();

            LOGGER.info("Index created with dynamic template mapping provided, Result:"
                    + createIndexResponse.isAcknowledged());
        } catch (IndexAlreadyExistsException iaeEx) {
            LOGGER.warn("Index already exists, no need to create again....");
        } catch (Exception e) {
            LOGGER.error("Failed to load file for elasticsearch mapping settings", e);
        }
    }

    /**
     * builds mappings for suggestions feature and fields aliasing.
     * @return mapping builder
     * @throws Exception exception
     */
    private XContentBuilder loadMappings() throws Exception {
        return XContentFactory.jsonBuilder()
                .startObject()
	                .startArray("dynamic_templates")
		                .startObject()
			                .startObject("modified_template")
			                	.field("match", "modified")
				                .startObject("mapping")
				                	.field("type", "multi_field")
					                .startObject("fields")
						                .startObject("modified_date")
						                	.field("type", "long")
						                .endObject()
						                .startObject("modified")
						                	.field("type", "string")
						                .endObject()
					                .endObject()
				                .endObject()
			                .endObject()
		                .endObject()
		                .startObject()
			                .startObject("base")
				                .field("match", "*")
				                .field("unmatch", ElasticSearchIndexerConstants.ENTRY_CLASSNAME)
				                .startObject("mapping")
				                	.field("type", "multi_field")
					                .startObject("fields")
						                .startObject("{name}")
						                	.field("type", "{dynamic_type}")
						                .endObject()
						                .startObject("ngrams")
							                .field("type", "string")
							                .field("index_analyzer", "nGram_analyzer")
							                .field("search_analyzer", "whitespace_analyzer")
						                .endObject()
					                .endObject()
				                .endObject()
			                .endObject()
		                .endObject()
	                .endArray()
                .endObject();
    }

    /**
     * builds settings for suggestions feature.
     * @return setting builder
     * @throws Exception exception
     */
    private XContentBuilder loadSettings() throws Exception {
        return XContentFactory.jsonBuilder()
                .startObject()
	                .startObject("analysis")
		                .startObject("filter")
			                .startObject("filternGram")
				                .field("max_gram", 15)
				                .field("min_gram", 2)
				                .field("type", "edgeNGram")
				                .field("term_vector", "with_positions_offsets")
				                .field("version", "4.1")
				                .humanReadable(true)
			                .endObject()
		                .endObject()
		                .startObject("analyzer")
			                .startObject("nGram_analyzer")
				                .field("type", "custom")
				                .field("tokenizer", "letter")
				                .startArray("filter")
					                .value("stop")
					                .value("lowercase")
					                .value("filternGram")
				                .endArray()
			                .endObject()
			                .startObject("whitespace_analyzer")
				                .field("type", "custom")
				                .field("tokenizer", "letter")
				                .startArray("filter")
					                .value("lowercase")
					                .value("stop")
				                .endArray()
			                .endObject()
		                .endObject()
	                .endObject()
                .endObject();
    }
}
