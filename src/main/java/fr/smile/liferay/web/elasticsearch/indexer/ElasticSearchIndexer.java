package fr.smile.liferay.web.elasticsearch.indexer;

import com.liferay.portal.kernel.search.Document;
import fr.smile.liferay.web.elasticsearch.exception.ElasticSearchIndexException;
import fr.smile.liferay.web.elasticsearch.indexer.document.ElasticSearchJsonDocument;

import java.util.Collection;

/**
 * @author marem
 * @since 30/10/15.
 */
public interface ElasticSearchIndexer {

    /**
     * Process document.
     *
     * @param document the document
     * @return the elasticserach json document
     * @throws ElasticSearchIndexException the elasticsearch indexing exception
     */
    ElasticSearchJsonDocument processDocument(Document document) throws ElasticSearchIndexException;

    /**
     * Process documents.
     *
     * @param documents the documents
     * @return the collection
     * @throws ElasticSearchIndexException the elasticsearch indexing exception
     */
    Collection<ElasticSearchJsonDocument> processDocuments(Collection<Document> documents)
            throws ElasticSearchIndexException;
}
