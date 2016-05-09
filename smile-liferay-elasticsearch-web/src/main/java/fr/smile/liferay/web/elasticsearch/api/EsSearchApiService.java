package fr.smile.liferay.web.elasticsearch.api;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.HitsImpl;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.facet.AssetEntriesFacet;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.MultiValueFacet;
import com.liferay.portal.kernel.search.facet.RangeFacet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.config.FacetConfiguration;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Time;
import fr.smile.liferay.web.elasticsearch.facet.ElasticSearchQueryFacetCollector;
import fr.smile.liferay.web.elasticsearch.model.index.LiferayIndex;
import fr.smile.liferay.web.elasticsearch.util.ElasticSearchIndexerConstants;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author marem
 * @since 30/12/15.
 */
@Service
public class EsSearchApiService {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(EsSearchApiService.class);

    /** The client. */
    @Autowired
    private Client client;

    /**
     * Liferay index.
     */
    @Autowired
    private LiferayIndex index;

    /**
     * Activate fuzzy search.
     */
    private boolean isFuzzyEnabled = false;

    /**
     * Constructor.
     */
    public EsSearchApiService() {
        isFuzzyEnabled = Boolean.parseBoolean(PropsUtil.get(ElasticSearchIndexerConstants.ES_FUZZY_ENABLED));
    }

    /**
     * To.
     */
    public static final String ELASTIC_SEARCH_TO = "TO";

    /**
     * Values.
     */
    public static final String ELASTIC_SEARCH_VALUES = "values";

    /**
     * Ranges.
     */
    public static final String ELASTIC_SEARCH_RANGES = "ranges";

    /**
     * Range.
     */
    public static final String ELASTIC_SEARCH_RANGE = "range";

    /**
     * Max terms.
     */
    public static final String ELASTIC_SEARCH_MAXTERMS = "maxTerms";

    /**
     * Modified.
     */
    public static final String ELASTIC_SEARCH_INNERFIELD_MDATE = "modified.modified_date";

    /**
     * Gets the search hits.
     *
     * @param query the query
     * @param sort the sort
     * @param start the start
     * @param end the end
     * @return the search hits
     */
    public final Hits getSearchHits(final Query query, final Sort[] sort, final int start, final int end) {
        LOGGER.info("Search against Elasticsearch with query, sort, start and end parameters");

        SearchContext searchContext = new SearchContext();
        searchContext.setStart(start);
        searchContext.setEnd(end);
        searchContext.setSorts(sort);

        return getSearchHits(searchContext, query);
    }

    /**
     * Gets the search hits.
     *
     * @param searchContext the search context
     * @param query the query
     * @return the search hits
     */
    public final Hits getSearchHits(final SearchContext searchContext, final Query query) {
        LOGGER.info("Search against Elasticsearch with SearchContext");
        String queryString = escape(query.toString());

        if (isFuzzyEnabled) {
            queryString = replaceKeywords(queryString, searchContext.getKeywords());
        }

        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(queryString);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(
                index.getName()
        ).setQuery(queryBuilder);

        // Handle Search Facet queries
        if (searchContext.getFacets() != null) {
            handleFacetQueries(searchContext.getFacets(), searchRequestBuilder);
        }

        if (searchContext.getSorts() != null && searchContext.getSorts().length > 0) {
            searchRequestBuilder = searchRequestBuilder.setFrom(searchContext.getStart())
                    .setSize(searchContext.getEnd());
            for (Sort sort : searchContext.getSorts()) {
                if (sort != null && sort.getFieldName() != null) {
                    searchRequestBuilder.addSort(getSort(sort));
                }
            }
        } else {
            searchRequestBuilder = searchRequestBuilder.setFrom(searchContext.getStart())
                    .setSize(searchContext.getEnd());
        }
        SearchResponse response = searchRequestBuilder.execute().actionGet();
        collectFacetResults(searchContext, response);
        return getHits(query, response, searchContext);
    }

