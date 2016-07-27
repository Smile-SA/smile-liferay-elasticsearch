package fr.smile.liferay.elasticsearch.management.model;

/**
 * @author marem
 * @since 27/07/16.
 */
public class Index {

    private String name;

    private String type;

    private int totalDocuments;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(final int totalDocuments) {
        this.totalDocuments = totalDocuments;
    }
}
