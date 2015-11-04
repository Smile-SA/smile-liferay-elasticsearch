package fr.smile.liferay.web.elasticsearch;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.*;
import fr.smile.liferay.web.elasticsearch.util.ElasticSearchHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author marem
 * @since 29/10/15.
 */
public class ElasticsearchIndexSearcherImpl extends BaseIndexSearcher {

    /** LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(ElasticsearchIndexSearcherImpl.class);

    /** The _es search helper. */
    @Autowired
    private ElasticSearchHelper _esSearchHelper;

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.IndexSearcher#search(com.liferay.portal.kernel.search.SearchContext, com.liferay.portal.kernel.search.Query)
     */
    @Override
    public Hits search(SearchContext searchContext, Query query) throws SearchException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Search against elasticsearch indexes");
        }

        return _esSearchHelper.getSearchHits(searchContext, query);
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.IndexSearcher#search(java.lang.String, long, com.liferay.portal.kernel.search.Query, com.liferay.portal.kernel.search.Sort[], int, int)
     */
    @Override
    public Hits search(final String searchEngineId, final long companyId, final Query query,
                       final Sort[] sort, final int start, final int end) throws SearchException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Search with sort and ranges against elasticsearch indexes");
        }
        return _esSearchHelper.getSearchHits(searchEngineId, companyId, query, sort, start, end);
    }
}