    /**
     * Get hits.
     * @param query query
     * @param response search response
     * @param searchContext search context
     * @return hits
     */
    private Hits getHits(final Query query, final SearchResponse response, final SearchContext searchContext) {
        Hits hits = new HitsImpl();
        SearchHits searchHits = response.getHits();
        hits.setDocs(getDocuments(searchHits, searchContext));
        hits.setScores(getScores(searchHits));
        hits.setSearchTime((float) (System.currentTimeMillis() - hits.getStart()) / Time.SECOND);
        hits.setQuery(query);

        if (searchContext.getKeywords() != null) {
            hits.setQueryTerms(searchContext.getKeywords().split(StringPool.SPACE));
        }
        hits.setLength((int) searchHits.getTotalHits());
        hits.setStart(hits.getStart());

        return hits;
    }

    /**
     * get SortBuilder based on sort array sent by Liferay.
     * @param sort sort
     * @return sort builder
     */
    private SortBuilder getSort(final Sort sort) {
        SortOrder sortOrder = SortOrder.ASC;
        if (sort.isReverse()) {
            sortOrder = SortOrder.DESC;
        }

        return SortBuilders.fieldSort(sort.getFieldName())
                .ignoreUnmapped(true)
                .order(sortOrder);
    }

    /**
     * Gets the scores.
     *
     * @param searchHits the search hits
     * @return the scores
     */
    private Float[] getScores(final SearchHits searchHits) {
        Float[] scores = new Float[searchHits.getHits().length];
        for (int i = 0; i < scores.length; i++) {
            scores[i] = searchHits.getHits()[i].getScore();
        }

        return scores;
    }

    /**
     * Gets the documents.
     *
     * @param searchHits the search hits
     * @param searchContext the search context
     * @return the documents
     */
    private Document[] getDocuments(final SearchHits searchHits, final SearchContext searchContext) {
        LOGGER.info("Getting document objects from SearchHits");

        String[] types = searchContext.getEntryClassNames();
        if (searchHits != null && searchHits.getTotalHits() > 0) {
            int failedJsonCount = 0;
            List<Document> documentsList = new ArrayList<>();
            for (SearchHit hit : searchHits.getHits()) {
                Document document = new DocumentImpl();
                try {
                    String className = null;
                    JSONObject json = JSONFactoryUtil.createJSONObject(hit.getSourceAsString());
                    Iterator jsonItr = json.keys();
                    while (jsonItr.hasNext()) {
                        String key = (String) jsonItr.next();
                        String value = json.getString(key);
                        LOGGER.debug(">>>>>>>>>> " + key + " : " + value);
                        document.add(new Field(key, value));
                        if (key.equalsIgnoreCase("entryClassName")) {
                            className = value;
                        }
                    }
                    if (ArrayUtil.contains(types, className)) {
                        documentsList.add(document);
                    }
                } catch (JSONException e) {
                    failedJsonCount++;
                    LOGGER.error("Error while processing the search result json objects", e);
                }
            }

            LOGGER.debug("Total size of the search results: " + documentsList.size());
            return documentsList.toArray(new Document[documentsList.size() - failedJsonCount]);
        } else {
            LOGGER.debug("No search results found");
            return new Document[0];
        }
    }

