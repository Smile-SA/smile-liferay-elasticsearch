package fr.smile.liferay.web.elasticsearch;

import com.liferay.portal.kernel.search.BaseIndexSearcher;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.BooleanQueryFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.Sort;
import fr.smile.liferay.web.elasticsearch.api.EsSearchApiService;
import fr.smile.liferay.web.elasticsearch.searcher.FacetedSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author marem
 * @since 29/10/15.
 */
@Service
public class ElasticsearchIndexSearcherImpl extends BaseIndexSearcher {

    /** The _es search helper. */
    @Autowired
    private EsSearchApiService esSearchApiService;

    /** Faceted searcher. */
    @Autowired
    private FacetedSearcher searcher;

    @Override
    public final Hits search(final SearchContext searchContext, final Query query) throws SearchException {
        Query rebuiltQuery = query;
        if (!StringUtils.isEmpty(searchContext.getKeywords())) {
            rebuiltQuery = searcher.rebuildQuery(searchContext);
        }

        return esSearchApiService.getSearchHits(searchContext, rebuiltQuery);
    }

    @Override
    public final Hits search(final String searchEngineId, final long companyId, final Query query,
                       final Sort[] sort, final int start, final int end) throws SearchException {
        return esSearchApiService.getSearchHits(query, sort, start, end);
    }
}
