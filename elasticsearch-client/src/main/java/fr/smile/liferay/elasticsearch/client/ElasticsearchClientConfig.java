package fr.smile.liferay.elasticsearch.client;

import org.elasticsearch.client.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

/**
 * @author marem
 * @since 27/07/16.
 */
@Configuration
public class ElasticsearchClientConfig {

    @Bean
    public ConnexionSettings connexionSettings() {
        return new ConnexionSettings();
    }

    @Bean
    public Client client() throws UnknownHostException {
        ElasticsearchClient esClient = new ElasticsearchClient();
        return esClient.getClient();
    }
}
