package fr.smile.liferay.web.elasticsearch.facet;

import com.liferay.portal.kernel.search.facet.collector.TermCollector;

/**
 * @author marem
 * @since 30/10/15.
 */
public class ElasticSearchDefaultTermCollector implements TermCollector {
    /**
     * Instantiates a new elasticsearch default term collector.
     *
     * @param term the term
     * @param frequency the frequency
     */
    public ElasticSearchDefaultTermCollector(String term, int frequency) {
        this.term = term;
        this.frequency = frequency;
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.TermCollector#getTerm()
     */
    @Override
    public String getTerm() {
        return this.term;
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.TermCollector#getFrequency()
     */
    @Override
    public int getFrequency() {
        return this.frequency;
    }


    /** The term. */
    private String term;

    /** The frequency. */
    private int frequency;
}
