package fr.smile.liferay.web.elasticsearch;

import com.google.common.io.Resources;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Prepare the index to be used by Liferay.
 */
@Configuration
public class ElasticsearchWebConfig {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(Index.class);

    /**
     * Default setting file path.
     */
    private static final String DEFAULT_SETTINGS_PATH = "elasticsearch/settings/settings.json";

    /**
     * Default mapping file path.
     */
    private static final String DEFAULT_MAPPINGS_PATH = "elasticsearch/mappings/mappings.json";

    /**
     * Index service.
     */
    @Autowired
    private IndexService indexService;

    /**
     * Configure a file (settings or mappings) path used in the configuration.
     * @param path the path defined in the Liferay's portal-ext.properties
     * @param defaultPath the default path if none is defined in Liferay configuration
     * @return the path of the configured file, an {@link ElasticsearchException} otherwise
     * @throws URISyntaxException exception when file uri is wrong
     */
    private String configurePath(final String path, final String defaultPath) throws URISyntaxException {
        String resultPath = path;

        if (resultPath == null) {
            URL url = Resources.getResource(defaultPath);
            if (url != null) {
                resultPath = Paths.get(url.toURI()).toString();
            }
            if (resultPath == null) {
                throw new ElasticsearchException("Error on retrieving index configuration: ");
            }
        }

        return resultPath;
    }

    /**
     * Build liferay index bean.
     * @return index
     * @throws IOException exception when reading mappings and settings files.
     * @throws URISyntaxException exception when file uri is wrong.
     */
    @Bean
    public Index liferayIndex() throws IOException, URISyntaxException {
        String name = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_INDEX);

        String settingsFilePath = PropsUtil.get(ElasticSearchIndexerConstants.ES_SETTINGS_PATH);
        settingsFilePath = configurePath(settingsFilePath, DEFAULT_SETTINGS_PATH);

        String mappingsFilePath = PropsUtil.get(ElasticSearchIndexerConstants.ES_MAPPINGS_PATH);
        mappingsFilePath = configurePath(mappingsFilePath, DEFAULT_MAPPINGS_PATH);

        String indexSettings = new String(Files.readAllBytes(Paths.get(settingsFilePath)));
        String indexMappings = new String(Files.readAllBytes(Paths.get(mappingsFilePath)));

        Index index = new Index(name, indexSettings, indexMappings);
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
