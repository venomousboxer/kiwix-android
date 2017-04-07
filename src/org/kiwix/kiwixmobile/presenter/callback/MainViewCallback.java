package org.kiwix.kiwixmobile.presenter.callback;

import android.content.DialogInterface;
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

  void selectTab(int position);

  KiwixWebView getWebView(String url);

  void setUpWebView();
}

