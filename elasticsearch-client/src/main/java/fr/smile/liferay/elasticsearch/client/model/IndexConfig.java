package fr.smile.liferay.elasticsearch.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This object provides a default class for {@link IndexMappings} and {@link IndexSettings}.
 */
@XmlRootElement
abstract class IndexConfig {

    @JsonProperty("index")
    private String index;

    /**
     * Default constructor.
     */
    public IndexConfig() {
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
