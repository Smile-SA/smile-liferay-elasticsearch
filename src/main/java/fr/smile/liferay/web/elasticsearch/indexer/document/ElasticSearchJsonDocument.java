package fr.smile.liferay.web.elasticsearch.indexer.document;

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
        private DocumentError(String value) {
            this.errorMsg = value;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        public String toString(){
            return errorMsg;

        }
    };


    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the index type.
     *
     * @return the index type
     */
    public String getIndexType() {
        return indexType;
    }

    /**
     * Sets the index type.
     *
     * @param indexType the new index type
     */
    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    /**
     * Gets the json document.
     *
     * @return the json document
     */
    public String getJsonDocument() {
        return jsonDocument;
    }

    /**
     * Sets the json document.
     *
     * @param jsonDocument the new json document
     */
    public void setJsonDocument(String jsonDocument) {
        this.jsonDocument = jsonDocument;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage the new error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Checks if is error.
     *
     * @return true, if is error
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Sets the error.
     *
     * @param isError the new error
     */
    public void setError(boolean isError) {
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
