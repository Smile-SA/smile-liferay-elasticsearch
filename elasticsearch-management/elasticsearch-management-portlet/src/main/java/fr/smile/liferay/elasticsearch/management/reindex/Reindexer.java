package fr.smile.liferay.elasticsearch.management.reindex;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ContactLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.asset.model.AssetCategory;
import com.liferay.portlet.asset.model.AssetVocabulary;
import com.liferay.portlet.asset.service.AssetCategoryLocalServiceUtil;
import com.liferay.portlet.asset.service.AssetVocabularyLocalServiceUtil;
import com.liferay.portlet.documentlibrary.model.DLFileEntry;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.DLFileEntryLocalServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalServiceUtil;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.model.JournalFolder;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portlet.journal.service.JournalFolderLocalServiceUtil;
import com.liferay.portlet.messageboards.model.MBThread;
import com.liferay.portlet.messageboards.service.MBThreadLocalServiceUtil;

import java.util.List;

/**
 * Class to reindex documents in Liferay Index.
 */
public final class Reindexer {

    /** The Constant LOGGER. */
    private static final Log LOGGER = LogFactoryUtil.getLog(Reindexer.class);

    /**
     * Singleton instance.
     */
    private static Reindexer instance;

    /**
     * Indexing runnable.
     */
    private IndexingRunnable runnable;

    /**
     * Default constructor.
     */
    private Reindexer() {
        runnable = new IndexingRunnable();
    }

    /**
     * Get singleton instance.
     * @return the singleton instance
     */
    public static Reindexer getInstance() {
        if (instance == null) {
            instance = new Reindexer();
        }
        return instance;
    }

    /**
     * Get current indexing status.
     * @return the indexing status
     */
    public final IndexingStatus getCurrentStatus() {
        return runnable.getStatus();
    }

    /**
     * Get processed documents.
     * @return the processed documents
     */
    public final int getProcessedDocuments() {
        return runnable.getProcessedDocuments();
    }

    /**
     * Get total number of documents.
     * @return the total number of documents
     */
    public final int getTotalDocuments() {
        return runnable.getTotalDocuments();
    }

    /**
     * Reindex all documents.
     */
    public final void reindexAll() {
        if (getCurrentStatus() == IndexingStatus.AVAILABLE) {
            new Thread(runnable).start();
        }
    }

    private class IndexingRunnable implements Runnable {

        private IndexingStatus status = IndexingStatus.AVAILABLE;

        private int processedDocuments;

        private int totalDocuments;

        IndexingRunnable() {

        }

        public IndexingStatus getStatus() {
            return status;
        }

        public int getProcessedDocuments() {
            return processedDocuments;
        }

        public int getTotalDocuments() {
            return totalDocuments;
        }

        private <T> void reindex(Class<T> className, List<T> documents) {
            Indexer indexer = IndexerRegistryUtil.getIndexer(className);

            for (T document : documents) {
                try {
                    indexer.reindex(document);
                    processedDocuments++;
                } catch (SearchException e) {
                    LOGGER.error("An error occured while reindexing document " + document, e);
                }
            }
        }

        @Override
        public void run() {
            status = IndexingStatus.PENDING;
            processedDocuments = 0;
            totalDocuments = 0;

            try {
                List<AssetCategory> assetCategories = AssetCategoryLocalServiceUtil.getCategories();

                List<AssetVocabulary> assetVocabularies = AssetVocabularyLocalServiceUtil.getAssetVocabularies(
                        0, AssetVocabularyLocalServiceUtil.getAssetVocabulariesCount()
                );

                List<DLFileEntry> dlFileEntries = DLFileEntryLocalServiceUtil.getDLFileEntries(
                        0, DLFileEntryLocalServiceUtil.getDLFileEntriesCount()
                );

                List<DLFolder> dlFolders = DLFolderLocalServiceUtil.getDLFolders(
                        0, DLFolderLocalServiceUtil.getDLFoldersCount()
                );

                List<JournalArticle> journalArticles = JournalArticleLocalServiceUtil.getJournalArticles(
                        0, JournalArticleLocalServiceUtil.getJournalArticlesCount()
                );

                List<JournalFolder> journalFolders = JournalFolderLocalServiceUtil.getJournalFolders(
                        0, JournalFolderLocalServiceUtil.getJournalFoldersCount()
                );

                List<MBThread> mbThreads = MBThreadLocalServiceUtil.getMBThreads(
                        0, MBThreadLocalServiceUtil.getMBThreadsCount()
                );

                List<User> users = UserLocalServiceUtil.getUsers(
                        0, UserLocalServiceUtil.getUsersCount()
                );

                List<Contact> contacts = ContactLocalServiceUtil.getContacts(
                        0, ContactLocalServiceUtil.getContactsCount()
                );


                totalDocuments = assetCategories.size()
                        + assetVocabularies.size()
                        + dlFileEntries.size()
                        + dlFolders.size()
                        + journalArticles.size()
                        + journalFolders.size()
                        + mbThreads.size()
                        + users.size()
                        + contacts.size();

                reindex(AssetCategory.class, assetCategories);
                reindex(AssetVocabulary.class, assetVocabularies);
                reindex(DLFileEntry.class, dlFileEntries);
                reindex(DLFolder.class, dlFolders);
                reindex(JournalArticle.class, journalArticles);
                reindex(JournalFolder.class, journalFolders);
                reindex(MBThread.class, mbThreads);
                reindex(User.class, users);
                reindex(Contact.class, contacts);
            } catch (SystemException e) {
                LOGGER.error("An error occured while reindexing documents", e);
            }

            status = IndexingStatus.AVAILABLE;
        }
    }

}
