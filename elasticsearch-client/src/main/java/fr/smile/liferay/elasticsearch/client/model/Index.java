package fr.smile.liferay.elasticsearch.client.model;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import fr.smile.liferay.elasticsearch.client.ElasticSearchIndexerConstants;
import fr.smile.liferay.elasticsearch.client.service.IndexService;
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
public class Index {

    /**
     * Index api service.
     */
    @Autowired
    private IndexService indexService;

    /**
     * Index name.
     */
    private String name;

    /**
     * Index settings.
     */
    private String indexSettings;

    /**
     * Index mappings.
     */
    private String indexMappings;

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(Index.class);

    /**
     * Constructor.
     * @param name index name
     * @param settings index settings
     * @param mappings index mappings
     */
    public Index(final String name, final String settings, final String mappings) {
        this.name = name;
        this.indexSettings = settings;
        this.indexMappings = mappings;
    }

    /**
     * Check if Liferay index already exists, else create one with
     * default mapping The Index creation is one time setup, so it is
     * important to check if index already exists before creation.
     */
    @PostConstruct
    public final void initIndex() {
        try {
            if (!indexService.checkIfIndexExists(name)) {
                indexService.createIndex(name, indexMappings, indexSettings);
            }
        } catch (ElasticsearchException configEx) {
            LOGGER.error("Error while connecting to Elasticsearch server:" + configEx.getMessage());
        }
    }

    /**
     * Get name.
     * @return name
     */
    public final String getName() {
        return name;
    }

    /**
     * Set name.
     * @param index index name
     */
    public final void setName(final String index) {
        this.name = index;
    }

    /**
     * Get index settings.
     * @return index settings
     */
    public final String getIndexSettings() {
        return indexSettings;
    }

    /**
     * Set index settings.
     * @param indexSettings index settings
     */
    public final void setIndexSettings(final String indexSettings) {
        this.indexSettings = indexSettings;
    }

    /**
     * Get index mappings.
     * @return index mappings
     */
    public final String getIndexMappings() {
        return indexMappings;
    }

    /**
     * Set index mappings.
     * @param indexMappings index mappings
     */
    public final void setIndexMappings(final String indexMappings) {
        this.indexMappings = indexMappings;
    }
}
