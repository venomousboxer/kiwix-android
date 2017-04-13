package org.kiwix.kiwixmobile.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.*;
import com.soundcloud.lightcycle.ActivityLightCycleDispatcher;
import java.util.ArrayList;
import javax.inject.Inject;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.ZimContentProvider;
import org.kiwix.kiwixmobile.database.BookmarksDao;
import org.kiwix.kiwixmobile.database.KiwixDatabase;
import org.kiwix.kiwixmobile.presenter.callback.MainViewCallback;
import org.kiwix.kiwixmobile.settings.Constants;
import org.kiwix.kiwixmobile.views.web.KiwixWebView;
import rx.subscriptions.CompositeSubscription;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class MenuPresenter extends ActivityLightCycleDispatcher<MainViewCallback> {

  private Context context;
  private final TabsPresenter tabsPresenter;
  private SharedPreferences preferences;
  private MainViewCallback callback;
  private CompositeSubscription subscriptions;
  private ActionMode actionMode;
  private Menu menu;

  @Inject public MenuPresenter(Context context, TabsPresenter tabsPresenter,
      SharedPreferences preferences) {
    this.context = context;
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
    this.menu = menu;
    callback.styleMenuButtons(menu);
    if (Constants.IS_CUSTOM_APP) {
      menu.findItem(R.id.menu_help).setVisible(false);
    }

    menu.findItem(R.id.menu_bookmarks).setVisible(true);
    menu.findItem(R.id.menu_home).setVisible(true);
    menu.findItem(R.id.menu_randomarticle).setVisible(true);
    menu.findItem(R.id.menu_searchintext).setVisible(true);

    MenuItem searchItem = menu.findItem(R.id.menu_search);
    searchItem.setVisible(true);
    searchItem.setOnMenuItemClickListener(item -> {
      callback.openSearchActivity();
      return true;
    });

    return true;
  }

  public boolean onPrepareOptionsMenu(Menu menu) {
    toggleActionItemsConfig();
    refreshBookmarkSymbol();
    callback.refreshNavigationButtons();

    if (tabsPresenter.getCurrentWebView().getUrl() == null || tabsPresenter.getCurrentWebView()
        .getUrl()
        .equals("file:///android_res/raw/help.html")) {
      menu.findItem(R.id.menu_read_aloud).setVisible(false);
    } else {
      menu.findItem(R.id.menu_read_aloud).setVisible(true);
    }
    return true;
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

  public void toggleActionItemsConfig() {
    if (menu != null) {
      MenuItem random = menu.findItem(R.id.menu_randomarticle);
      MenuItem home = menu.findItem(R.id.menu_home);
      if (context.getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
        random.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        home.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      } else {
        random.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        home.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
      }
    }
  }

  public void refreshBookmarkSymbol() { // Checks if current webview is in bookmarks array
    if (menu == null) return;
    //if (bookmarks == null || bookmarks.size() == 0) {
    BookmarksDao bookmarksDao = new BookmarksDao(KiwixDatabase.getInstance(context));
    ArrayList<String> bookmarks =
        bookmarksDao.getBookmarks(ZimContentProvider.getId(), ZimContentProvider.getName());
    //}
    if (menu.findItem(R.id.menu_bookmarks) != null &&
        tabsPresenter.getCurrentWebView().getUrl() != null &&
        ZimContentProvider.getId() != null &&
        !tabsPresenter.getCurrentWebView().getUrl().equals("file:///android_res/raw/help.html")) {
      menu.findItem(R.id.menu_bookmarks)
          .setEnabled(true)
          .setIcon(
              bookmarks.contains(tabsPresenter.getCurrentWebView().getUrl())
                  ? R.drawable.action_bookmark_active
                  : R.drawable.action_bookmark)
          .getIcon().setAlpha(255);
    } else {
      menu.findItem(R.id.menu_bookmarks)
          .setEnabled(false)
          .setIcon(R.drawable.action_bookmark)
          .getIcon().setAlpha(130);
    }
  }
}
