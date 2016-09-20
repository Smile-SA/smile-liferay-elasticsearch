package fr.smile.liferay.web.elasticsearch;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import fr.smile.liferay.elasticsearch.client.ConnexionSettings;
import fr.smile.liferay.elasticsearch.client.ElasticSearchIndexerConstants;
import fr.smile.liferay.elasticsearch.client.model.Index;
import fr.smile.liferay.elasticsearch.client.service.IndexService;
import org.elasticsearch.ElasticsearchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author marem
 * @since 27/07/16.
 */
@Configuration
public class ElasticsearchWebConfig {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(Index.class);

    /**
     * Index service.
     */
    @Autowired
    private IndexService indexService;

    /**
     * Build liferay index bean.
     * @return index
     * @throws IOException exception when reading mappings and settings files.
     */
    @Bean
    public Index liferayIndex() throws IOException {
        String name = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_INDEX);

        String settingsFilePath = PropsUtil.get(ElasticSearchIndexerConstants.ES_SETTINGS_PATH);
        if (settingsFilePath == null) {
            ClassLoader classLoader = getClass().getClassLoader();
            URL url = classLoader.getResource("elasticsearch/settings/settings.json");
            if (url != null) {
                settingsFilePath = url.getPath();
            }
        }

        if (settingsFilePath == null) {
            throw new ElasticsearchException("Error on retrieving index settings");
        }


        String mappingsFilePath = PropsUtil.get(ElasticSearchIndexerConstants.ES_MAPPINGS_PATH);
        if (mappingsFilePath == null) {
            ClassLoader classLoader = getClass().getClassLoader();
            URL url = classLoader.getResource("elasticsearch/mappings/mappings.json");
            if (url != null) {
                mappingsFilePath = url.getPath();
            }
        }

        if (mappingsFilePath == null) {
            throw new ElasticsearchException("Error on retrieving index mappings");
        }

        String indexSettings = new String(Files.readAllBytes(Paths.get(settingsFilePath)));
        String indexMappings = new String(Files.readAllBytes(Paths.get(mappingsFilePath)));


        Index index = new Index(name, indexMappings, indexSettings);
        try {
            if (!indexService.checkIfIndexExists(name)) {
                indexService.createIndex(index);
            }
        } catch (ElasticsearchException configEx) {
            LOGGER.error("Error while connecting to Elasticsearch server:" + configEx.getMessage());
        }

        return index;
    }
}
