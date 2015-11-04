package fr.smile.liferay.web.elasticsearch.indexer;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import fr.smile.liferay.web.elasticsearch.exception.ElasticSearchIndexException;
import fr.smile.liferay.web.elasticsearch.indexer.document.ElasticSearchJsonDocument;
import fr.smile.liferay.web.elasticsearch.indexer.document.ElasticSearchJsonDocumentBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author marem
 * @since 30/10/15.
 */
public class ElasticSearchIndexerImpl implements ElasticSearchIndexer {

    /** The Constant LOGGER. */
    private final static Log LOGGER = LogFactoryUtil.getLog(ElasticSearchIndexerImpl.class);

    /** The document json builder. */
    @Autowired
    private ElasticSearchJsonDocumentBuilder documentJSONBuilder;


    /* (non-Javadoc)
     * @see com.rivetlogic.portal.search.elasticsearch.indexer.ElasticsearchIndexer#processDocuments(java.util.Collection)
     */
    public Collection<ElasticSearchJsonDocument> processDocuments(Collection<Document> documents) throws ElasticSearchIndexException {
        LOGGER.info("Processing multiple document objects for elasticsearch indexing");

        Collection<ElasticSearchJsonDocument> esDocuments = new ArrayList<ElasticSearchJsonDocument>();
        // transform Document object into JSON object and send it to
        // elasticsearch server for indexing
        for (Document doc : documents) {
            esDocuments.add(documentJSONBuilder.convertToJSON(doc));
        }

        return esDocuments;
    }

    /* (non-Javadoc)
     * @see com.rivetlogic.portal.search.elasticsearch.indexer.ElasticsearchIndexer#processDocument(com.liferay.portal.kernel.search.Document)
     */
    public ElasticSearchJsonDocument processDocument(Document document) throws ElasticSearchIndexException {
        Collection<Document> documents = new ArrayList<Document>();
        documents.add(document);
        LOGGER.info("Processing Document to update elasticsearch indexes");

        List<ElasticSearchJsonDocument> esDocuments = (List<ElasticSearchJsonDocument>) processDocuments(documents);
        return esDocuments.get(0);
    }
}
