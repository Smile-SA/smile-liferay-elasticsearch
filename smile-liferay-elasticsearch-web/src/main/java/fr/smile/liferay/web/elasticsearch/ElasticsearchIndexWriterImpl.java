package fr.smile.liferay.web.elasticsearch;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexWriter;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentComparator;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import fr.smile.liferay.elasticsearch.client.model.ElasticSearchJsonDocument;
import fr.smile.liferay.elasticsearch.client.model.Index;
import fr.smile.liferay.elasticsearch.client.service.IndexService;
import fr.smile.liferay.web.elasticsearch.exception.ElasticSearchIndexException;
import fr.smile.liferay.web.elasticsearch.model.document.ElasticSearchJsonDocumentBuilder;
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
public class ElasticsearchIndexWriterImpl extends BaseIndexWriter {

    /** The document json builder. */
    @Autowired
    private ElasticSearchJsonDocumentBuilder documentJSONBuilder;

    /** Liferay index. */
    @Autowired
    private Index index;

    /** Liferay index service. */
    @Autowired
    private IndexService indexService;

    /** version. */
    public static final String VERSION = "version";

    /**
     * War type.
     */
    public static final String WAR = "/war";

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

        if (!uid.endsWith(WAR)) {
            indexService.removeDocument(uid, index.getName());
        }

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
        LOGGER.error("Delete portlet documents from elasticsearch indexing");
        //throw new SearchException("Portlet deployment documents are not supported");
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
            indexService.writeDocument(index, elasticserachJSONDocument);
        } catch (ElasticSearchIndexException e) {
            throw new SearchException(e);
        }
    }

    /**
     * Process collection of documents.
     * @param documents documents to process
     * @return json document
     * @throws ElasticSearchIndexException elasticsearch exception
     */
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

    /**
     * Process single document.
     * @param document document to process
     * @return json document
     * @throws ElasticSearchIndexException elasticsearch exception
     */
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
