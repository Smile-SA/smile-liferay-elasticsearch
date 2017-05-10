package fr.smile.liferay.elasticsearch.client.model;

/**
 * @author marem
 * @since 30/10/15.
 */
public class ElasticSearchJsonDocument {
    /**
     * The Enum DocumentError.
     */
    public enum DocumentError {

        /** The missing classpk. */
        HIDDEN_DOCUMENT("liferay document is of hidden type"),

        /** The missing classpk. */
        MISSING_CLASSPK("entryClassPK is missing from document object"),

        /** The missing entryclassname. */
        MISSING_ENTRYCLASSNAME("entryClassName is missing from document object"),

        /** The excluded type. */
        EXCLUDED_TYPE("this type is not supported at this time");

        /** The error msg. */
        private String errorMsg;

        /**
         * Instantiates a new document error.
         *
         * @param value the value
         */
        private DocumentError(final String value) {
            this.errorMsg = value;
        }

        @Override
        public String toString() {
            return errorMsg;
        }
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public final String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the index type.
     *
     * @return the index type
     */
    public final String getIndexType() {
        return indexType;
    }

    /**
     * Sets the index type.
     *
     * @param indexType the new index type
     */
    public final void setIndexType(final String indexType) {
        this.indexType = indexType;
    }

    /**
     * Gets the json document.
     *
     * @return the json document
     */
    public final String getJsonDocument() {
        return jsonDocument;
    }

    /**
     * Sets the json document.
     *
     * @param jsonDocument the new json document
     */
    public final void setJsonDocument(final String jsonDocument) {
        this.jsonDocument = jsonDocument;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public final String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage the new error message
     */
    public final void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Checks if is error.
     *
     * @return true, if is error
     */
    public final boolean isError() {
        return isError;
    }

    /**
     * Sets the error.
     *
     * @param isError the new error
     */
    public final void setError(final boolean isError) {
        this.isError = isError;
    }

    /** The id. */
    private String id;

    /** The index type. */
    private String indexType;

    /** The json document. */
    private String jsonDocument;

    /** The error message. */
    private String errorMessage;

    /** The is error. */
    private boolean isError;
}
