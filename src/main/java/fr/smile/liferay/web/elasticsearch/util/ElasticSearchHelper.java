package fr.smile.liferay.web.elasticsearch.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregator;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregatorFactory;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

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
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Time;

import fr.smile.liferay.web.elasticsearch.facet.ElasticSearchQueryFacetCollector;

/**
 * @author marem
 * @since 29/10/15.
 */
public class ElasticSearchHelper {

    /** The Constant _log. */
    private static final Log _log = LogFactoryUtil.getLog(ElasticSearchHelper.class);

    /** The _es connector. */
    @Autowired
    private ElasticSearchConnector _esConnector;

    /**
     * Gets the search hits.
     *
     * @param searchContext the search context
     * @param query the query
     * @return the search hits
     */
    public final Hits getSearchHits(final SearchContext searchContext, final Query query) {
        if (_log.isInfoEnabled()) {
            _log.info("Search against Elasticsearch with SearchContext");
        }
        Hits hits = new HitsImpl();
        hits.setStart(new Date().getTime());

        Client client = this._esConnector.getClient();

        String keywords = searchContext.getKeywords();

        String queryString = query.toString();
        queryString = escape(queryString);

        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(queryString);
        SearchRequestBuilder searchRequestBuilder = client
                .prepareSearch(
                        ElasticSearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX)
                .setQuery(queryBuilder);

        // Handle Search Facet queries
        handleFacetQueries(searchContext, searchRequestBuilder);
        SearchResponse response;
        if (getSort(searchContext.getSorts()) != null) {
            searchRequestBuilder = searchRequestBuilder.setFrom(searchContext.getStart())
                    .setSize(searchContext.getEnd())
                    .addSort(getSort(searchContext.getSorts()));
        } else {
            searchRequestBuilder = searchRequestBuilder.setFrom(searchContext.getStart())
                    .setSize(searchContext.getEnd());
        }
        response = searchRequestBuilder.execute().actionGet();
        collectFacetResults(searchContext, response);

        SearchHits searchHits = response.getHits();
        hits.setDocs(getDocuments(searchHits, searchContext));
        hits.setScores(getScores(searchHits));
        hits.setSearchTime(
                (float) (System.currentTimeMillis() - hits.getStart()) / Time.SECOND);
        hits.setQuery(query);
        if (keywords != null) {
            hits.setQueryTerms(keywords.split(StringPool.SPACE));
        }
        hits.setLength((int) searchHits.getTotalHits());
        hits.setStart(hits.getStart());

        return hits;
    }

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
        if (_log.isInfoEnabled()) {
            _log.info("Search against Elasticsearch with query, sort, start and end parameters");
        }
        Hits hits = new HitsImpl();
        Client client = this._esConnector.getClient();

        String queryString = query.toString();
        queryString = escape(queryString);

        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(queryString);
        SearchRequestBuilder searchRequestBuilder = client
                .prepareSearch(ElasticSearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX)
                .setQuery(queryBuilder);
        SearchResponse response;
        if (getSort(sort) != null) {
            response = searchRequestBuilder.setFrom(start)
                    .setSize(end)
                    .addSort(getSort(sort))
                    .execute().actionGet();
        } else {
            response = searchRequestBuilder.setFrom(start)
                    .setSize(end)
                    .execute().actionGet();
        }
        SearchHits searchHits = response.getHits();
        hits.setDocs(getDocuments(searchHits));
        hits.setScores(getScores(searchHits));
        hits.setSearchTime((float) (System.currentTimeMillis() - hits.getStart()) / Time.SECOND);
        hits.setQuery(query);
        hits.setLength((int) searchHits.getTotalHits());
        hits.setStart(hits.getStart());

