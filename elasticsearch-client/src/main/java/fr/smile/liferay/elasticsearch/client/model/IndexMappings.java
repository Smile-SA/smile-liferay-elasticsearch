package fr.smile.liferay.elasticsearch.client.model;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Index mappings representation.
 */
@XmlRootElement
public class IndexMappings extends IndexConfig {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(IndexMappings.class);

    @JsonProperty("mappings")
    private Map<String, Object> mappings;

    /**
     * Default constructor.
     */
    public IndexMappings() {
    }

    /**
     * Get the index mappings.
     * @return the mappings
     */
    public Map<String, Object> getMappings() {
        return mappings;
    }

    /**
     * Set the index mappings.
     * @param mappings the index mappings
     */
    public void setMappings(Map<String, Object> mappings) {
        this.mappings = mappings;
    }

    /**
     * Extract the index mappings from the ElasticSearch [@link MappingMetaData}.
     * @param indexName the index name
     * @param esMappings the ElasticSearch mappings
     * @return an instance of {@link IndexMappings}
     */
    public static IndexMappings from(String indexName, ImmutableOpenMap<String, MappingMetaData> esMappings) {
        if (esMappings != null && !esMappings.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            Iterator<ObjectObjectCursor<String, MappingMetaData>> itValues = esMappings.iterator();
            while (itValues.hasNext()) {
                ObjectObjectCursor<String, MappingMetaData> cursorValues = itValues.next();
                try {
                    map.put(cursorValues.key, cursorValues.value.getSourceAsMap().get("properties"));
                } catch (IOException e) {
                    LOGGER.error("An error occured while retrieving values for mapping " + cursorValues.key);
                }
            }

            IndexMappings indexMappings = new IndexMappings();
            indexMappings.setIndex(indexName);
            indexMappings.setMappings(map);
            return indexMappings;
        }
        return null;
    }
}
