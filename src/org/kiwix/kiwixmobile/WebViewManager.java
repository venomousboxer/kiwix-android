package org.kiwix.kiwixmobile;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kiwix.kiwixmobile.views.web.KiwixWebView;

// TODO change to ActivityScope
@Singleton public class WebViewManager {
  private List<WeakReference<KiwixWebView>> webViews = new ArrayList<>();

  @Inject public WebViewManager() {
  }

  public void setWebViews(List<WeakReference<KiwixWebView>> webViews) {
    this.webViews.clear();
    this.webViews = webViews;
  }

  public void addTab(KiwixWebView webView) {
    webViews.add(new WeakReference<>(webView));
  }

  public void addTab(int index, KiwixWebView webView) {
    webViews.add(index, new WeakReference<>(webView));
  }

  public KiwixWebView getTab(int index) {
    if (size() >= index) {
      return webViews.get(index).get();
    }
    return null;
  }

  public void removeTab(int index) {
    if (size() >= index) {
      webViews.remove(index);
    }
  }

  public int size() {
    if (webViews != null) {
      return webViews.size();
    }
    return 0;
  }

  public void clear() {
    webViews.clear();
  }
}
