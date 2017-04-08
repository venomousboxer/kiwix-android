package org.kiwix.kiwixmobile.presenter;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.soundcloud.lightcycle.ActivityLightCycleDispatcher;
import javax.inject.Inject;
import org.kiwix.kiwixmobile.ZimContentProvider;
import org.kiwix.kiwixmobile.WebViewManager;
import org.kiwix.kiwixmobile.presenter.callback.MainViewCallback;
import org.kiwix.kiwixmobile.views.web.KiwixWebView;
import org.kiwix.kiwixmobile.views.web.ScrollingKiwixWebView;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static org.kiwix.kiwixmobile.constants.PreferenceTag.PREF_HIDE_TOOLBAR;

public class TabsPresenter extends ActivityLightCycleDispatcher<MainViewCallback> {

  private MainViewCallback callback;
  private CompositeSubscription subscriptions;
  private SharedPreferences preferences;
  private WebViewManager webViewManager;

  private int currentWebViewIndex = 0;
  private KiwixWebView tempForUndo;

  @Inject public TabsPresenter(SharedPreferences preferences, WebViewManager webViewManager) {
    this.preferences = preferences;
    this.webViewManager = webViewManager;
    this.subscriptions = new CompositeSubscription();
  }

  @Override public void onCreate(MainViewCallback host, @Nullable Bundle bundle) {
    super.onCreate(host, bundle);
    this.callback = host;
    handleNewTabClick();
    handleForwardTabClick();
    handleBackTabClick();
  }

  @Override public void onDestroy(MainViewCallback host) {
    super.onDestroy(host);
    subscriptions.clear();
  }

  private void handleNewTabClick() {
    Subscription click = callback.newTabClickListener().subscribe(
        aVoid -> newTab()
    );
    subscriptions.add(click);
  }

  private void handleForwardTabClick() {
    Subscription click = callback.forwardTabClickListener().subscribe(aVoid -> {
      if (getCurrentWebView().canGoForward()) {
        getCurrentWebView().goForward();
      }
    });
    subscriptions.add(click);
  }

  private void handleBackTabClick() {
    Subscription click = callback.backTabClickListener().subscribe(aVoid -> {
      if (getCurrentWebView().canGoBack()) {
        getCurrentWebView().goBack();
      }
    });
    subscriptions.add(click);
  }

  private String mainPage() {
    String uri = ZimContentProvider.CONTENT_URI + ZimContentProvider.getMainPage();
    return Uri.parse(uri).toString();
  }

  public KiwixWebView newTab() {
    return newTab(mainPage());
  }

  public void selectTab(int position) {
    currentWebViewIndex = position;
    callback.setTabDrawerAdapterSelection(position);
    callback.removeWebViewFrame();
    KiwixWebView webView = callback.addWebViewToFrame(position);
    callback.setTabDrawerAdapterSelection(currentWebViewIndex);
    callback.closeLeftDrawer();
    callback.loadPrefs();
    callback.refreshBookmarkSymbol();
    callback.updateTableOfContents();

    boolean isToolbarHidden = preferences.getBoolean(PREF_HIDE_TOOLBAR, false);
    if (isToolbarHidden) {
      ((ScrollingKiwixWebView) webView).ensureToolbarDisplayed();
    }
  }

  public KiwixWebView newTab(String url) {
    KiwixWebView webView = callback.getWebView(url);
    webViewManager.addTab(webView);
    selectTab(webViewManager.size() - 1);
    callback.refreshTabDrawerAdapter();
    callback.setUpWebView();
    callback.initParser(webView);
    return webView;
  }

  public void newTabInBackground(String url) {
    KiwixWebView webView = callback.getWebView(url);
    webViewManager.addTab(webView);
    callback.refreshTabDrawerAdapter();
    callback.setUpWebView();
    callback.initParser(webView);
  }

  public void restoreTab(int index) {
    webViewManager.addTab(index, tempForUndo);
    callback.refreshTabDrawerAdapter();
    selectTab(index);
    callback.setUpWebView();
  }

  public void closeTab(int index) {
    tempForUndo = webViewManager.getTab(index);
    int selectedPosition = callback.getSelectedTabDrawerPosition();
    int newSelectedPosition = selectedPosition;

    if (index <= selectedPosition) newSelectedPosition = selectedPosition - 1;
    if (index == 0) newSelectedPosition = 0;
    if (webViewManager.size() == 1) newTab();

    webViewManager.removeTab(index);
    callback.showRestoreTabSnackbar(index);
    selectTab(newSelectedPosition);
    callback.refreshTabDrawerAdapter();
  }

  public int getCurrentWebViewIndex() {
    return currentWebViewIndex;
  }

  public KiwixWebView getCurrentWebView() {
    if (webViewManager.size() == 0) return newTab();
    if (currentWebViewIndex < webViewManager.size()) {
      return webViewManager.getTab(currentWebViewIndex);
    } else {
      return webViewManager.getTab(0);
    }
  }
}
