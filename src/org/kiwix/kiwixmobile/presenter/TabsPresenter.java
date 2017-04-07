package org.kiwix.kiwixmobile.presenter;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.soundcloud.lightcycle.ActivityLightCycleDispatcher;
import javax.inject.Inject;
import org.kiwix.kiwixmobile.ZimContentProvider;
import org.kiwix.kiwixmobile.library.WebViewManager;
import org.kiwix.kiwixmobile.presenter.callback.MainViewCallback;
import org.kiwix.kiwixmobile.views.web.KiwixWebView;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class TabsPresenter extends ActivityLightCycleDispatcher<MainViewCallback> {

  private MainViewCallback callback;
  private CompositeSubscription subscriptions;
  private WebViewManager webViewManager;

  //private int currentWebViewIndex = 0;

  @Inject public TabsPresenter(WebViewManager webViewManager) {
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
    });
    subscriptions.add(click);
  }

  private void handleBackTabClick() {
    Subscription click = callback.backTabClickListener().subscribe(aVoid -> {
    });
    subscriptions.add(click);
  }

  private String mainPage() {
    String uri = ZimContentProvider.CONTENT_URI + ZimContentProvider.getMainPage();
    return Uri.parse(uri).toString();
  }

  private KiwixWebView newTab() {
    return newTab(mainPage());
  }

  private KiwixWebView newTab(String url) {
    KiwixWebView webView = callback.getWebView(url);
    webViewManager.addTab(webView);
    selectTab(webViewManager.size() - 1);
    //tabDrawerAdapter.notifyDataSetChanged();
    callback.setUpWebView();
    //documentParser.initInterface(webView);
    return webView;
  }

  private void selectTab(int index){
    //currentWebViewIndex = index;
    callback.selectTab(index);
  }
}
