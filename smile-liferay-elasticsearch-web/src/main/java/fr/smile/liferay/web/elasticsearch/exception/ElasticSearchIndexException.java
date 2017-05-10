package fr.smile.liferay.web.elasticsearch.exception;

/**
 * Exception thrown at every index-related error.
 */
public class ElasticSearchIndexException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4419932835671285073L;

    /**
     * Instantiates a new elasticsearch indexing exception.
     */
    public ElasticSearchIndexException() {
    }

    /**
     * Instantiates a new elasticsearch indexing exception.
     *
     * @param msg the msg
     */
    public ElasticSearchIndexException(final String msg) {
        super(msg);
    }

    /**
     * Instantiates a new elasticsearch indexing exception.
     *
     * @param t the t
     */
    public ElasticSearchIndexException(final Throwable t) {
        super(t);
    }

    /**
     * Instantiates a new elasticsearch indexing exception.
     *
     * @param msg the msg
     * @param t the t
     */
    public ElasticSearchIndexException(final String msg, final Throwable t) {
        super(msg, t);
    }

}
