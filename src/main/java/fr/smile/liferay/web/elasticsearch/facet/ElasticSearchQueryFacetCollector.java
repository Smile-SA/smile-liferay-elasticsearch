package fr.smile.liferay.web.elasticsearch.facet;

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
    /**
     * Instantiates a new elasticsearch query facet collector.
     *
     * @param fieldName the field name
     * @param facetResults the facet results
     */
    public ElasticSearchQueryFacetCollector(String fieldName, Map<String, Integer> facetResults) {
        this._fieldName = fieldName;

        for (Map.Entry<String, Integer> entry : facetResults.entrySet()) {
            String term = entry.getKey();
            Integer count = entry.getValue();

            this._counts.put(term, count);
        }
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.FacetCollector#getFieldName()
     */
    public String getFieldName() {
        return this._fieldName;
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.FacetCollector#getTermCollector(java.lang.String)
     */
    public TermCollector getTermCollector(String term) {
        Integer count = this._counts.get(term);

        return new ElasticSearchDefaultTermCollector(term, count.intValue());
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.FacetCollector#getTermCollectors()
     */
    public List<TermCollector> getTermCollectors() {
        if (this._termCollectors != null) {
            return this._termCollectors;
        }

        List<TermCollector> termCollectors = new ArrayList<TermCollector>();

        for (Map.Entry<String, Integer> entry : this._counts.entrySet()) {
            Integer count = entry.getValue();

            TermCollector termCollector = new ElasticSearchDefaultTermCollector((String) entry.getKey(),
                    count.intValue());

            termCollectors.add(termCollector);
        }

        this._termCollectors = termCollectors;

        return this._termCollectors;
    }

    /** The _counts. */
    private Map<String, Integer> _counts = new HashMap<String, Integer>();

    /** The _field name. */
    private String _fieldName;

    /** The _term collectors. */
    private List<TermCollector> _termCollectors;
}
