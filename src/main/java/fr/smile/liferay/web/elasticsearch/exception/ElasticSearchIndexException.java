package fr.smile.liferay.web.elasticsearch.exception;

/**
 * @author marem
 * @since 30/10/15.
 */
public class ElasticSearchIndexException extends Exception {

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

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4419932835671285073L;

}
