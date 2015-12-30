package fr.smile.liferay.web.elasticsearch;

import com.liferay.portal.kernel.search.*;
import fr.smile.liferay.web.elasticsearch.api.EsSearchApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author marem
 * @since 29/10/15.
 */
@Service
public class ElasticsearchIndexSearcherImpl implements IndexSearcher {

    /** The _es search helper. */
    @Autowired
    private EsSearchApiService esSearchApiService;

    @Override
    public final Hits search(final SearchContext searchContext, final Query query) throws SearchException {
        return esSearchApiService.getSearchHits(searchContext, query);
    }

    @Override
    public final Hits search(final String searchEngineId, final long companyId, final Query query,
                       final Sort[] sort, final int start, final int end) throws SearchException {
        return esSearchApiService.getSearchHits(query, sort, start, end);
    }
}
