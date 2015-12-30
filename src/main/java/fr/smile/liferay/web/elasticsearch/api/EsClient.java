package fr.smile.liferay.web.elasticsearch.api;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import fr.smile.liferay.web.elasticsearch.util.ElasticSearchIndexerConstants;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author marem
 * @since 30/12/15.
 */
@Configuration
public class EsClient {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(EsIndexApiService.class);

    /** Elasticsearch build settings */
    public static final String ES_SETTING_PATH_HOME = "path.home";
    public static final String ES_SETTING_CLUSTERNAME = "cluster.name";
    public static final String ES_SETTING_CLIENT_SNIFF = "client.transport.sniff";

    private String serverHome;

    private String clusterName;

    private String[] nodes;

    @Bean
    public Client elasticSearchClient() throws UnknownHostException {
        this.clusterName = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_CLUSTERNAME);
        this.serverHome = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_HOME_PATH);

        if (Validator.isNull(this.serverHome) || this.serverHome.isEmpty()) {
            throw new ElasticsearchException("Elasticsearch server home folder is not configured...");
        }

        String csElasticsearchNodes = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_NODE);
        if (Validator.isNull(csElasticsearchNodes) || csElasticsearchNodes.isEmpty()) {
            throw new ElasticsearchException("Elasticsearch server node is not configured...");
        }

        this.nodes = csElasticsearchNodes.split(StringPool.COMMA);
        InetSocketTransportAddress[] transportAddresses = new InetSocketTransportAddress[nodes.length];

        /** Create a settings object with custom attributes and build */
        Settings.Builder settingsBuilder = Settings.settingsBuilder()
                .put(ES_SETTING_PATH_HOME, serverHome)
                .put(ES_SETTING_CLIENT_SNIFF, true);
        if (Validator.isNotNull(this.clusterName) && !this.clusterName.isEmpty()
                && !"elasticsearch".equalsIgnoreCase(this.clusterName)) {
            settingsBuilder.put(ES_SETTING_CLUSTERNAME, this.clusterName);
        }


        /** Prepare a list of Hosts */
        for (int i = 0; i < nodes.length; i++) {
            String[] hostnames = nodes[i].split(StringPool.COLON);
            InetSocketTransportAddress transportAddress = new InetSocketTransportAddress(
                    InetAddress.getByName(hostnames[0]),
                    Integer.parseInt(hostnames[1])
            );
            transportAddresses[i] = transportAddress;
        }

        return TransportClient.builder().settings(
                settingsBuilder.build()
        ).build().addTransportAddresses(transportAddresses);
    }

    public String getServerHome() {
        return serverHome;
    }

    public void setServerHome(final String serverHome) {
        this.serverHome = serverHome;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    public String[] getNodes() {
        return nodes;
    }

    public void setNodes(final String[] nodes) {
        this.nodes = nodes;
    }
}
