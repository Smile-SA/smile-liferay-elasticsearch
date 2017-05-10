package fr.smile.liferay.web.elasticsearch.facet;

import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import fr.smile.liferay.web.elasticsearch.api.EsSearchApiService;

import java.text.DateFormat;
import java.util.*;

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
        int count = 0;

        if (this.counts.get(term) != null) {
            count = this.counts.get(term);
        }
        else {
            String normalizeLabel = normalizeRangeLabel(term);
            if (!normalizeLabel.equals(term)) {
                count = this.counts.get(normalizeLabel);
            }
        }
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
        DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
                "yyyyMMddHHmmss");
        return StringPool.OPEN_BRACKET + dateFormat.format(date.getTime())
                + StringPool.SPACE + EsSearchApiService.ELASTIC_SEARCH_TO
                + StringPool.SPACE + dateFormat.format(now.getTime())
                + StringPool.CLOSE_BRACKET;
    }

    /** The counts. */
    private Map<String, Integer> counts = new HashMap<String, Integer>();

    /** The _field name. */
    private String fieldName;

    /** The _term collectors. */
    private List<TermCollector> termCollectors;
}
