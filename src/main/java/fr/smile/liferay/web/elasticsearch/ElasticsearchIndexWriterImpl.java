package fr.smile.liferay.web.elasticsearch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.liferay.portal.kernel.search.*;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import fr.smile.liferay.web.elasticsearch.exception.ElasticSearchIndexException;
import fr.smile.liferay.web.elasticsearch.indexer.ElasticSearchIndexer;
import fr.smile.liferay.web.elasticsearch.indexer.document.ElasticSearchJsonDocument;
import fr.smile.liferay.web.elasticsearch.util.ElasticSearchConnector;
import fr.smile.liferay.web.elasticsearch.util.ElasticSearchIndexerConstants;
import org.springframework.stereotype.Service;

/**
 * @author marem
 * @since 29/10/15.
 */
@Service
public class ElasticsearchIndexWriterImpl implements IndexWriter {

    /** The indexer. */
    @Autowired
    private ElasticSearchIndexer indexer;

    /** The _es connector. */
    @Autowired
    private ElasticSearchConnector esConnector;

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
        documentComparator.addOrderBy(ElasticSearchIndexerConstants.VERSION);
        Collections.sort((List<Document>) documents, documentComparator);

        for (Document document : documents) {
            updateDocument(searchContext, document);
        }
    }

    @Override
    public final void deleteDocument(final SearchContext searchContext, final String uid) throws SearchException {
        LOGGER.debug("Delete document from elasticsearch indexices");
        deleteIndexByQuery(uid);
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
        documentComparator.addOrderBy(ElasticSearchIndexerConstants.VERSION);
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
            ElasticSearchJsonDocument elasticserachJSONDocument = indexer.processDocument(document);
            writeIndex(elasticserachJSONDocument);
        } catch (ElasticSearchIndexException e) {
            throw new SearchException(e);
        }
    }

    /**
     * A method to persist Liferay index to Elasticsearch server document.
     *
     * @param esDocument
     *            the json document
     */
    private void writeIndex(final ElasticSearchJsonDocument esDocument) {

        try {
            if (esDocument.isError()) {
                LOGGER.warn("Coudln't store document in index. Error..." + esDocument.getErrorMessage());
            } else {
                Client client = esConnector.getClient();
                IndexResponse response = client.prepareIndex(
                        ElasticSearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX,
                		esDocument.getIndexType(),
                        esDocument.getId()
                ).setSource(esDocument.getJsonDocument()).execute().actionGet();

                LOGGER.debug("Document indexed successfully with Id: " + esDocument.getId()
                        + " ,Type:" + esDocument.getIndexType()
                        + " ,Updated index version:" + response.getVersion());
            }
        } catch (NoNodeAvailableException noNodeEx) {
            LOGGER.error("No node available:" + noNodeEx.getDetailedMessage());
        }
    }


    /**
     * Delete index by query.
     *
     * @param uid
     *            the uid
     */
    private void deleteIndexByQuery(final String uid) {

        try {
            /** Don't handle plugin deployment documents, skip them */
            if (!uid.endsWith(ElasticSearchIndexerConstants.WAR)) {
                Client client = esConnector.getClient();
                QueryStringQueryBuilder query = QueryBuilders.queryStringQuery(
                        ElasticSearchIndexerConstants.ELASTIC_SEARCH_QUERY_UID + uid
                );

                SearchResponse scrollResp = client
                        .prepareSearch(ElasticSearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX)
                        .setQuery(query)
                        .execute().actionGet();

                LOGGER.debug("Document deleted successfully with Id:" + uid + " , Status:" + scrollResp.status());
            }
        } catch (NoNodeAvailableException noNodeEx) {
            LOGGER.error("No node available:" + noNodeEx.getDetailedMessage());
        }
    }

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(ElasticsearchIndexWriterImpl.class);

}
