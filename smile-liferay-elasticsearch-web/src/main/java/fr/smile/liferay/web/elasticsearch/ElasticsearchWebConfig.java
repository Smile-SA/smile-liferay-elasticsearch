package fr.smile.liferay.web.elasticsearch;

import com.liferay.portal.kernel.util.PropsUtil;
import fr.smile.liferay.elasticsearch.client.ConnexionSettings;
import fr.smile.liferay.elasticsearch.client.ElasticSearchIndexerConstants;
import fr.smile.liferay.elasticsearch.client.model.Index;
import org.elasticsearch.ElasticsearchException;
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

    /**
     * Build connection settings bean.
     * @return connection setting
     */
    @Bean
    public final ConnexionSettings connexionSettings() {
        return new ConnexionSettings();
    }

    /**
     * Build liferay index bean.
     * @return index
     * @throws IOException exception when reading mappings and settings files.
     */
    @Bean
    public final Index liferayIndex() throws IOException {
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


        return new Index(name, indexSettings, indexMappings);
    }
}