        return hits;
    }

    /**
     * get SortBuilder based on sort array sent by Liferay.
     * @param sorts sorts
     * @return sort builder
     */
    private SortBuilder getSort(final Sort[] sorts) {
        SortBuilder sortBuilder = null;
        if (sorts != null) {
            for (Sort sort : sorts) {
                if (sort != null && sort.getFieldName() != null) {
                    sortBuilder = SortBuilders.fieldSort(sort.getFieldName())
                            .ignoreUnmapped(true)
                            .order((sort.isReverse()) ? SortOrder.DESC : SortOrder.ASC);
                }
            }
        }

        return sortBuilder;
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
     * @return the documents
     */
    private Document[] getDocuments(final SearchHits searchHits) {
        if (_log.isInfoEnabled()) {
            _log.info("Getting document objects from SearchHits");
        }
        int total = Integer.parseInt((searchHits != null)? String.valueOf(searchHits.getTotalHits()) : "0");
        int failedJsonCount = 0;
        if (total > 0) {
            List<Document> documentsList = new ArrayList<Document>(total);
            @SuppressWarnings("rawtypes")
            Iterator itr = searchHits.iterator();
            while (itr.hasNext()) {
                Document document = new DocumentImpl();
                SearchHit hit = (SearchHit) itr.next();

                JSONObject json;
                try {
                    json = JSONFactoryUtil.createJSONObject(hit.getSourceAsString());
                    @SuppressWarnings("rawtypes")
                    Iterator jsonItr = json.keys();
                    while (jsonItr.hasNext()) {
                        String key = (String) jsonItr.next();
                        String value = json.getString(key);
                        if (_log.isDebugEnabled()) {
                            _log.debug(">>>>>>>>>> " + key + " : " + value);
                        }
                        document.add(new Field(key, value));
                    }
                    documentsList.add(document);
                } catch (JSONException e) {
                    failedJsonCount++;
                    _log.error("Error while processing the search result json objects", e);
                }
            }
            if (_log.isInfoEnabled()) {
                _log.info("Total size of the search results: " + documentsList.size());
            }
            return documentsList.toArray(new Document[documentsList.size() - failedJsonCount]);
        } else {
            if (_log.isInfoEnabled()) {
                _log.info("No search results found");
            }
            return new Document[0];
        }
    }

    /**
     * Gets the documents.
     *
     * @param searchHits the search hits
     * @param searchContext the search context
     * @return the documents
     */
    private Document[] getDocuments(final SearchHits searchHits, final SearchContext searchContext) {
        _log.info("Getting document objects from SearchHits");

        String[] types = searchContext.getEntryClassNames();


        if (searchHits != null && searchHits.getTotalHits() > 0) {
            int failedJsonCount = 0;
            String className = null;

            int total = Integer.parseInt(String.valueOf(searchHits.getTotalHits()));
            List<Document> documentsList = new ArrayList<Document>(total);
            for (SearchHit hit : searchHits.getHits()) {
                Document document = new DocumentImpl();
                JSONObject json;
                try {
                    json = JSONFactoryUtil.createJSONObject(hit.getSourceAsString());
                    @SuppressWarnings("rawtypes")
                    Iterator jsonItr = json.keys();
                    while (jsonItr.hasNext()) {
                        String key = (String) jsonItr.next();
                        String value = json.getString(key);
                        if (_log.isDebugEnabled()) {
                            _log.debug(">>>>>>>>>> " + key + " : " + value);
                        }
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
                    _log.error("Error while processing the search result json objects", e);
                }
            }

            _log.info("Total size of the search results: " + documentsList.size());
            return documentsList.toArray(new Document[documentsList.size() - failedJsonCount]);
        } else {
            _log.info("No search results found");
            return new Document[0];
        }
    }

    /**
     * Parses the es facet to return a map with Entryclassname and its count.
     *
     * @param esFacet the es facet
     * @return the map
     */
    private Map<String, Integer> parseESFacet(Aggregation esFacet) {
    	Terms terms = (Terms) esFacet;
    	Collection<Terms.Bucket> buckets = terms.getBuckets();
      Map<String, Integer> esTermFacetResultMap = new HashMap<String, Integer>();
      for (Terms.Bucket bucket : buckets) {
          esTermFacetResultMap.put(bucket.getKeyAsString(), (int) bucket.getDocCount());
      }

        return esTermFacetResultMap;
    }
    

    /**
     * This method adds multiple facets to Elastic search query builder.
     *
     * @param searchContext the search context
     * @param searchRequestBuilder the search request builder
     */
    private void handleFacetQueries(SearchContext searchContext, SearchRequestBuilder searchRequestBuilder) {
        Map<String, Facet> facets = searchContext.getFacets();
        for (Facet facet : facets.values()) {
            if (!facet.isStatic()) {

                FacetConfiguration liferayFacetConfiguration = facet.getFacetConfiguration();
                JSONObject liferayFacetDataJSONObject = liferayFacetConfiguration.getData();
                if (facet instanceof MultiValueFacet) {


                    TermsBuilder termsFacetBuilder = AggregationBuilders.terms(liferayFacetConfiguration.getFieldName());
                    termsFacetBuilder.field(liferayFacetConfiguration.getFieldName());
                    if (liferayFacetDataJSONObject.has(ElasticSearchIndexerConstants.ELASTIC_SEARCH_MAXTERMS)) {
                        termsFacetBuilder.size(liferayFacetDataJSONObject.getInt(ElasticSearchIndexerConstants.ELASTIC_SEARCH_MAXTERMS));
                    }
                    searchRequestBuilder.addAggregation(termsFacetBuilder);
                } else if (facet instanceof RangeFacet) {
                    RangeBuilder rangeFacetBuilder = AggregationBuilders.range(liferayFacetConfiguration.getFieldName());

                    /**
                     *A typical ranges array looks like below.
                     *[{"range":"[20140603200000 TO 20140603220000]","label":"past-hour"},{"range":"[20140602210000 TO 20140603220000]","label":"past-24-hours"},...]
                     *
                     */
                    JSONArray rangesJSONArray = liferayFacetDataJSONObject.getJSONArray(ElasticSearchIndexerConstants.ELASTIC_SEARCH_RANGES);
                    rangeFacetBuilder.field(ElasticSearchIndexerConstants.ELASTIC_SEARCH_INNERFIELD_MDATE);
                    if (rangesJSONArray != null) {
                        for (int i = 0; i < rangesJSONArray.length(); i++) {
                            JSONObject rangeJSONObject = rangesJSONArray.getJSONObject(i);
                            String[] fromTovalues = fetchFromToValuesInRage(rangeJSONObject);
                            if(fromTovalues != null){
                                rangeFacetBuilder.addRange(Double.parseDouble(fromTovalues[0].trim()), Double.parseDouble(fromTovalues[1].trim()));
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
    private void collectFacetResults(SearchContext searchContext, SearchResponse response) {

        for(Entry<String, Facet> facetEntry: searchContext.getFacets().entrySet()) {
            Facet liferayFacet = facetEntry.getValue();
            if(!liferayFacet.isStatic()){
                Aggregation esFacet = response.getAggregations().get(facetEntry.getKey());
                if(esFacet != null) {
                    FacetCollector facetCollector = null;
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
                        if(_log.isDebugEnabled()){
                            _log.debug("Handling AssetEntriesFacet now for field:"+facetEntry.getKey()+"...");
                        }
                        Map<String, Integer> esTermsFacetResults = parseESFacet(esFacet);
                        facetResults = new HashMap<String, Integer>();

                        for (String entryClassname : fetchEntryClassnames(liferayFacet)) {

                            if (esTermsFacetResults.get(entryClassname.toLowerCase()) != null) {
                                facetResults.put(entryClassname, esTermsFacetResults.get(entryClassname.toLowerCase()));
                            } else {
                                facetResults.put(entryClassname, new Integer(0));
                            }
                            if(_log.isDebugEnabled()){
                                _log.debug("AssetEntriesFacet>>>>>>>>>>>>Term:"+entryClassname+" <<<<Count:"+esTermsFacetResults.get(entryClassname.toLowerCase()));
                            }
                        }

                    } else if ((liferayFacet instanceof MultiValueFacet)) {
                        facetResults = new HashMap<String, Integer>();
                        Terms esTermsFacetResults = (Terms) esFacet;
                        Collection<Terms.Bucket> buckets = esTermsFacetResults.getBuckets();
                        for (Terms.Bucket bucket : buckets) {
                            facetResults.put(bucket.getKeyAsString(), (int) bucket.getDocCount());
                            if(_log.isDebugEnabled()){
                                _log.debug("MultiValueFacet>>>>>>>>>>>>Term:"+bucket.getKeyAsString()+" <<<<Count:"+bucket.getDocCount());
                            }
                        }
                    } else if ((liferayFacet instanceof RangeFacet)) {
                    	Range esRange = (Range) esFacet;
                        facetResults = new HashMap<String, Integer>();
                        for(Range.Bucket entry : esRange.getBuckets()){
                            facetResults.put(buildRangeTerm(entry), new Integer((int) entry.getDocCount()));
                            if(_log.isDebugEnabled()){
                                _log.debug(">>>>>>>From:"+entry.getFromAsString()+">>>>>>>To:"+entry.getToAsString()+">>>>>>>Count:"+entry.getDocCount());
                            }
                        }
                    }

                    facetCollector = new ElasticSearchQueryFacetCollector(facetEntry.getKey(), facetResults);
                    liferayFacet.setFacetCollector(facetCollector);
                    if(_log.isDebugEnabled()){
                        _log.debug("Facet collector successfully set for field:"+facetEntry.getKey()+"...");
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
    private String buildRangeTerm(Range.Bucket entry){

        StringBuilder termBuilder = new StringBuilder();
        termBuilder.append(StringPool.OPEN_BRACKET);
        termBuilder.append(entry.getFromAsString());
        termBuilder.append(StringPool.SPACE);
        termBuilder.append(ElasticSearchIndexerConstants.ELASTIC_SEARCH_TO);
        termBuilder.append(StringPool.SPACE);
        termBuilder.append(entry.getToAsString());
        termBuilder.append(StringPool.CLOSE_BRACKET);
        return termBuilder.toString();
    }

    /**
     * Fetch entry classnames.
     *
     * @param liferayFacet the liferay facet
     * @return the sets the
     */
    private Set<String> fetchEntryClassnames(Facet liferayFacet) {
        JSONObject dataJSONObject = liferayFacet.getFacetConfiguration().getData();
        JSONArray valuesArray = dataJSONObject.getJSONArray(ElasticSearchIndexerConstants.ELASTIC_SEARCH_VALUES);
        Set<String> entryClassnames = new HashSet<String>();
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
    private String[] fetchFromToValuesInRage(JSONObject jsonObject){
        String fromToFormatRange = jsonObject.getString(ElasticSearchIndexerConstants.ELASTIC_SEARCH_RANGE);
        String[] fromToArray = null;
        if(fromToFormatRange != null && fromToFormatRange.length() > 0){
            fromToArray = fromToFormatRange.substring(1, fromToFormatRange.length()-1).split(ElasticSearchIndexerConstants.ELASTIC_SEARCH_TO);
        }
        return fromToArray;
    }

    public static String escape(final String s) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if(c == 47) {
                sb.append('\\');
            }

            sb.append(c);
        }

        return sb.toString();
    }
}