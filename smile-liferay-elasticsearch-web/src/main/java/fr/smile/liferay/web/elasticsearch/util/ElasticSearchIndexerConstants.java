package fr.smile.liferay.web.elasticsearch.util;

/**
 * @author marem
 * @since 29/10/15.
 */
public final class ElasticSearchIndexerConstants {

    /**
     * Util class private constructor.
     */
    private ElasticSearchIndexerConstants() {
    }

    /** Elasticsearch portal property keys. */
    public static final String ES_KEY_CLUSTERNAME = "elasticsearch.clusterName";

    /**
     * Elasticsearch node.
     */
    public static final String ES_KEY_NODE = "elasticsearch.node";

    /**
     * Elasticsearch home file.
     */
    public static final String ES_KEY_HOME_PATH = "elasticsearch.homeFile";

    /**
     * Elasticsearch index.
     */
    public static final String ES_KEY_INDEX = "elasticsearch.index";

    /**
     * Index settings file path.
     */
    public static final String ES_SETTINGS_PATH = "elasticsearch.settings.path";

    /**
     * Index mapping file path.
     */
    public static final String ES_MAPPINGS_PATH = "elasticsearch.mappings.path";

    /**
     * Is Fuzzy search enabled.
     */
    public static final String ES_FUZZY_ENABLED = "elasticsearch.fuzzy.enable";

}
