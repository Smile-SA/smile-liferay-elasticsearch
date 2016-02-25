package fr.smile.liferay.web.elasticsearch.facet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author marem
 * @since 30/10/15.
 */
public class ElasticSearchQueryFacetCollector implements FacetCollector {
    private static final Log LOGGER = LogFactoryUtil.getLog(ElasticSearchQueryFacetCollector.class);
    /**
     * Instantiates a new elasticsearch query facet collector.
     *
     * @param fieldName the field name
     * @param facetResults the facet results
     */
    public ElasticSearchQueryFacetCollector(final String fieldName, final Map<String, Integer> facetResults) {
        this.fieldName = fieldName;

        for (Map.Entry<String, Integer> entry : facetResults.entrySet()) {
            String term = entry.getKey();
            Integer count = entry.getValue();

            this.counts.put(term, count);
        }
    }

    @Override
    public final String getFieldName() {
        return this.fieldName;
    }

    @Override
    public final TermCollector getTermCollector(final String term) {
        Integer count = this.counts.get(term);
        LOGGER.info("Term : " + term + " " + " count : "+ count );
        count = count == null ? 0 : count;
        return new ElasticSearchDefaultTermCollector(term, count);
    }

    @Override
    public final List<TermCollector> getTermCollectors() {
        if (this.termCollectors != null) {
            return this.termCollectors;
        }

        List<TermCollector> termCollectors = new ArrayList<TermCollector>();

        for (Map.Entry<String, Integer> entry : this.counts.entrySet()) {
            Integer count = entry.getValue();
            TermCollector termCollector = new ElasticSearchDefaultTermCollector(entry.getKey(),  count);
            termCollectors.add(termCollector);
        }

        this.termCollectors = termCollectors;

        return this.termCollectors;
    }

    /** The counts. */
    private Map<String, Integer> counts = new HashMap<String, Integer>();

    /** The _field name. */
    private String fieldName;

    /** The _term collectors. */
    private List<TermCollector> termCollectors;
}
