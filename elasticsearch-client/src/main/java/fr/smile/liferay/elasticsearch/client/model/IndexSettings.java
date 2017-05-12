package fr.smile.liferay.elasticsearch.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.elasticsearch.common.settings.Settings;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Index settings representation.
 */
@XmlRootElement
public class IndexSettings extends IndexConfig {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(IndexSettings.class);

    @JsonProperty("settings")
    private Map<String, String> settings;

    /**
     * Default constructor.
     */
    public IndexSettings() {
    }

    /**
     * Get the index settings.
     * @return the settings
     */
    public Map<String, String> getSettings() {
        return settings;
    }

    /**
     * Set the index settings.
     * @param settings the index settings
     */
    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    /**
     * Extract the index settings from the ElasticSearch [@link Settings}.
     * @param indexName the index name
     * @param esSettings the ElasticSearch settings
     * @return an instance of {@link IndexSettings}
     */
    public static IndexSettings from(String indexName, Settings esSettings) {
        if (esSettings != null && !esSettings.isEmpty()) {
            IndexSettings indexSettings = new IndexSettings();
            indexSettings.setIndex(indexName);
            indexSettings.setSettings(esSettings.getAsMap());
            return indexSettings;
        }
        return null;
    }
}
