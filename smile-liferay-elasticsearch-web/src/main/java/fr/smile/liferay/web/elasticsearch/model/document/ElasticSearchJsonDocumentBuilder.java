package fr.smile.liferay.web.elasticsearch.model.document;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.LocaleUtil;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * {@link ElasticSearchJsonDocument} creation service.
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
            excludedTypes = new HashSet<>();
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
     * Suffix a string with a "_sortable" string.
     * @param str the string to concat with the suffix
     * @return the string suffixed
     */
    private String suffixWithSortable(String str) {
        return str + StringPool.UNDERLINE + "sortable";
    }

    /**
     * Suffix a string with a locale string format.
     * @param str the string to concat
     * @param locale the locale
     * @return the string localized
     */
    private String localized(String str, Locale locale) {
        return str + StringPool.UNDERLINE + locale.toString();
    }

    /**
     * Fill content builder with a field.
     * @param contentBuilder the content builder
     * @param field the field
     * @param name the field name
     * @throws IOException any io exception that could happen in treatment
     */
    private void buildField(XContentBuilder contentBuilder, Field field, String name) throws IOException {
        if (field.isLocalized()) {
            buildLocalizedField(contentBuilder, field, name);
        } else {
            buildSimpleField(contentBuilder, field, name);
        }
    }

    /**
     * Fill content builder with a localized field.
     * @param contentBuilder the content builder
     * @param field the field
     * @param name the field name
     * @throws IOException any io exception that could happen in treatment
     */
    private void buildLocalizedField(XContentBuilder contentBuilder, Field field, String name) throws IOException {
        Map<Locale, String> fieldValues = field.getLocalizedValues();

        Locale locale;
        String value;
        boolean sortable;
        for (Map.Entry<Locale, String> localeEntry : fieldValues.entrySet()) {
            locale = localeEntry.getKey();
            value = localeEntry.getValue();
            sortable = field.isSortable();

            String languageId = LocaleUtil.toLanguageId(locale);
            String defaultLanguageId = LocaleUtil.toLanguageId(LocaleUtil.getDefault());

            if (languageId.equals(defaultLanguageId)) {
                contentBuilder.field(name, value);
            }

            if (sortable) {
                contentBuilder.field(suffixWithSortable(name), value);
            }

            if (value != null && !value.isEmpty()) {
                contentBuilder.field(localized(name, locale), value);

                if (sortable) {
                    contentBuilder.field(suffixWithSortable(localized(name, locale)), value);
                }
            }
        }
    }

    /**
     * Fill content builder with a simple field (not localized).
     * @param contentBuilder the content builder
     * @param field the field
     * @param name the field name
     * @throws IOException any io exception that could happen in treatment
     */
    private void buildSimpleField(XContentBuilder contentBuilder, Field field, String name) throws IOException {
        String[] fieldValues = field.getValues();
        Collection<String> values = null;

        if (fieldValues != null && fieldValues.length > 1) {
            List<String> tempList = Arrays.asList(fieldValues);

            Predicate<String> isNotEmptyElementPredicate = new Predicate<String>() {
                public boolean apply(final String p) {
                    return p.trim().length() > 0;
                }
            };

            values = Collections2.filter(tempList, isNotEmptyElementPredicate);
        }

        if (values != null && values.size() > 0) {
            contentBuilder.array(name, values.toArray());
        } else {
            String value = field.getValue();
            if (value != null) {
                contentBuilder.field(name, value.trim());
            }
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

        if (isValid(document, elasticsearchJSONDocument)) {
            try {
                XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject();

                /** Create a JSON string for remaining fields of document */
                Map<String, Field> fields = document.getFields();
                for (Map.Entry<String, Field> entry :  fields.entrySet()) {
                    buildField(contentBuilder, entry.getValue(), entry.getKey());
                }
                contentBuilder.endObject();

                elasticsearchJSONDocument.setJsonDocument(contentBuilder.string());
                LOGGER.debug("Liferay Document converted to ESJSON document successfully:" + contentBuilder.string());
            } catch (IOException e) {
                LOGGER.error("IO Error during converstion of Liferay Document to JSON format" + e.getMessage());
            }
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
        if (hiddenField != null) {
            return Boolean.getBoolean(hiddenField.getValue());
        }
        return false;
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
