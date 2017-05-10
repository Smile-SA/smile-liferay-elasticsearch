package fr.smile.liferay.elasticsearch.client;

import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import org.elasticsearch.ElasticsearchException;

/**
 * This object contains all the information needed to connect to the ElasticSearch index.
 */
public class ConnexionSettings {

    /**
     * Nodes.
     */
    private String[] nodes;

    /**
     * Server home.
     */
    private String serverHome;

    /**
     * Cluster name.
     */
    private String clusterName;

    /**
     * Build configuration.
     * @throws ElasticsearchException if config is not valid.
     */
    public ConnexionSettings() throws ElasticsearchException {
        this.clusterName = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_CLUSTERNAME);
        if (Validator.isNull(this.clusterName) || this.clusterName.isEmpty()) {
            throw new ElasticsearchException("Elasticsearch cluster name is not configured...");
        }

        this.serverHome = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_HOME_PATH);

        if (Validator.isNull(this.serverHome) || this.serverHome.isEmpty()) {
            throw new ElasticsearchException("Elasticsearch server home folder is not configured...");
        }

        String csElasticsearchNodes = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_NODE);
        if (Validator.isNull(csElasticsearchNodes) || csElasticsearchNodes.isEmpty()) {
            throw new ElasticsearchException("Elasticsearch server node is not configured...");
        }

        this.nodes = csElasticsearchNodes.split(StringPool.COMMA);
    }

    /**
     * Get server home.
     * @return server home
     */
    public final String getServerHome() {
        return serverHome;
    }

    /**
     * Get cluster name.
     * @return cluster name
     */
    public final String getClusterName() {
        return clusterName;
    }

    /**
     * Get elasticsearch cluster node names.
     * @return node names
     */
    public final String[] getNodes() {
        return nodes;
    }
}
