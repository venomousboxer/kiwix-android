package org.kiwix.kiwixmobile.readinglists.entities;

/**
 * Created by EladKeyshawn on 04/04/2017.
 */

public class BookmarkArticle {
  private String bookmarkUrl;
  private String bookmarkTitle;
  private String parentReadinglist;
  private String ZimId = "null";
  private String ZimName = "null";

  public BookmarkArticle(String bookmarkUrl, String bookmarkTitle, String parentReadinglist) {
    this.bookmarkUrl = bookmarkUrl;
    this.bookmarkTitle = bookmarkTitle;
    this.parentReadinglist = parentReadinglist;
  }

  public BookmarkArticle(String bookmarkUrl, String bookmarkTitle, String parentReadinglist, String zimId, String zimName) {
    this.bookmarkUrl = bookmarkUrl;
    this.bookmarkTitle = bookmarkTitle;
    this.parentReadinglist = parentReadinglist;
    ZimId = zimId;
    ZimName = zimName;
  }

  public BookmarkArticle() {

  }

  public BookmarkArticle(String pageUrl, String pageTitle) {
    this.bookmarkTitle = pageTitle;
    this.bookmarkUrl = pageUrl;
  }

  public void setBookmarkUrl(String bookmarkUrl) {
    this.bookmarkUrl = bookmarkUrl;
  }

  public void setBookmarkTitle(String bookmarkTitle) {
    this.bookmarkTitle = bookmarkTitle;
  }

  public void setParentReadinglist(String parentReadinglist) {
    this.parentReadinglist = parentReadinglist;
  }

  public void setZimId(String zimId) {
    ZimId = zimId;
  }

  public void setZimName(String zimName) {
    ZimName = zimName;
  }

  public String getArticleUrl() {
    return bookmarkUrl;
  }

  public String getArticleTitle() {
    return bookmarkTitle;
  }

  public String getParentReadinglist() {
    return parentReadinglist;
  }

  public String getZimId() {
    return ZimId;
  }

  public String getZimName() {
    return ZimName;
  }
}
