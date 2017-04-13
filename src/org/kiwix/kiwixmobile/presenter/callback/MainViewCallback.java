package org.kiwix.kiwixmobile.presenter.callback;

import android.content.DialogInterface;
import android.view.Menu;
import org.kiwix.kiwixmobile.ViewCallback;
import org.kiwix.kiwixmobile.views.web.KiwixWebView;
import rx.Observable;

public interface MainViewCallback extends ViewCallback {

  void showRateDialog(
      String title, String body, String positive, String negative, String neutral,
      DialogInterface.OnClickListener positiveListener,
      DialogInterface.OnClickListener negativeListener,
      DialogInterface.OnClickListener neutralListener);

  void openPlayStorePage();

  Observable<Void> newTabClickListener();

  Observable<Void> forwardTabClickListener();

  Observable<Void> backTabClickListener();

  KiwixWebView getWebView(String url);

  void setUpWebView();

  void setTabDrawerAdapterSelection(int position);

  void removeWebViewFrame();

  KiwixWebView addWebViewToFrame(int position);

  void closeLeftDrawer();

  void loadPrefs();

  void updateTableOfContents();

  void refreshTabDrawerAdapter();

  void initParser(KiwixWebView webView);

  int getSelectedTabDrawerPosition();

  void showRestoreTabSnackbar(int index);

  boolean openMainPage();

  void showSearchInText(KiwixWebView webView);

  void toggleBookmark();

  void goToBookmarks();

  boolean openRandomArticle();

  void showHelpPage();

  void manageZimFiles(int i);

  void selectSettings();

  void readAloudMenuClick();

  void inflateReadAloudMenu(Menu menu);

  void readSelection(KiwixWebView currentWebView);

  void styleMenuButtons(Menu menu);

  void openSearchActivity();

  void refreshNavigationButtons();
}