    /**
     * This method adds multiple facets to Elastic search query builder.
     *
     * @param facets the facets
     * @param searchRequestBuilder the search request builder
     */
    private void handleFacetQueries(final Map<String, Facet> facets,
                                    final SearchRequestBuilder searchRequestBuilder) {
        for (Facet facet : facets.values()) {
            if (!facet.isStatic()) {
                FacetConfiguration liferayFacetConfiguration = facet.getFacetConfiguration();
                JSONObject liferayFacetDataJSONObject = liferayFacetConfiguration.getData();
                if (facet instanceof MultiValueFacet) {
                    TermsBuilder termsFacetBuilder = AggregationBuilders.terms(
                        liferayFacetConfiguration.getFieldName()
                    );
                    termsFacetBuilder.field(liferayFacetConfiguration.getFieldName());
                    if (liferayFacetDataJSONObject.has(ELASTIC_SEARCH_MAXTERMS)) {
                        termsFacetBuilder.size(liferayFacetDataJSONObject.getInt(ELASTIC_SEARCH_MAXTERMS));
                    }
                    searchRequestBuilder.addAggregation(termsFacetBuilder);
                } else if (facet instanceof RangeFacet) {
                    RangeBuilder rangeFacetBuilder = AggregationBuilders.range(
                        liferayFacetConfiguration.getFieldName()
                    );

                    /**
                     *A typical ranges array looks like below.
                     *[{"range":"[20140603200000 TO 20140603220000]","label":"past-hour"},
                     * {"range":"[20140602210000 TO 20140603220000]","label":"past-24-hours"},...]
                     */
                    JSONArray rangesJSONArray = liferayFacetDataJSONObject.getJSONArray(ELASTIC_SEARCH_RANGES);
                    rangeFacetBuilder.field(ELASTIC_SEARCH_INNERFIELD_MDATE);
                    if (rangesJSONArray != null) {
                        for (int i = 0; i < rangesJSONArray.length(); i++) {
                            JSONObject rangeJSONObject = rangesJSONArray.getJSONObject(i);
                            String[] fromTovalues = fetchFromToValuesInRage(rangeJSONObject);
                            if (fromTovalues != null) {
                                rangeFacetBuilder.addRange(
                                        Double.parseDouble(fromTovalues[0].trim()),
                                        Double.parseDouble(fromTovalues[1].trim())
                                );
                            }
                        }
                    }
                    searchRequestBuilder.addAggregation(rangeFacetBuilder);
                }
            }
        }
    }

    /**
     * This method converts the Elastic search facet results to Liferay facet collector.
     *
     * @param searchContext the search context
     * @param response the response
     */
    private void collectFacetResults(final SearchContext searchContext, final SearchResponse response) {

        for (Map.Entry<String, Facet> facetEntry: searchContext.getFacets().entrySet()) {
            Facet liferayFacet = facetEntry.getValue();
            if (!liferayFacet.isStatic()) {
                Aggregation esFacet = response.getAggregations().get(facetEntry.getKey());
                if (esFacet != null) {
                    Map<String, Integer> facetResults = null;

                    /**
                     * AssetEntries consist of Fully qualified class names, since the classnames are
                     * case insensitive and at the same time ES facet result terms are returned in
                     * lowercase, we need to handle this case differently. While creating the Facet
                     * collectors, the terms (in this case Entryclassnames) are obtained from Liferay
                     * facet configuration.
                     * E.g:com.liferay.portlet.messageboards.model.MBThread would be converted to
                     * com.liferay.portlet.messageboards.model.mbmessage in ES server facet result
                     */
                    if ((liferayFacet instanceof AssetEntriesFacet)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Handling AssetEntriesFacet now for field:" + facetEntry.getKey() + "...");
                        }
                        Map<String, Integer> esTermsFacetResults = parseESFacet(esFacet);
                        facetResults = new HashMap<>();

                        for (String entryClassname : fetchEntryClassnames(liferayFacet)) {

                            if (esTermsFacetResults.get(entryClassname.toLowerCase()) != null) {
                                facetResults.put(entryClassname, esTermsFacetResults.get(entryClassname.toLowerCase()));
                            } else {
                                facetResults.put(entryClassname, 0);
                            }
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("AssetEntriesFacet>>>>>>>>>>>>Term:" + entryClassname
                                                + " <<<<Count:" + esTermsFacetResults.get(entryClassname.toLowerCase())
                                );
                            }
                        }

                    } else if ((liferayFacet instanceof MultiValueFacet)) {
                        facetResults = new HashMap<>();
                        Terms esTermsFacetResults = (Terms) esFacet;
                        Collection<Terms.Bucket> buckets = esTermsFacetResults.getBuckets();
                        for (Terms.Bucket bucket : buckets) {
                            facetResults.put(bucket.getKeyAsString(), (int) bucket.getDocCount());
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("MultiValueFacet>>>>>>>>>>>>Term:" + bucket.getKeyAsString()
                                                + " <<<<Count:" + bucket.getDocCount()
                                );
                            }
                        }
                    } else if ((liferayFacet instanceof RangeFacet)) {
                        Range esRange = (Range) esFacet;
                        facetResults = new HashMap<>();
                        for (Range.Bucket entry : esRange.getBuckets()) {
                            facetResults.put(buildRangeTerm(entry), (int) entry.getDocCount());
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(">>>>>>>From:" + entry.getFromAsString()
                                        + ">>>>>>>To:" + entry.getToAsString()
                                        + ">>>>>>>Count:" + entry.getDocCount());
                            }
                        }
                    }

