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
    public ElasticSearchIndexException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new elasticsearch indexing exception.
     *
     * @param t the t
     */
    public ElasticSearchIndexException(Throwable t) {
        super(t);
    }

    /**
     * Instantiates a new elasticsearch indexing exception.
     *
     * @param msg the msg
     * @param t the t
     */
    public ElasticSearchIndexException(String msg, Throwable t) {
        super(msg, t);
    }

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4419932835671285073L;

}
