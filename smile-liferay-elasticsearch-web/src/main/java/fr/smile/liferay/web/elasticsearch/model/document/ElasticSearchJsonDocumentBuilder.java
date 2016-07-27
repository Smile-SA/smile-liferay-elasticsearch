package fr.smile.liferay.web.elasticsearch.model.document;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import fr.smile.liferay.elasticsearch.client.model.ElasticSearchJsonDocument;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author marem
 * @since 30/10/15.
 */
@Service
public class ElasticSearchJsonDocumentBuilder {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(ElasticSearchJsonDocumentBuilder.class);

    /**
     * Document type.
     */
    public static final String DOCUMENT_TYPE = "LiferayAssetType";

    /**
     * Exclude types.
     */
    @Value("${indexExcludedType}")
    private String excludedTypesProperty;

    /** The excluded types. */
    private Set<String> excludedTypes;

    /**
     * Init method.
     */
    @PostConstruct
    public final void loadExcludedTypes() {
        if (Validator.isNotNull(excludedTypesProperty)) {
            excludedTypes = new HashSet<String>();
            String[] excludedTypesArray = excludedTypesProperty.split(StringPool.COMMA);
            for (String excludedType : Arrays.asList(excludedTypesArray)) {
                excludedTypes.add(excludedType);
            }
            LOGGER.debug("Loaded Excluded index types are:" + excludedTypesProperty);
        } else {
            LOGGER.debug("Excluded index types are not defined");
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


        ElasticSearchJsonDocument elasticsearchJSONDocument = new ElasticSearchJsonDocument();
        try {
            XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject();

            if (!isValid(document, elasticsearchJSONDocument)) {
                return elasticsearchJSONDocument;
            }

            /** Create a JSON string for remaining fields of document */
            Map<String, Field> fields = document.getFields();
            for (Map.Entry<String, Field> entry :  fields.entrySet()) {
                Field field = entry.getValue();

                Collection<String> values = null;
                if (field.getValues() != null && field.getValues().length > 1) {
                    List<String> tempList = Arrays.asList(field.getValues());

                    Predicate<String> isNotEmptyElementPredicate = new Predicate<String>() {
                        public boolean apply(final String p) {
                            String value = p.trim();
                            return !"".equals(value);
                        }
                    };

                    values = Collections2.filter(tempList, isNotEmptyElementPredicate);
                }

                if (values != null && values.size() > 0) {
                    contentBuilder.array(entry.getKey(), values.toArray());
                } else {
                    String value = field.getValue();
                    if (value != null) {
                        contentBuilder.field(entry.getKey(), value.trim());
                    }
                }
            }
            contentBuilder.endObject();

            elasticsearchJSONDocument.setJsonDocument(contentBuilder.string());
            LOGGER.debug("Liferay Document converted to ESJSON document successfully:" + contentBuilder.string());
        } catch (IOException e) {
            LOGGER.error("IO Error during converstion of Liferay Document to JSON format" + e.getMessage());
        }
        return elasticsearchJSONDocument;
    }

    /**
     * Checks if liferay document is valid to index.
     * @param liferayDocument liferay document
     * @param document document to index
     * @return true if valid
     */
    private boolean isValid(final Document liferayDocument, final ElasticSearchJsonDocument document) {
        Map<String, Field> fields = liferayDocument.getFields();
        Field classnameField = fields.get(Field.ENTRY_CLASS_NAME);
        String entryClassName = "";
        if (classnameField != null) {
            entryClassName = classnameField.getValue();
        }

        /**
         * To avoid multiple documents for versioned assets such as Journal articles, DL entry etc
         * the primary Id will be Indextype + Entry class PK. The primary Id is to maintain uniqueness
         * in ES server database and nothing to do with UID or is not used for any other purpose.
         */
        Field classPKField = fields.get(Field.ENTRY_CLASS_PK);
        String entryClassPK = "";
        if (classPKField != null) {
            entryClassPK = classPKField.getValue();
        }
        if (entryClassPK.isEmpty()) {
            document.setError(true);
            document.setErrorMessage(
                    "" + ElasticSearchJsonDocument.DocumentError.MISSING_CLASSPK
            );
            return false;
        }

        /** Replace '.' by '_' in Entry class name,since '.' is not recommended by Elasticsearch in Index type */
        String indexType = entryClassName.replace(StringPool.PERIOD, StringPool.UNDERLINE);
        document.setIndexType(indexType);
        document.setId(indexType + entryClassPK);

        if (isDocumentHidden(liferayDocument)) {
            document.setError(true);
            document.setErrorMessage(
                    "" + ElasticSearchJsonDocument.DocumentError.HIDDEN_DOCUMENT
            );
            return false;
        }
        if (entryClassName.isEmpty()) {
            document.setError(true);
            document.setErrorMessage(
                    "" + ElasticSearchJsonDocument.DocumentError.MISSING_ENTRYCLASSNAME
            );
            return false;
        }
        if (isExcludedType(entryClassName)) {
            document.setError(true);
            document.setErrorMessage(
                    "Index Type:" + entryClassName
                            + StringPool.COMMA
                            + ElasticSearchJsonDocument.DocumentError.EXCLUDED_TYPE
            );
            return false;
        }

        return true;
    }


    /**
     * Check if liferay Document is of type hidden.
     *
     * @param document the document
     * @return true, if is document hidden
     */
    private boolean isDocumentHidden(final Document document) {
        Field hiddenField = document.getFields().get(Field.HIDDEN);
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