                    FacetCollector facetCollector = new ElasticSearchQueryFacetCollector(
                        facetEntry.getKey(),
                        facetResults
                    );
                    liferayFacet.setFacetCollector(facetCollector);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Facet collector successfully set for field:" + facetEntry.getKey() + "...");
                    }
                }
            }
        }
    }


    /**
     * Builds the range term.
     *
     * @param entry the entry
     * @return the string
     */
    private String buildRangeTerm(final Range.Bucket entry) {
        return StringPool.OPEN_BRACKET + entry.getFromAsString()
                + StringPool.SPACE + ELASTIC_SEARCH_TO
                + StringPool.SPACE + entry.getToAsString()
                + StringPool.CLOSE_BRACKET;
    }

    /**
     * Fetch entry classnames.
     *
     * @param liferayFacet the liferay facet
     * @return the sets the
     */
    private Set<String> fetchEntryClassnames(final Facet liferayFacet) {
        JSONObject dataJSONObject = liferayFacet.getFacetConfiguration().getData();
        JSONArray valuesArray = dataJSONObject.getJSONArray(ELASTIC_SEARCH_VALUES);
        Set<String> entryClassnames = new HashSet<>();
        if (valuesArray != null) {
            for (int z = 0; z < valuesArray.length(); z++) {
                entryClassnames.add(valuesArray.getString(z));
            }
        }
        return entryClassnames;
    }

    /**
     * Fetch from to values in rage.
     *
     * @param jsonObject the json object
     * @return the string[]
     */
    private String[] fetchFromToValuesInRage(final JSONObject jsonObject) {
        String fromToFormatRange = jsonObject.getString(ELASTIC_SEARCH_RANGE);
        String[] fromToArray = null;
        if (fromToFormatRange != null && fromToFormatRange.length() > 0) {
            fromToArray = fromToFormatRange.substring(1, fromToFormatRange.length() - 1).split(ELASTIC_SEARCH_TO);
        }
        return fromToArray;
    }

    /**
     * Escape query.
     * @param s strig query to escape
     * @return query escaped
     */
    public static String escape(final String s) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == 47) {
                sb.append('\\');
            }

            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Parses the es facet to return a map with Entryclassname and its count.
     *
     * @param esFacet the es facet
     * @return the map
     */
    private Map<String, Integer> parseESFacet(final Aggregation esFacet) {
        Terms terms = (Terms) esFacet;
        Collection<Terms.Bucket> buckets = terms.getBuckets();
        Map<String, Integer> esTermFacetResultMap = new HashMap<>();
        for (Terms.Bucket bucket : buckets) {
            esTermFacetResultMap.put(bucket.getKeyAsString(), (int) bucket.getDocCount());
        }

        return esTermFacetResultMap;
    }

    /**
     * Replace keywords.
     * @param inputQueryString query string
     * @param inputKeywords keywords
     * @return replaced keywords
     */
    private String replaceKeywords(final String inputQueryString, final String inputKeywords) {
        // Remove the double quotes from searchContext keywords
        String keywords = inputKeywords.replaceAll("\"", "");
        String[] listTerms = keywords.split(" ");
        String newKeywords = "";

        // If only one keyword, use of Fuzzy
        if (listTerms.length == 1) {
            newKeywords += listTerms[0] + "~2";

            // If more than one keywords, use of field grouping
        } else {
            newKeywords += "(";
            for (int i = 0; i < listTerms.length; i++) {
                newKeywords += "+" + listTerms[i];
                if (i != listTerms.length - 1) {
                    newKeywords += " ";
                }
            }
            newKeywords += ")";
        }

        // Modify the query to use Fuzzy or Field Grouping
        return inputQueryString.replaceAll("\\*", "")
                .replaceAll(":\"" + keywords + "\"", ":" + keywords)
                .replaceAll(":" + keywords, ":" + newKeywords);
    }
}
