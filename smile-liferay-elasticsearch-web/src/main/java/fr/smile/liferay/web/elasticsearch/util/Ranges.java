package fr.smile.liferay.web.elasticsearch.util;

import com.liferay.portal.kernel.util.StringPool;
import fr.smile.liferay.web.elasticsearch.api.Constant;

/**
 * Creates a representation of ranges with the syntax allowed by ElasticSearch.
 */
public class Ranges {

    /**
     * Build a string representation of a range.
     * @param from range from
     * @param to range to
     * @return a string representation of a range
     */
    public static String toRange(String from, String to) {
        return StringPool.OPEN_BRACKET + from
                + StringPool.SPACE + Constant.ELASTIC_SEARCH_TO
                + StringPool.SPACE + to
                + StringPool.CLOSE_BRACKET;
    }

    /**
     * Build a string representation of a range.
     * @param from range from
     * @param to range to
     * @return a string representation of a range
     */
    public static String toRange(long from, long to) {
        return toRange(String.valueOf(from), String.valueOf(to));
    }

}
