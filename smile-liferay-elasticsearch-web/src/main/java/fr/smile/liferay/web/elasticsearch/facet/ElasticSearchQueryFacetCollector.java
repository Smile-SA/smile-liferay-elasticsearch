package fr.smile.liferay.web.elasticsearch.facet;

import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import fr.smile.liferay.web.elasticsearch.util.Ranges;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query Facet Collector.
 */
public class ElasticSearchQueryFacetCollector implements FacetCollector {

    /** The counts. */
    private Map<String, Integer> counts = new HashMap<>();

    /** The _field name. */
    private String fieldName;

    /** The _term collectors. */
    private List<TermCollector> termCollectors;

    /**
     * Instantiates a new elasticsearch query facet collector.
     *
     * @param fieldName the field name
     * @param facetResults the facet results
     */
    public ElasticSearchQueryFacetCollector(final String fieldName, final Map<String, Integer> facetResults) {
        this.fieldName = fieldName;
        this.counts.putAll(facetResults);
    }

    @Override
    public final String getFieldName() {
        return fieldName;
    }

    @Override
    public final TermCollector getTermCollector(final String term) {
        int count = 0;

        if (counts.containsKey(term)) {
            count = counts.get(term);
        }
        else {
            String normalizeLabel = normalizeRangeLabel(term);
            if (!normalizeLabel.equals(term)) {
                count = counts.get(normalizeLabel);
            }
        }
        return new ElasticSearchDefaultTermCollector(term, count);
    }

    @Override
    public final List<TermCollector> getTermCollectors() {
        if (this.termCollectors == null) {
            termCollectors = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                TermCollector termCollector = new ElasticSearchDefaultTermCollector(entry.getKey(), entry.getValue());
                termCollectors.add(termCollector);
            }
        }
        return termCollectors;
    }

    private String normalizeRangeLabel(String dateLabel) {

        Calendar now = Calendar.getInstance();

        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MINUTE, 0);

        Calendar date = (Calendar)now.clone();

        switch (dateLabel) {
            case "past-hour":
                date.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) - 1);
                break;
            case "past-24-hours":
                date.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) - 1);
                break;
            case "past-week":
                date.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) - 7);
                break;
            case "past-month":
                date.set(Calendar.MONTH, now.get(Calendar.MONTH) - 1);
                break;
            case "past-year":
                date.set(Calendar.YEAR, now.get(Calendar.YEAR) - 1);
                break;
            default:
                return dateLabel;
        }
        now.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) + 1);
        DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat("yyyyMMddHHmmss");

        return Ranges.toRange(dateFormat.format(date.getTime()), dateFormat.format(now.getTime()));
    }

}
