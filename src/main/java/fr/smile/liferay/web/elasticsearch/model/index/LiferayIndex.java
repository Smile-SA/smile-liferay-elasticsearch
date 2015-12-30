package fr.smile.liferay.web.elasticsearch.model.index;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import fr.smile.liferay.web.elasticsearch.api.EsIndexApiService;
import fr.smile.liferay.web.elasticsearch.util.ElasticSearchIndexerConstants;
import org.elasticsearch.ElasticsearchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author marem
 * @since 29/10/15.
 */
@Service
public class LiferayIndex {

    @Autowired
    private EsIndexApiService esIndexApiService;

    private String name;

    private String indexSettings;

    private String indexMappings;

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(LiferayIndex.class);

    public LiferayIndex() throws IOException {
        this.name = PropsUtil.get(ElasticSearchIndexerConstants.ES_KEY_INDEX);

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

        indexSettings = new String(Files.readAllBytes(Paths.get(settingsFilePath)));

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

        indexMappings = new String(Files.readAllBytes(Paths.get(mappingsFilePath)));
    }

    /**
     * Check if Liferay index already exists, else create one with
     * default mapping The Index creation is one time setup, so it is
     * important to check if index already exists before creation
     */
    @PostConstruct
    public void initIndex() {
        try {
            if (!esIndexApiService.isLiferayIndexExists(name)) {
                LOGGER.debug("Liferay index does not exists, creating it");
                esIndexApiService.createIndex(name, indexMappings, indexSettings);
            }
        } catch (ElasticsearchException configEx) {
            LOGGER.error("Error while connecting to Elasticsearch server:" + configEx.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String index) {
        this.name = index;
    }

    public String getIndexSettings() {
        return indexSettings;
    }

    public void setIndexSettings(final String indexSettings) {
        this.indexSettings = indexSettings;
    }

    public String getIndexMappings() {
        return indexMappings;
    }

    public void setIndexMappings(final String indexMappings) {
        this.indexMappings = indexMappings;
    }
}
