package fr.smile.liferay.web.elasticsearch.searcher;

import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.BooleanQueryFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerPostProcessor;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author marem
 * @since 15/11/16.
 */
@Service
public class FacetedSearcher extends com.liferay.portal.kernel.search.FacetedSearcher {

    /**
     * Transform original query to apply specific rules.
     * @param searchContext search context
     * @return transformed query
     */
    public final BooleanQuery rebuildQuery(final SearchContext searchContext) {
        BooleanQuery contextQuery = BooleanQueryFactoryUtil.create(
                searchContext);

        contextQuery.addRequiredTerm(
                Field.COMPANY_ID, searchContext.getCompanyId());

        BooleanQuery fullQuery = null;
        try {
            fullQuery = createFullQuery(
                    contextQuery, searchContext);
            QueryConfig queryConfig = searchContext.getQueryConfig();

            fullQuery.setQueryConfig(queryConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fullQuery;
    }

    @Override
    public final BooleanQuery getFullQuery(final SearchContext searchContext)
            throws SearchException {

        try {
            BooleanQuery contextQuery = BooleanQueryFactoryUtil.create(
                    searchContext);

            addSearchAssetCategoryIds(contextQuery, searchContext);
            addSearchAssetTagNames(contextQuery, searchContext);
            addSearchEntryClassNames(contextQuery, searchContext);
            addSearchFolderId(contextQuery, searchContext);
            addSearchGroupId(contextQuery, searchContext);
            addSearchLayout(contextQuery, searchContext);
            addSearchUserId(contextQuery, searchContext);

            BooleanQuery fullQuery = createFullQuery(
                    contextQuery, searchContext);

            fullQuery.setQueryConfig(searchContext.getQueryConfig());

            return fullQuery;
        } catch (SearchException se) {
            throw se;
        } catch (Exception e) {
            throw new SearchException(e);
        }
    }

    @Override
    protected final BooleanQuery createFullQuery(
            final BooleanQuery contextQuery, final SearchContext searchContext)
            throws Exception {

        BooleanQuery searchQuery = BooleanQueryFactoryUtil.create(
                searchContext);

        String keywords = searchContext.getKeywords();

        if (Validator.isNotNull(keywords)) {
            addSearchLocalizedTerm(
                    searchQuery, searchContext, Field.ASSET_CATEGORY_TITLES, false);

            searchQuery.addExactTerm(Field.ASSET_TAG_NAMES, keywords);

            BooleanQuery keyWordsQuery = BooleanQueryFactoryUtil.create(searchContext);
            String[] listTerms = keywords.split(" ");
            for (String field : Field.KEYWORDS) {
                BooleanQuery keyWordsFieldQuery = BooleanQueryFactoryUtil.create(searchContext);
                for (String term : listTerms) {
                    keyWordsFieldQuery.addRequiredTerm(field, term, true);
                }
                keyWordsQuery.add(keyWordsFieldQuery, BooleanClauseOccur.SHOULD);
            }

            searchQuery.add(keyWordsQuery, BooleanClauseOccur.MUST);

            int groupId = GetterUtil.getInteger(
                    searchContext.getAttribute(Field.GROUP_ID));

            if (groupId == 0) {
                searchQuery.addTerm(
                        Field.STAGING_GROUP, "true", false,
                        BooleanClauseOccur.MUST_NOT);
            }
        }

        for (String entryClassName : searchContext.getEntryClassNames()) {
            Indexer indexer = IndexerRegistryUtil.getIndexer(entryClassName);

            if (indexer == null) {
                continue;
            }

            String searchEngineId = searchContext.getSearchEngineId();

            if (!searchEngineId.equals(indexer.getSearchEngineId())) {
                continue;
            }

            if (Validator.isNotNull(keywords)) {
                addSearchExpandoKeywords(
                        searchQuery, searchContext, keywords, entryClassName);
            }

            indexer.postProcessSearchQuery(searchQuery, searchContext);

            for (IndexerPostProcessor indexerPostProcessor : indexer.getIndexerPostProcessors()) {

                indexerPostProcessor.postProcessSearchQuery(
                        searchQuery, searchContext);
            }
        }

        Map<String, Facet> facets = searchContext.getFacets();

        for (Facet facet : facets.values()) {
            BooleanClause facetClause = facet.getFacetClause();

            if (facetClause != null) {
                contextQuery.add(
                        facetClause.getQuery(),
                        facetClause.getBooleanClauseOccur());
            }
        }

        BooleanQuery fullQuery = BooleanQueryFactoryUtil.create(searchContext);

        fullQuery.add(contextQuery, BooleanClauseOccur.MUST);

        if (searchQuery.hasClauses()) {
            fullQuery.add(searchQuery, BooleanClauseOccur.MUST);
        }

        BooleanClause[] booleanClauses = searchContext.getBooleanClauses();

        if (booleanClauses != null) {
            for (BooleanClause booleanClause : booleanClauses) {
                fullQuery.add(
                        booleanClause.getQuery(),
                        booleanClause.getBooleanClauseOccur());
            }
        }

        for (String entryClassName : searchContext.getEntryClassNames()) {
            Indexer indexer = IndexerRegistryUtil.getIndexer(entryClassName);

            if (indexer == null) {
                continue;
            }

            String searchEngineId = searchContext.getSearchEngineId();

            if (!searchEngineId.equals(indexer.getSearchEngineId())) {
                continue;
            }

            for (IndexerPostProcessor indexerPostProcessor : indexer.getIndexerPostProcessors()) {

                indexerPostProcessor.postProcessFullQuery(
                        fullQuery, searchContext);
            }
        }

        return fullQuery;
    }
}
