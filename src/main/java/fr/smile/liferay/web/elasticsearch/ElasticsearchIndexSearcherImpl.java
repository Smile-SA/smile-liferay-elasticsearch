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

    @Override
    public Hits search(SearchContext searchContext, Query query) throws SearchException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Search against elasticsearch indexes");
        }

        return _esSearchHelper.getSearchHits(searchContext, query);
    }

    @Override
    public Hits search(final String searchEngineId, final long companyId, final Query query,
                       final Sort[] sort, final int start, final int end) throws SearchException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Search with sort and ranges against elasticsearch indexes");
        }
        return _esSearchHelper.getSearchHits(query, sort, start, end);
    }
}
