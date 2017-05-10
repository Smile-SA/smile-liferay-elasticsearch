package fr.smile.liferay.web.elasticsearch.facet;

import com.liferay.portal.kernel.search.facet.collector.TermCollector;

/**
 * Default Term Collector.
 */
public class ElasticSearchDefaultTermCollector implements TermCollector {

    /** The term. */
    private String term;

    /** The frequency. */
    private int frequency;

    /**
     * Instantiates a new elasticsearch default term collector.
     *
     * @param term the term
     * @param frequency the frequency
     */
    public ElasticSearchDefaultTermCollector(final String term, final int frequency) {
        this.term = term;
        this.frequency = frequency;
    }

    @Override
    public final String getTerm() {
        return term;
    }

    @Override
    public final int getFrequency() {
        return frequency;
    }
}
