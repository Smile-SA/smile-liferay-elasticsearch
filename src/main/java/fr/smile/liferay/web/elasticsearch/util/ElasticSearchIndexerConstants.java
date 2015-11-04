package fr.smile.liferay.web.elasticsearch.util;

import com.liferay.portal.kernel.util.StringPool;

/**
 * @author marem
 * @since 29/10/15.
 */
public class ElasticSearchIndexerConstants {

    /** Elasticsearch build settings */
    public static final String ES_SETTING_PATH_HOME = "path.home";
    public static final String ES_SETTING_CLUSTERNAME = "cluster.name";
    public static final String ES_SETTING_CLIENT_SNIFF = "client.transport.sniff";

    /** Elasticsearch portal property keys */
    public static final String ES_KEY_CLUSTERNAME = "elasticsearch.clusterName";
    public static final String ES_KEY_NODE = "elasticsearch.node";
    public static final String ES_KEY_HOME_PATH = "elasticsearch.homeFile";

    /** Portlet property keys */
    public static final String ES_KEY_EXCLUDED_INDEXTYPE = "indexExcludedType";

    /** Other constants required in plugin */
    public static final String ELASTIC_SEARCH_INDEXER_WEB_CONTENT = "web_content";
    public static final String ELASTIC_SEARCH_INDEXER_USER = "user";
    public static final String ELASTIC_SEARCH = "elasticsearch";
    public static final String ELASTIC_SEARCH_LIFERAY_INDEX = "liferay";
    public static final String ELASTIC_SEARCH_UID = "uid";
    public static final String ELASTIC_SEARCH_QUERY_UID = ELASTIC_SEARCH_UID + StringPool.COLON;

    public static final String ELASTIC_SEARCH_TERM_PARAMS = "params";
    public static final String ELASTIC_SEARCH_TERM_PROPERTIES = "properties";
    public static final String ELASTIC_SEARCH_TO = "TO";
    public static final String ELASTIC_SEARCH_TERM_STATUS = "status";
    public static final String ELASTIC_SEARCH_TERM_DATE = "date";
    public static final String ELASTIC_SEARCH_ADVANCED_SEARCH = "advancedSearch";
    public static final String ELASTIC_SEARCH_VALUES = "values";
    public static final String ELASTIC_SEARCH_RANGES = "ranges";
    public static final String ELASTIC_SEARCH_RANGE = "range";
    public static final String ELASTIC_SEARCH_MAXTERMS = "maxTerms";

    public static final String ELASTIC_SEARCH_INNERFIELD_MDATE = "modified.modified_date";

    public static final String ELASTIC_SEARCH_DATE_FORMAT = "yyyy-MM-dd";

    public static final String ELASTIC_SEARCH_FIELD_NAMES_FOR_FACETS = "userId,assetTagNames,modified,categoryId,entryClassName,folderId,groupId";
    public static final String ELASTIC_SEARCH_FACET_USERID = "userId";
    public static final String ELASTIC_SEARCH_FACET_ASSETTAGNAMES = "assetTagNames";
    public static final String ELASTIC_SEARCH_FACET_MODIFIED = "modified";
    public static final String ELASTIC_SEARCH_FACET_ASSETCATEGORYIDS = "categoryId";
    public static final String ELASTIC_SEARCH_FACET_ENTRYCLASSNAME = "entryClassName";
    public static final String ELASTIC_SEARCH_FACET_FOLDERID = "folderId";
    public static final String ELASTIC_SEARCH_FACET_GROUPID = "groupId";

    public static final String ENTRY_CLASSNAME = "entryClassName";
    public static final String ENTRY_CLASSPK = "entryClassPK";
    public static final String HIDDEN = "hidden";
    public static final String VERSION = "version";
    public static final String WAR = "/war";
}
