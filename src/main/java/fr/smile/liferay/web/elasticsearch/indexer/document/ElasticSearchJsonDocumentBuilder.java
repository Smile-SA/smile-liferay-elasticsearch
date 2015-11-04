package fr.smile.liferay.web.elasticsearch.indexer.document;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.util.portlet.PortletProps;
import fr.smile.liferay.web.elasticsearch.util.ElasticSearchIndexerConstants;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author marem
 * @since 30/10/15.
 */
public class ElasticSearchJsonDocumentBuilder {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(ElasticSearchJsonDocumentBuilder.class);

    /** The excluded types. */
    private Set<String> excludedTypes;

    /**
     * Init method.
     */
    public final void loadExcludedTypes() {
        String cslExcludedType = PortletProps.get(ElasticSearchIndexerConstants.ES_KEY_EXCLUDED_INDEXTYPE);
        if (Validator.isNotNull(cslExcludedType)) {
            excludedTypes = new HashSet<String>();
            String[] excludedTypesArray = cslExcludedType.split(StringPool.COMMA);
            for (String excludedType : excludedTypesArray) {
                excludedTypes.add(excludedType);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Loaded Excluded index types are:" + cslExcludedType);
            }
            LOGGER.info("Loaded Excluded index types are:" + cslExcludedType);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Excluded index types are not defined");
            }
            LOGGER.info("Excluded index types are not defined");
        }
    }

    /**
     * Convert to json.
     *
     * @param document
     *            the document
     * @return the string
     */
    public final ElasticSearchJsonDocument convertToJSON(final Document document) {

        Map<String, Field> fields = document.getFields();
        ElasticSearchJsonDocument elasticserachJSONDocument = new ElasticSearchJsonDocument();

        try {
            XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject();

            Field classnameField = document.getField(ElasticSearchIndexerConstants.ENTRY_CLASSNAME);
            String entryClassName = "";
            if (classnameField != null) {
                entryClassName = classnameField.getValue();
            }

            /**
             * Handle all error scenarios prior to conversion
             */
            if (isDocumentHidden(document)) {
                elasticserachJSONDocument.setError(true);
                elasticserachJSONDocument.setErrorMessage(
                        "" + ElasticSearchJsonDocument.DocumentError.HIDDEN_DOCUMENT
                );
                return elasticserachJSONDocument;
            }
            if (entryClassName.isEmpty()) {
                elasticserachJSONDocument.setError(true);
                elasticserachJSONDocument.setErrorMessage(
                        "" + ElasticSearchJsonDocument.DocumentError.MISSING_ENTRYCLASSNAME
                );
                return elasticserachJSONDocument;
            }
            if (isExcludedType(entryClassName)) {
                elasticserachJSONDocument.setError(true);
                elasticserachJSONDocument.setErrorMessage(
                        "Index Type:" + entryClassName
                        + StringPool.COMMA
                        + ElasticSearchJsonDocument.DocumentError.EXCLUDED_TYPE
                );
                return elasticserachJSONDocument;
            }

            /**
             * To avoid multiple documents for versioned assets such as Journal articles, DL entry etc
             * the primary Id will be Indextype + Entry class PK. The primary Id is to maintain uniqueness
             * in ES server database and nothing to do with UID or is not used for any other purpose.
             */
            Field classPKField = document.getField(ElasticSearchIndexerConstants.ENTRY_CLASSPK);
            String entryClassPK = "";
            if (classPKField != null) {
                entryClassPK = classPKField.getValue();
            }
            if (entryClassPK.isEmpty()) {
                elasticserachJSONDocument.setError(true);
                elasticserachJSONDocument.setErrorMessage(
                        "" + ElasticSearchJsonDocument.DocumentError.MISSING_CLASSPK
                );
            }

            /** Replace '.' by '_' in Entry class name,since '.' is not recommended by Elasticsearch in Index type */
            String indexType = entryClassName.replace(StringPool.PERIOD, StringPool.UNDERLINE);
            elasticserachJSONDocument.setIndexType(indexType);

            elasticserachJSONDocument.setId(indexType + entryClassPK);

            /** Create a JSON string for remaining fields of document */
            for (Map.Entry<String, Field> entry :  fields.entrySet()) {
                Field field = entry.getValue();
                contentBuilder.field(entry.getKey(), field.getValue());
            }
            contentBuilder.endObject();

            elasticserachJSONDocument.setJsonDocument(contentBuilder.string());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Liferay Document converted to ESJSON document successfully:" + contentBuilder.string());
            }
        } catch (IOException e) {
            LOGGER.error("IO Error during converstion of Liferay Document to JSON format" + e.getMessage());
        }
        return elasticserachJSONDocument;
    }


    /**
     * Check if liferay Document is of type hidden.
     *
     * @param document the document
     * @return true, if is document hidden
     */
    private boolean isDocumentHidden(final Document document) {
        Field hiddenField = document.getField(ElasticSearchIndexerConstants.HIDDEN);
        boolean hiddenFlag = false;
        if (hiddenField != null) {
            hiddenFlag = Boolean.getBoolean(hiddenField.getValue());
        }
        return hiddenFlag;
    }

    /**
     * Check if EntryClassname is com.liferay.portal.kernel.plugin.PluginPackage/ExportImportHelper
     * which need not be indexed.
     *
     * @param indexType the index type
     * @return true, if is excluded type
     */
    private boolean isExcludedType(final String indexType) {
        if (indexType != null && excludedTypes != null) {
            for (String excludedType : excludedTypes) {
                if (indexType.toLowerCase().contains(excludedType.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
