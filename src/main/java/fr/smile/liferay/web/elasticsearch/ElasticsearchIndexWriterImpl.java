package fr.smile.liferay.web.elasticsearch;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.*;
import fr.smile.liferay.web.elasticsearch.api.EsIndexApiService;
import fr.smile.liferay.web.elasticsearch.exception.ElasticSearchIndexException;
import fr.smile.liferay.web.elasticsearch.model.document.ElasticSearchJsonDocument;
import fr.smile.liferay.web.elasticsearch.model.document.ElasticSearchJsonDocumentBuilder;
import fr.smile.liferay.web.elasticsearch.model.index.LiferayIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author marem
 * @since 29/10/15.
 */
@Service
public class ElasticsearchIndexWriterImpl implements IndexWriter {

    /** The document json builder. */
    @Autowired
    private ElasticSearchJsonDocumentBuilder documentJSONBuilder;

    @Autowired
    private LiferayIndex index;

    @Autowired
    private EsIndexApiService elasticsearchApiService;

    public static final String VERSION = "version";

    @Override
    public final void addDocument(final SearchContext searchContext, final Document document) throws SearchException {
        LOGGER.debug("Add document for elasticsearch indexing");
        processIt(document);
    }

    @Override
    public final void addDocuments(final SearchContext searchContext, final Collection<Document> documents)
            throws SearchException {
        LOGGER.debug("Add documents for elasticsearch indexing");
        /** This is to sort the Documents with version field from oldest to latest updates to
         retain the modifications */
        DocumentComparator documentComparator = new DocumentComparator(true, false);
        documentComparator.addOrderBy(VERSION);
        Collections.sort((List<Document>) documents, documentComparator);

        for (Document document : documents) {
            updateDocument(searchContext, document);
        }
    }

    @Override
    public final void deleteDocument(final SearchContext searchContext, final String uid) throws SearchException {
        LOGGER.debug("Delete document from elasticsearch indexices");
        elasticsearchApiService.removeDocument(uid, index.getName());
    }

    @Override
    public final void deleteDocuments(final SearchContext searchContext, final Collection<String> uids)
            throws SearchException {
        for (String uid : uids) {
            deleteDocument(searchContext, uid);
        }
    }

    @Override
    public final void deletePortletDocuments(final SearchContext searchContext, final String portletId)
            throws SearchException {
        LOGGER.debug("Delete portlet documents from elasticsearch indexing");
        throw new SearchException("Portlet deployment documents are not supported");
    }

    @Override
    public final void updateDocument(final SearchContext searchContext, final Document document)
            throws SearchException {
        LOGGER.debug("Update document from elasticsearch indexing");
        processIt(document);
    }

    @Override
    public final void updateDocuments(final SearchContext searchContext, final Collection<Document> documents)
            throws SearchException {

        /** This is to sort the Documents with version field from oldest to latest updates to
         retain the modifications */
        DocumentComparator documentComparator = new DocumentComparator(true, false);
        documentComparator.addOrderBy(VERSION);
        Collections.sort((List<Document>) documents, documentComparator);

        for (Document document : documents) {
            updateDocument(searchContext, document);
        }
    }

    /**
     * Process it.
     *
     * @param document
     *            the document
     * @throws SearchException
     *             the search exception
     */
    private void processIt(final Document document) throws SearchException {
        LOGGER.debug("Processing document for elasticsearch indexing");
        try {
            ElasticSearchJsonDocument elasticserachJSONDocument = processDocument(document);
            elasticsearchApiService.writeDocument(elasticserachJSONDocument);
        } catch (ElasticSearchIndexException e) {
            throw new SearchException(e);
        }
    }

    public final Collection<ElasticSearchJsonDocument> processDocuments(final Collection<Document> documents)
            throws ElasticSearchIndexException {
        LOGGER.info("Processing multiple document objects for elasticsearch indexing");

        Collection<ElasticSearchJsonDocument> esDocuments = new ArrayList<ElasticSearchJsonDocument>();
        // transform Document object into JSON object and send it to
        // elasticsearch server for indexing
        for (Document doc : documents) {
            esDocuments.add(documentJSONBuilder.convertToJSON(doc));
        }

        return esDocuments;
    }

    public final ElasticSearchJsonDocument processDocument(final Document document)
            throws ElasticSearchIndexException {
        Collection<Document> documents = new ArrayList<Document>();
        documents.add(document);
        LOGGER.info("Processing Document to update elasticsearch indexes");

        List<ElasticSearchJsonDocument> esDocuments = (List<ElasticSearchJsonDocument>) processDocuments(documents);
        return esDocuments.get(0);
    }




    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(ElasticsearchIndexWriterImpl.class);

}
