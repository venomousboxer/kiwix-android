package org.kiwix.kiwixmobile.database;

import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Query;

import org.kiwix.kiwixmobile.database.entity.Bookmarks;
import org.kiwix.kiwixmobile.database.entity.ReadingListFolders;
import org.kiwix.kiwixmobile.readinglists.entities.BookmarkArticle;
import org.kiwix.kiwixmobile.readinglists.entities.ReadinglistFolder;
import org.kiwix.kiwixmobile.readinglists.lists.ReadingListArticleItem;
import org.kiwix.kiwixmobile.readinglists.lists.ReadingListItem;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by EladKeyshawn on 04/04/2017.
 */

public class ReadingListFolderDao {
    private KiwixDatabase mDb;


    public ReadingListFolderDao(KiwixDatabase kiwikDatabase) {
        this.mDb = kiwikDatabase;
    }





    public ArrayList<BookmarkArticle> getArticlesOfFolder(ReadinglistFolder folder) {
        SquidCursor<Bookmarks> articlesInFolderCursor = mDb.query(
                Bookmarks.class,
                Query.selectDistinct(Bookmarks.BOOKMARK_TITLE).selectMore(Bookmarks.BOOKMARK_URL, Bookmarks.PARENT_READINGLIST)
                    .where(Bookmarks.PARENT_READINGLIST.eq(folder.getFolderTitle()))
                        .orderBy(Bookmarks.BOOKMARK_TITLE.asc()));
        ArrayList<BookmarkArticle> result = new ArrayList<>();
        try {
            while (articlesInFolderCursor.moveToNext()) {
                BookmarkArticle bookmark = new BookmarkArticle();
                bookmark.setBookmarkUrl(articlesInFolderCursor.get(Bookmarks.BOOKMARK_URL));
                bookmark.setBookmarkTitle(articlesInFolderCursor.get(Bookmarks.BOOKMARK_TITLE));
                bookmark.setParentReadinglist(articlesInFolderCursor.get(Bookmarks.PARENT_READINGLIST));
                result.add(bookmark);
            }

        } finally {
            articlesInFolderCursor.close();
        }
        return result;
    }

    public ArrayList<ReadinglistFolder> getFolders() {
        SquidCursor<ReadingListFolders> foldersCursor = mDb.query(
                ReadingListFolders.class,
                Query.selectDistinct(ReadingListFolders.FOLDER_TITLE)
                        .orderBy(ReadingListFolders.FOLDER_TITLE.asc()));
        ArrayList<ReadinglistFolder> result = new ArrayList<>();
        try {
            while (foldersCursor.moveToNext()) {
                result.add(new ReadinglistFolder(foldersCursor.get(ReadingListFolders.FOLDER_TITLE)));
            }
        } finally {
            foldersCursor.close();
        }
        return result;
    }


    public void saveFolder(ReadinglistFolder folder) {
        if (folder != null) {
            mDb.persist(new ReadingListFolders().setFolderTitle(folder.getFolderTitle()).setArticleCount(folder.getArticlesCount()));
        }

    }


    public void saveBookmark(BookmarkArticle article) {
        if (article != null) {
            mDb.persist(new Bookmarks().setBookmarkTitle(article.getBookmarkTitle())
                    .setBookmarkUrl(article.getBookmarkUrl())
                    .setParentReadinglist(article.getParentReadinglist())
                    .setZimId(article.getZimId())
                    .setZimName(article.getZimName()));

        }
    }

    private void deleteFolder(ReadingListItem folder) {
        mDb.deleteWhere(Bookmarks.class, Bookmarks.PARENT_READINGLIST.eq(folder.getTitle()));
        mDb.deleteWhere(ReadingListFolders.class, ReadingListFolders.FOLDER_TITLE.eq(folder.getTitle()));
    }


    public void deleteFolders(Set<ReadingListItem> folders) {
        for (ReadingListItem folder : folders) {
            deleteFolder(folder);
        }
    }



    public void deleteAll(){
        mDb.clear();
    }

    public void deleteArticles(Set<ReadingListArticleItem> selectedItems) {
        for (ReadingListArticleItem item : selectedItems) {
            mDb.deleteWhere(Bookmarks.class, Bookmarks.BOOKMARK_URL.eq(item.getArticle_url())
            .and(Bookmarks.BOOKMARK_TITLE.eq(item.getTitle())));
        }
    }


}


