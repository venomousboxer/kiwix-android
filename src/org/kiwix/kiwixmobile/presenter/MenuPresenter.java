package org.kiwix.kiwixmobile.presenter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.*;
import com.soundcloud.lightcycle.ActivityLightCycleDispatcher;
import javax.inject.Inject;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.presenter.callback.MainViewCallback;
import org.kiwix.kiwixmobile.views.web.KiwixWebView;
import rx.subscriptions.CompositeSubscription;

public class MenuPresenter extends ActivityLightCycleDispatcher<MainViewCallback> {

  private final TabsPresenter tabsPresenter;
  private SharedPreferences preferences;
  private MainViewCallback callback;
  private CompositeSubscription subscriptions;
  private ActionMode actionMode;

  @Inject public MenuPresenter(TabsPresenter tabsPresenter, SharedPreferences preferences) {
    this.tabsPresenter = tabsPresenter;
    this.preferences = preferences;
    this.subscriptions = new CompositeSubscription();
  }

  @Override public void onCreate(MainViewCallback host, @Nullable Bundle bundle) {
    super.onCreate(host, bundle);
    this.callback = host;
  }

  @Override public boolean onOptionsItemSelected(MainViewCallback host, MenuItem item) {
    KiwixWebView webView = tabsPresenter.getCurrentWebView();
    switch (item.getItemId()) {

      case R.id.menu_home:
      case android.R.id.home:
        host.openMainPage();
        break;

      case R.id.menu_searchintext:
        host.showSearchInText(webView);
        break;

      case R.id.menu_bookmarks:
        host.toggleBookmark();
        break;

      case R.id.menu_bookmarks_list:
        host.goToBookmarks();
        break;

      case R.id.menu_randomarticle:
        host.openRandomArticle();
        break;

      case R.id.menu_help:
        host.showHelpPage();
        break;

      case R.id.menu_openfile:
        host.manageZimFiles(0);
        break;

      case R.id.menu_settings:
        host.selectSettings();
        break;

      case R.id.menu_read_aloud:
        host.readAloudMenuClick();
        break;

      default:
        break;
    }
    return super.onOptionsItemSelected(host, item);
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    return false;
  }

  public boolean onPrepareOptionsMenu(Menu menu) {
    return false;
  }

  public void onActionModeStarted(ActionMode mode) {
    if (actionMode == null) {
      actionMode = mode;
      Menu menu = mode.getMenu();
      // Inflate custom menu icon.
      callback.inflateReadAloudMenu(menu);
      readAloudSelection(menu);
    }
  }

  private void readAloudSelection(Menu menu) {
    if (menu != null) {
      menu.findItem(R.id.menu_speak_text).setOnMenuItemClickListener(item -> {
        callback.readSelection(tabsPresenter.getCurrentWebView());
        if (actionMode != null) {
          actionMode.finish();
        }
        return true;
      });
    }
  }

  public void onActionModeFinished(ActionMode mode) {
    actionMode = null;
  }
}
