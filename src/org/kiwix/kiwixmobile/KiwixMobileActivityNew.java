package org.kiwix.kiwixmobile;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.*;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.jakewharton.rxbinding.view.RxView;
import com.soundcloud.lightcycle.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Named;
import org.json.JSONArray;
import org.kiwix.kiwixmobile.database.BookmarksDao;
import org.kiwix.kiwixmobile.database.KiwixDatabase;
import org.kiwix.kiwixmobile.di.components.ApplicationComponent;
import org.kiwix.kiwixmobile.presenter.*;
import org.kiwix.kiwixmobile.presenter.callback.MainViewCallback;
import org.kiwix.kiwixmobile.settings.Constants;
import org.kiwix.kiwixmobile.settings.KiwixSettingsActivity;
import org.kiwix.kiwixmobile.utils.*;
import org.kiwix.kiwixmobile.utils.files.FileUtils;
import org.kiwix.kiwixmobile.views.AnimatedProgressBar;
import org.kiwix.kiwixmobile.views.CompatFindActionModeCallback;
import org.kiwix.kiwixmobile.views.web.*;
import rx.Observable;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static org.kiwix.kiwixmobile.constants.ActivityRequest.*;
import static org.kiwix.kiwixmobile.constants.IntentExtra.*;
import static org.kiwix.kiwixmobile.constants.NamedDependency.NAMED_PARSER_JS;
import static org.kiwix.kiwixmobile.constants.PreferenceTag.*;
import static org.kiwix.kiwixmobile.utils.StyleUtils.dialogStyle;

public class KiwixMobileActivityNew
    extends BaseActivity<KiwixMobileActivityNew>
    implements LightCycleDispatcher<ActivityLightCycle<KiwixMobileActivityNew>>,
    WebViewCallback, MainViewCallback {

  @Inject @LightCycle MainViewPresenter mainViewPresenter;

  @Inject @LightCycle RateDialogPresenter rateDialogPresenter;

  @Inject @LightCycle TabsPresenter tabsPresenter;

  @Inject @LightCycle MenuPresenter menuPresenter;

  @Inject @Named(NAMED_PARSER_JS) String parserJS;

  @Inject SharedPreferences preferences;

  @Inject LanguageUtils languageUtils;

  @Inject WebViewManager webViewManager;

  public static final String TAG_KIWIX = "kiwix";

  public static final String contactEmailAddress = "android@kiwix.org";

  private boolean isBackToTopEnabled = false;

  private boolean isToolbarHidden = false;

  private boolean isHideToolbar = false;

  private boolean isSpeaking = false;

  protected boolean requestClearHistoryAfterLoad = false;

  private boolean isOpenNewTabInBackground;

  public static boolean nightMode;

  public static boolean refresh;

  private static Uri KIWIX_LOCAL_MARKET_URI;

  private static Uri KIWIX_BROWSER_MARKET_URI;

  private DocumentParser documentParser;

  public List<TableDrawerAdapter.DocumentSection> documentSections;

  public Menu menu;

  private ArrayList<String> bookmarks;

  private CompatFindActionModeCallback compatCallback;

  private TabDrawerAdapter tabDrawerAdapter;

  private File file;

  private ActionMode actionMode = null;

  private boolean isFirstRun;

  private BookmarksDao bookmarksDao;

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindView(R.id.button_backtotop) Button backToTopButton;

  @BindView(R.id.toolbar_layout) RelativeLayout toolbarContainer;

  @BindView(R.id.progress_view) AnimatedProgressBar progressBar;

  @BindView(R.id.snackbar_layout) CoordinatorLayout snackbarLayout;

  @BindView(R.id.new_tab_button) RelativeLayout tabNewButton;

  @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;

  @BindView(R.id.left_drawer) LinearLayout tabDrawerLeftContainer;

  @BindView(R.id.right_drawer) LinearLayout tableDrawerRightContainer;

  @BindView(R.id.left_drawer_list) RecyclerView tabDrawerLeft;

  @BindView(R.id.right_drawer_list) RecyclerView tableDrawerRight;

  @BindView(R.id.content_frame) FrameLayout contentFrame;

  @BindView(R.id.action_back_button) ImageView tabBackButton;

  @BindView(R.id.action_forward_button) ImageView tabForwardButton;

  @BindView(R.id.action_back) View tabBackButtonContainer;

  @BindView(R.id.action_forward) View tabForwardButtonContainer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    nightMode = preferences.getBoolean(PREF_NIGHT_MODE, false);
    if (nightMode) setTheme(R.style.AppTheme_Night);

    handleLocaleCheck();

    setUpToolbar();
    isFirstRun = preferences.getBoolean(PREF_IS_FIRST_RUN, true);

    initPlayStoreUri();

    isHideToolbar = preferences.getBoolean(PREF_HIDE_TOOLBAR, false);

    documentSections = new ArrayList<>();
    tabDrawerAdapter = new TabDrawerAdapter(webViewManager);
    tabDrawerLeft.setLayoutManager(new LinearLayoutManager(this));
    tabDrawerLeft.setAdapter(tabDrawerAdapter);
    tableDrawerRight.setLayoutManager(new LinearLayoutManager(this));

    TableDrawerAdapter tableDrawerAdapter = new TableDrawerAdapter();
    tableDrawerRight.setAdapter(tableDrawerAdapter);
    tableDrawerAdapter.setTableClickListener(new TableDrawerAdapter.TableClickListener() {
      @Override public void onHeaderClick(View view) {
        tabsPresenter.getCurrentWebView().setScrollY(0);
        drawerLayout.closeDrawer(GravityCompat.END);
      }

      @Override public void onSectionClick(View view, int position) {
        tabsPresenter.getCurrentWebView().loadUrl("javascript:document.getElementById('"
            + documentSections.get(position).id
            + "').scrollIntoView();");

        drawerLayout.closeDrawers();
      }
    });

    tableDrawerAdapter.notifyDataSetChanged();

    tabDrawerAdapter.setTabClickListener(new TabDrawerAdapter.TabClickListener() {
      @Override public void onSelectTab(View view, int position) {
        tabsPresenter.selectTab(position);
      }

      @Override public void onCloseTab(View view, int position) {
        tabsPresenter.closeTab(position);
      }
    });

    ActionBarDrawerToggle drawerToggle =
        new KiwixActionBarDrawerToggle(this, drawerLayout, toolbar);

    drawerLayout.addDrawerListener(drawerToggle);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    drawerToggle.syncState();

    compatCallback = new CompatFindActionModeCallback(this);
    documentParser = new DocumentParser(new DocumentParser.SectionsListener() {
      @Override
      public void sectionsLoaded(String title, List<TableDrawerAdapter.DocumentSection> sections) {
        documentSections.addAll(sections);
        tableDrawerAdapter.setTitle(title);
        tableDrawerAdapter.setSections(documentSections);
        tableDrawerAdapter.notifyDataSetChanged();
      }

      @Override public void clearSections() {
        documentSections.clear();
        tableDrawerAdapter.notifyDataSetChanged();
      }
    });

    manageExternalLaunchAndRestoringViewState();
    loadPrefs();
    updateTitle(ZimContentProvider.getZimFileTitle());

    Intent i = getIntent();
    if (i.getBooleanExtra(EXTRA_LIBRARY, false)) {
      manageZimFiles(2);
    }
    if (i.hasExtra(EXTRA_FILE_SEARCHED)) {
      searchForTitle(i.getStringExtra(EXTRA_FILE_SEARCHED));
      tabsPresenter.selectTab(webViewManager.size() - 1);
    }
    if (i.hasExtra(EXTRA_CHOSE_URL)) {
      tabsPresenter.newTab();
      tabsPresenter.getCurrentWebView().loadUrl(i.getStringExtra(EXTRA_CHOSE_URL));
    }
    if (i.hasExtra(EXTRA_CHOSE_TITLE)) {
      tabsPresenter.newTab();
      tabsPresenter.getCurrentWebView().loadUrl(i.getStringExtra(EXTRA_CHOSE_TITLE));
    }
    if (i.hasExtra(EXTRA_ZIM_FILE)) {
      File file = new File(FileUtils.getFileName(i.getStringExtra(EXTRA_ZIM_FILE)));
      LibraryFragment.mService.cancelNotification(i.getIntExtra(EXTRA_NOTIFICATION_ID, 0));
      Uri uri = Uri.fromFile(file);

      finish();
      Intent zimFile = new Intent(KiwixMobileActivityNew.this, KiwixMobileActivity.class);
      zimFile.setData(uri);
      startActivity(zimFile);
    }

    isToolbarHidden = isHideToolbar;
  }

  @Override public Observable<Void> newTabClickListener() {
    return RxView.clicks(tabNewButton);
  }

  @Override public Observable<Void> forwardTabClickListener() {
    return RxView.clicks(tabForwardButtonContainer);
  }

  @Override public Observable<Void> backTabClickListener() {
    return RxView.clicks(tabForwardButtonContainer);
  }

  @Override
  public void onActionModeStarted(ActionMode mode) {
    menuPresenter.onActionModeStarted(mode);
    super.onActionModeStarted(mode);
  }

  @Override
  public void onActionModeFinished(ActionMode mode) {
    menuPresenter.onActionModeFinished(mode);
    super.onActionModeFinished(mode);
  }

  private void initPlayStoreUri() {
    KIWIX_LOCAL_MARKET_URI = Uri.parse("market://details?id=" + getPackageName());
    KIWIX_BROWSER_MARKET_URI =
        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName());
  }

  private void setUpToolbar() {
    setSupportActionBar(toolbar);
    toolbar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        openSearchActivity();
      }
    });
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
          drawerLayout.closeDrawer(GravityCompat.END);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
          drawerLayout.closeDrawer(GravityCompat.START);
        } else {
          drawerLayout.openDrawer(GravityCompat.START);
        }
      }
    });
  }

  private void goToSearch(boolean isVoice) {
    final String zimFile = ZimContentProvider.getZimFile();
    saveTabStates();
    Intent i = new Intent(KiwixMobileActivityNew.this, SearchActivity.class);
    i.putExtra(EXTRA_ZIM_FILE, zimFile);
    if (isVoice) {
      i.putExtra(EXTRA_IS_WIDGET_VOICE, true);
    }
    startActivityForResult(i, REQUEST_FILE_SEARCH);
  }

  private void updateTitle(String zimFileTitle) {
    if (zimFileTitle == null || zimFileTitle.trim().isEmpty()) {
      getSupportActionBar().setTitle(createMenuText(getString(R.string.app_name)));
    } else {
      getSupportActionBar().setTitle(createMenuText(zimFileTitle));
    }
  }

  // Reset the Locale and change the font of all TextViews and its subclasses, if necessary
  private void handleLocaleCheck() {
    LanguageUtils.handleLocaleChange(this);
    languageUtils.changeFont(getLayoutInflater());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // TODO create a base Activity class that class this.
    FileUtils.deleteCachedFiles(this);
  }

  @Override protected void setContentView() {
    setContentView(R.layout.main);
    ButterKnife.bind(this);
  }

  @Override protected void setupDagger(ApplicationComponent appComponent) {
    appComponent.inject(this);
  }

  @Override public void updateTableOfContents() {
    tabsPresenter.getCurrentWebView().loadUrl("javascript:(" + parserJS + ")()");
  }

  private void shrinkDrawers() {
    MarginLayoutParams leftLayoutMargins =
        (MarginLayoutParams) tabDrawerLeftContainer.getLayoutParams();
    MarginLayoutParams rightLayoutMargins =
        (MarginLayoutParams) tableDrawerRightContainer.getLayoutParams();

    leftLayoutMargins.topMargin = DimenUtils.getToolbarHeight(KiwixMobileActivityNew.this);
    rightLayoutMargins.topMargin = DimenUtils.getToolbarHeight(KiwixMobileActivityNew.this);
    tabDrawerLeftContainer.setLayoutParams(leftLayoutMargins);
    tableDrawerRightContainer.setLayoutParams(rightLayoutMargins);
  }

  private void expandDrawers() {
    MarginLayoutParams leftLayoutMargins =
        (MarginLayoutParams) tabDrawerLeftContainer.getLayoutParams();
    MarginLayoutParams rightLayoutMargins =
        (MarginLayoutParams) tableDrawerRightContainer.getLayoutParams();
    leftLayoutMargins.topMargin = 0;
    rightLayoutMargins.topMargin = 0;
    tabDrawerLeftContainer.setLayoutParams(leftLayoutMargins);
    tableDrawerRightContainer.setLayoutParams(rightLayoutMargins);
  }

  @Override public KiwixWebView getWebView(String url) {
    KiwixWebView webView;
    if (isHideToolbar) {
      webView = createScrollingWebView();
    } else {
      webView =
          createStaticWebView();
    }
    webView.loadUrl(url);
    webView.loadPrefs();

    return webView;
  }

  @NonNull
  private StaticKiwixWebView createStaticWebView() {
    return new StaticKiwixWebView(KiwixMobileActivityNew.this, this, toolbarContainer);
  }

  @NonNull private KiwixWebView createScrollingWebView() {
    KiwixWebView webView;
    webView = new ScrollingKiwixWebView(KiwixMobileActivityNew.this, this, toolbarContainer);
    ((ScrollingKiwixWebView) webView).setOnToolbarVisibilityChangeListener(
        new ScrollingKiwixWebView.OnToolbarVisibilityChangeListener() {
          @Override
          public void onToolbarDisplayed() {
            shrinkDrawers();
          }

          @Override
          public void onToolbarHidden() {
            expandDrawers();
          }
        }
    );
    return webView;
  }

  @Override public void showRestoreTabSnackbar(final int index) {
    Snackbar snackbar = Snackbar.make(snackbarLayout,
        getString(R.string.tab_closed),
        Snackbar.LENGTH_LONG)
        .setAction(getString(R.string.undo), v -> {
          tabsPresenter.restoreTab(index);
          drawerLayout.openDrawer(GravityCompat.START);
        });
    snackbar.setActionTextColor(Color.WHITE);
    snackbar.show();
  }

  @Override public void closeLeftDrawer() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      new Handler().postDelayed(() -> drawerLayout.closeDrawers(), 150);
    }
  }

  @Override public KiwixWebView addWebViewToFrame(int position) {
    KiwixWebView webView = webViewManager.getTab(position);
    contentFrame.addView(webView);
    return webView;
  }

  @Override public void removeWebViewFrame() {contentFrame.removeAllViews();}

  @Override public void setTabDrawerAdapterSelection(int position) {
    tabDrawerAdapter.setSelected(position);
  }

  public void readAloudMenuClick() {
  }

  @Override public void inflateReadAloudMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_webview_read_aloud, menu);
  }

  @Override public void readSelection(KiwixWebView currentWebView) {
  }

  public void showSearchInText(KiwixWebView webView) {
    compatCallback.setActive();
    compatCallback.setWebView(webView);
    startSupportActionMode(compatCallback);
    compatCallback.showSoftInput();
  }

  @Override public void goToBookmarks() {
    saveTabStates();
    Intent intentBookmarks = new Intent(getBaseContext(), BookmarksActivity.class);
    intentBookmarks.putExtra(EXTRA_BOOKMARK_CONTENTS, bookmarks);
    startActivityForResult(intentBookmarks, REQUEST_BOOKMARK_CHOSEN);
  }

  public void showHelpPage() {
    tabsPresenter.getCurrentWebView().loadUrl("file:///android_res/raw/help.html");
  }

  public void sendContactEmail() {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("plain/text");
    intent.putExtra(Intent.EXTRA_EMAIL, new String[] { contactEmailAddress });
    intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback in " +
        LanguageUtils.getCurrentLocale(this).getDisplayLanguage());
    startActivity(Intent.createChooser(intent, ""));
  }

  @Override
  public void openExternalUrl(Intent intent) {
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(intent);
    } else {
      String error = getString(R.string.no_reader_application_installed);
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
  }

  public boolean openZimFile(File file, boolean clearHistory) {
    if (file.canRead() || Build.VERSION.SDK_INT < 19 || (Constants.IS_CUSTOM_APP
        && Build.VERSION.SDK_INT != 23)) {
      if (file.exists()) {
        if (ZimContentProvider.setZimFile(file.getAbsolutePath()) != null) {

          if (clearHistory) {
            requestClearHistoryAfterLoad = true;
          }
          if (menu != null) {
            // TODO FIX THIS
            //initAllMenuItems();
          } else {
            // Menu may not be initialized yet. In this case
            // signal to menu create to show
            //requestInitAllMenuItems = true;
          }

          //Bookmarks
          bookmarks = new ArrayList<>();
          bookmarksDao = new BookmarksDao(KiwixDatabase.getInstance(this));
          bookmarks =
              bookmarksDao.getBookmarks(ZimContentProvider.getId(), ZimContentProvider.getName());

          openMainPage();
          refreshBookmarks();
          return true;
        } else {
          Toast.makeText(this, getResources().getString(R.string.error_fileinvalid),
              Toast.LENGTH_LONG).show();
          showHelpPage();
        }
      } else {
        Log.e(TAG_KIWIX, "ZIM file doesn't exist at " + file.getAbsolutePath());

        Toast.makeText(this, getResources().getString(R.string.error_filenotfound), Toast.LENGTH_LONG)
            .show();
        showHelpPage();
      }
      return false;
    } else {
      this.file = file;
      ActivityCompat.requestPermissions(this,
          new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
          REQUEST_STORAGE_PERMISSION);
      if (Constants.IS_CUSTOM_APP && Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
        Toast.makeText(this, getResources().getString(R.string.request_storage_custom), Toast.LENGTH_LONG)
            .show();
      } else {
        Toast.makeText(this, getResources().getString(R.string.request_storage), Toast.LENGTH_LONG)
            .show();
      }
      return false;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
      String permissions[], int[] grantResults) {
    switch (requestCode) {
      case REQUEST_STORAGE_PERMISSION: {
        if (grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          finish();
          Intent newZimFile = new Intent(KiwixMobileActivityNew.this, KiwixMobileActivity.class);
          newZimFile.setData(Uri.fromFile(file));
          startActivity(newZimFile);
        } else {
          AlertDialog.Builder builder = new AlertDialog.Builder(this, dialogStyle());
          builder.setMessage(getResources().getString(R.string.reboot_message));
          AlertDialog dialog = builder.create();
          dialog.show();
          finish();
        }
      }
    }
  }

  // Workaround for popup bottom menu on older devices
  public void styleMenuButtons(Menu m) {
    // Find each menu item and set its text colour
    for (int i = 0; i < m.size(); i++) {
      m.getItem(i).setTitle(createMenuItem(m.getItem(i).getTitle().toString()));
    }
  }

  // Create a correctly colored title for menu items
  private SpannableString createMenuItem(String title) {
    SpannableString s = new SpannableString(title);
    if (nightMode) {
      s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
    } else {
      s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
    }
    return s;
  }

  // Create a correctly colored title for menu items
  private SpannableString createMenuText(String title) {
    SpannableString s = new SpannableString(title);
    s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
    return s;
  }

  public void openSearchActivity() {
    final String zimFile = ZimContentProvider.getZimFile();
    Intent i = new Intent(KiwixMobileActivityNew.this, SearchActivity.class);
    i.putExtra(EXTRA_ZIM_FILE, zimFile);
    startActivityForResult(i, REQUEST_FILE_SEARCH);
    overridePendingTransition(0, 0);
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
          if (tabsPresenter.getCurrentWebView().canGoBack()) {
            tabsPresenter.getCurrentWebView().goBack();
          } else {
            finish();
          }
          if (compatCallback.mIsActive) {
            compatCallback.finish();
          }
          return true;
        case KeyEvent.KEYCODE_MENU:
          openOptionsMenu();
          return true;
      }
    }
    return false;
  }

  public void toggleBookmark() {
    //Check maybe need refresh
    String article = tabsPresenter.getCurrentWebView().getUrl();
    boolean isBookmark = false;
    if (article != null && !bookmarks.contains(article)) {
      saveBookmark(article, tabsPresenter.getCurrentWebView().getTitle());
      isBookmark = true;
    } else if (article != null) {
      deleteBookmark(article);
      isBookmark = false;
    }
    popBookmarkSnackbar(isBookmark);
    supportInvalidateOptionsMenu();
  }

  private void popBookmarkSnackbar(boolean isBookmark) {
    if (isBookmark) {
      Snackbar bookmarkSnackbar =
          Snackbar.make(snackbarLayout, getString(R.string.bookmark_added), Snackbar.LENGTH_LONG)
              .setAction(getString(R.string.open), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  goToBookmarks();
                }
              });
      bookmarkSnackbar.setActionTextColor(getResources().getColor(R.color.white));
      bookmarkSnackbar.show();
    } else {
      Snackbar bookmarkSnackbar =
          Snackbar.make(snackbarLayout, getString(R.string.bookmark_removed), Snackbar.LENGTH_LONG);
      bookmarkSnackbar.show();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    invalidateOptionsMenu();
    if (isToolbarHidden != isHideToolbar) {
      isToolbarHidden = isHideToolbar;

      List<WeakReference<KiwixWebView>> newWebViews = new ArrayList<>();
      for (int i = 0; i < webViewManager.size(); i++) {
        KiwixWebView newView = getWebView(webViewManager.getTab(i).getUrl());
        newWebViews.add(i, new WeakReference<KiwixWebView>(newView));
      }
      webViewManager.setWebViews(newWebViews);
      tabsPresenter.selectTab(tabsPresenter.getCurrentWebViewIndex());
    }
    if (refresh) {
      refresh = false;
      recreate();
    }
    if (menu != null) {
      refreshBookmarkSymbol();
    }
    if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
      if (menu != null) {
        menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      }
    } else {
      if (menu != null) {
        menu.getItem(4).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
      }
    }

    Intent intent = getIntent();
    if (intent.getAction() != null) {

      if (intent.getAction().equals(Intent.ACTION_PROCESS_TEXT)) {
        final String zimFile = ZimContentProvider.getZimFile();
        saveTabStates();
        Intent i = new Intent(KiwixMobileActivityNew.this, SearchActivity.class);
        i.putExtra(EXTRA_ZIM_FILE, zimFile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          i.putExtra(Intent.EXTRA_PROCESS_TEXT, intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT));
        }
        intent.setAction("");
        startActivityForResult(i, REQUEST_FILE_SEARCH);
      } else if (intent.getAction().equals(KiwixSearchWidget.TEXT_CLICKED)) {
        intent.setAction("");
        goToSearch(false);
      } else if (intent.getAction().equals(KiwixSearchWidget.STAR_CLICKED)) {
        intent.setAction("");
        goToBookmarks();
      } else if (intent.getAction().equals(KiwixSearchWidget.MIC_CLICKED)) {
        intent.setAction("");
        goToSearch(true);
      }
    }
    updateWidgets(this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    boolean isWidgetSearch = intent.getBooleanExtra(EXTRA_IS_WIDGET_SEARCH, false);
    boolean isWidgetVoiceSearch = intent.getBooleanExtra(EXTRA_IS_WIDGET_VOICE, false);
    boolean isWidgetStar = intent.getBooleanExtra(EXTRA_IS_WIDGET_STAR, false);

    if (isWidgetStar && ZimContentProvider.getId() != null) {
      goToBookmarks();
    } else if (isWidgetSearch && ZimContentProvider.getId() != null) {
      goToSearch(false);
    } else if (isWidgetVoiceSearch && ZimContentProvider.getId() != null) {
      goToSearch(true);
    } else if (isWidgetStar || isWidgetSearch || isWidgetVoiceSearch) {
      manageZimFiles(0);
    }
  }

  private void refreshBookmarks() {
    if (bookmarks != null) {
      bookmarks.clear();
    }
    if (bookmarksDao != null) {
      bookmarks =
          bookmarksDao.getBookmarks(ZimContentProvider.getId(), ZimContentProvider.getName());
    }
  }

  // TODO: change saving bookbark by zim name not id
  private void saveBookmark(String articleUrl, String articleTitle) {
    bookmarksDao.saveBookmark(articleUrl, articleTitle, ZimContentProvider.getId(), ZimContentProvider
        .getName());
    refreshBookmarks();
  }

  private void deleteBookmark(String article) {
    bookmarksDao.deleteBookmark(article, ZimContentProvider.getId(), ZimContentProvider.getName());
    refreshBookmarks();
  }

  public boolean openArticleFromBookmarkTitle(String bookmarkTitle) {
    return openArticle(ZimContentProvider.getPageUrlFromTitle(bookmarkTitle));
  }

  private void contentsDrawerHint() {
    drawerLayout.postDelayed(() -> drawerLayout.openDrawer(GravityCompat.END), 500);

    AlertDialog.Builder builder = new AlertDialog.Builder(this, dialogStyle());
    builder.setMessage(getString(R.string.hint_contents_drawer_message))
        .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {})
        .setTitle(getString(R.string.did_you_know))
        .setIcon(R.drawable.icon_question);
    AlertDialog alert = builder.create();
    alert.show();
  }

  private boolean openArticle(String articleUrl) {
    if (articleUrl != null) {
      tabsPresenter.getCurrentWebView().loadUrl(
          Uri.parse(ZimContentProvider.CONTENT_URI + articleUrl).toString());
    }
    return true;
  }

  @Override public boolean openRandomArticle() {
    String articleUrl = ZimContentProvider.getRandomArticleUrl();
    Log.d(TAG_KIWIX, "openRandomArticle: " + articleUrl);
    return openArticle(articleUrl);
  }

  @Override public boolean openMainPage() {
    String articleUrl = ZimContentProvider.getMainPage();
    return openArticle(articleUrl);
  }

  public static void updateWidgets(Context context) {
    Intent intent = new Intent(context.getApplicationContext(), KiwixSearchWidget.class);
    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
    // since it seems the onUpdate() is only fired on that:
    AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
    int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, KiwixSearchWidget.class));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      widgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list);

      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
      context.sendBroadcast(intent);
    }
  }

  @Override public void setUpWebView() {

    tabsPresenter.getCurrentWebView().getSettings().setJavaScriptEnabled(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true);
    }

    // webView.getSettings().setLoadsImagesAutomatically(false);
    // Does not make much sense to cache data from zim files.(Not clear whether
    // this actually has any effect)
    tabsPresenter.getCurrentWebView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

    // Should basically resemble the behavior when setWebClient not done
    // (i.p. internal urls load in webview, external urls in browser)
    // as currently no custom setWebViewClient required it is commented
    // However, it must notify the bookmark system when a page is finished loading
    // so that it can refresh the menu.

    backToTopButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        KiwixMobileActivityNew.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            tabsPresenter.getCurrentWebView().pageUp(true);
          }
        });
      }
    });
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    toggleActionItemsConfig();
  }

  void toggleActionItemsConfig() {
    if (menu != null) {
      MenuItem random = menu.findItem(R.id.menu_randomarticle);
      MenuItem home = menu.findItem(R.id.menu_home);
      if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
        random.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        home.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      } else {
        random.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        home.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
      }
    }
  }

  public void searchForTitle(String title) {
    String articleUrl;

    if (title.startsWith("A/")) {
      articleUrl = title;
    } else {
      articleUrl = ZimContentProvider.getPageUrlFromTitle(title);
    }
    openArticle(articleUrl);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    Log.i(TAG_KIWIX, "Intent data: " + data);

    switch (requestCode) {
      case REQUEST_FILE_SELECT:
        if (resultCode == RESULT_OK) {
          // The URI of the selected file
          final Uri uri = data.getData();
          File file = null;
          if (uri != null) {
            String path = uri.getPath();
            if (path != null) {
              file = new File(path);
            }
          }
          if (file == null) {
            return;
          }

          finish();
          Intent zimFile = new Intent(KiwixMobileActivityNew.this, KiwixMobileActivity.class);
          zimFile.setData(uri);
          startActivity(zimFile);
        }
        break;
      case REQUEST_FILE_SEARCH:
        if (resultCode == RESULT_OK) {
          String title =
              data.getStringExtra(EXTRA_FILE_SEARCHED).replace("<b>", "").replace("</b>", "");
          searchForTitle(title);
        }
        break;
      case REQUEST_PREFERENCES:
        if (resultCode == RESULT_RESTART) {
          finish();
          startActivity(new Intent(KiwixMobileActivityNew.this, KiwixMobileActivity.class));
        }
        if (resultCode == RESULT_HISTORY_CLEARED) {
          webViewManager.clear();
          tabsPresenter.newTab();
          tabDrawerAdapter.notifyDataSetChanged();
        }
        loadPrefs();
        break;

      case REQUEST_BOOKMARK_CHOSEN:
        if (resultCode == RESULT_OK) {
          boolean itemClicked = data.getBooleanExtra("bookmarkClicked", false);
          if (ZimContentProvider.getId() == null) return;

          //Bookmarks
          bookmarksDao = new BookmarksDao(KiwixDatabase.getInstance(this));
          bookmarks =
              bookmarksDao.getBookmarks(ZimContentProvider.getId(), ZimContentProvider.getName());

          if (itemClicked) {
            String bookmarkChosen;
            if (data.getStringExtra(EXTRA_CHOSE_URL) != null) {
              bookmarkChosen = data.getStringExtra(EXTRA_CHOSE_URL);
              tabsPresenter.newTab();
              tabsPresenter.getCurrentWebView().loadUrl(bookmarkChosen);
            } else {
              tabsPresenter.newTab();
              bookmarkChosen = data.getStringExtra(EXTRA_CHOSE_TITLE);
              openArticleFromBookmarkTitle(bookmarkChosen);
            }
          }
          if (menu != null) {
            refreshBookmarkSymbol();
          }
        }
        break;
      default:
        break;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    this.menu = menu;
    styleMenuButtons(menu);
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
      openSearchActivity();
      return true;
    });

    //return menuPresenter.onCreateOptionsMenu(menu);
    return true;
  }

  // This method refreshes the menu for the bookmark system.
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    toggleActionItemsConfig();
    refreshBookmarkSymbol();
    refreshNavigationButtons();

    if (tabsPresenter.getCurrentWebView().getUrl() == null || tabsPresenter.getCurrentWebView()
        .getUrl()
        .equals("file:///android_res/raw/help.html")) {
      menu.findItem(R.id.menu_read_aloud).setVisible(false);
    } else {
      menu.findItem(R.id.menu_read_aloud).setVisible(true);
    }

    return true;
  }

  public void refreshBookmarkSymbol() { // Checks if current webview is in bookmarks array
    if (menu == null) return;
    if (bookmarks == null || bookmarks.size() == 0) {
      bookmarksDao = new BookmarksDao(KiwixDatabase.getInstance(this));
      bookmarks =
          bookmarksDao.getBookmarks(ZimContentProvider.getId(), ZimContentProvider.getName());
    }
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

  public void refreshNavigationButtons() {
    toggleImageViewGrayFilter(tabBackButton, tabsPresenter.getCurrentWebView().canGoBack());
    toggleImageViewGrayFilter(tabForwardButton, tabsPresenter.getCurrentWebView().canGoForward());
    tabBackButtonContainer.setEnabled(tabsPresenter.getCurrentWebView().canGoBack());
    tabForwardButtonContainer.setEnabled(tabsPresenter.getCurrentWebView().canGoForward());
  }

  public void toggleImageViewGrayFilter(ImageView image, boolean state) {
    Drawable originalIcon = image.getDrawable();
    Drawable res = originalIcon.mutate();
    if (state) {
      res.clearColorFilter();
    } else {
      res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
    }
    image.setImageDrawable(res);
  }

  public void loadPrefs() {

    nightMode = preferences.getBoolean(PREF_NIGHT_MODE, false);
    isBackToTopEnabled = preferences.getBoolean(PREF_BACK_TO_TOP, false);
    isHideToolbar = preferences.getBoolean(PREF_HIDE_TOOLBAR, false);
    boolean isZoomEnabled = preferences.getBoolean(PREF_ZOOM_ENABLED, false);
    isOpenNewTabInBackground = preferences.getBoolean(PREF_NEW_TAB_BACKGROUND, false);

    if (isZoomEnabled) {
      int zoomScale = (int) preferences.getFloat(PREF_ZOOM, 100.0f);
      tabsPresenter.getCurrentWebView().setInitialScale(zoomScale);
    } else {
      tabsPresenter.getCurrentWebView().setInitialScale(0);
    }

    if (!isBackToTopEnabled) {
      backToTopButton.setVisibility(View.INVISIBLE);
    }

    // Night mode status
    Log.d(TAG_KIWIX, "nightMode value (" + nightMode + ")");
    if (nightMode) {
      tabsPresenter.getCurrentWebView().toggleNightMode();
    } else {
      tabsPresenter.getCurrentWebView().deactivateNightMode();
    }
  }

  public void manageZimFiles(int tab) {
    refreshBookmarks();
    final Intent target = new Intent(this, ZimManageActivity.class);
    target.setAction(Intent.ACTION_GET_CONTENT);
    // The MIME data type filter
    target.setType("//");
    target.putExtra(ZimManageActivity.TAB_EXTRA, tab);
    // Only return URIs that can be opened with ContentResolver
    target.addCategory(Intent.CATEGORY_OPENABLE);
    // Force use of our file selection component.
    // (Note may make sense to just define a custom intent instead)

    startActivityForResult(target, REQUEST_FILE_SELECT);
  }

  public void selectSettings() {
    final String zimFile = ZimContentProvider.getZimFile();
    Intent i = new Intent(this, KiwixSettingsActivity.class);
    i.putExtra(EXTRA_ZIM_FILE, zimFile);
    startActivityForResult(i, REQUEST_PREFERENCES);
  }

  public void saveTabStates() {
    SharedPreferences.Editor editor = preferences.edit();

    JSONArray urls = new JSONArray();
    JSONArray positions = new JSONArray();
    for (int i = 0; i < webViewManager.size(); i++) {
      KiwixWebView tab = webViewManager.getTab(i);
      if (tab.getUrl() == null) continue;
      urls.put(tab.getUrl());
      positions.put(tab.getScrollY());
    }

    editor.putString(PREF_CURRENT_FILE, ZimContentProvider.getZimFile());
    editor.putString(PREF_CURRENT_ARTICLES, urls.toString());
    editor.putString(PREF_CURRENT_POSITIONS, positions.toString());
    editor.putInt(PREF_CURRENT_TAB, tabsPresenter.getCurrentWebViewIndex());

    editor.apply();
  }

  public void restoreTabStates() {
    String zimFile = preferences.getString(PREF_CURRENT_FILE, null);
    String zimArticles = preferences.getString(PREF_CURRENT_ARTICLES, null);
    String zimPositions = preferences.getString(PREF_CURRENT_POSITIONS, null);

    int currentTab = preferences.getInt(PREF_CURRENT_TAB, 0);

    openZimFile(new File(zimFile), false);
    try {
      JSONArray urls = new JSONArray(zimArticles);
      JSONArray positions = new JSONArray(zimPositions);
      int i = 0;
      tabsPresenter.getCurrentWebView().loadUrl(urls.getString(i));
      tabsPresenter.getCurrentWebView().setScrollY(positions.getInt(i));
      i++;
      for (; i < urls.length(); i++) {
        tabsPresenter.newTab(urls.getString(i));
        tabsPresenter.getCurrentWebView().setScrollY(positions.getInt(i));
      }
      tabsPresenter.selectTab(currentTab);
    } catch (Exception e) {
      Log.d(TAG_KIWIX, " Kiwix sharedpreferences corrupted");
    }
  }

  private void manageExternalLaunchAndRestoringViewState() {

    if (getIntent().getData() != null) {
      String filePath =
          FileUtils.getLocalFilePathByUri(getApplicationContext(), getIntent().getData());

      if (filePath == null) {
        Toast.makeText(KiwixMobileActivityNew.this, getString(R.string.error_filenotfound), Toast.LENGTH_LONG)
            .show();
        return;
      }

      Log.d(TAG_KIWIX, " Kiwix started from a filemanager. Intent filePath: "
          + filePath
          + " -> open this zimfile and load menu_main page");
      openZimFile(new File(filePath), false);
    } else {
      String zimFile = preferences.getString(PREF_CURRENT_FILE, null);
      if (zimFile != null && new File(zimFile).exists()) {
        Log.d(TAG_KIWIX,
            " Kiwix normal start, zimFile loaded last time -> Open last used zimFile " + zimFile);
        restoreTabStates();
        // Alternative would be to restore webView state. But more effort to implement, and actually
        // fits better normal android behavior if after closing app ("back" button) state is not maintained.
      } else {

        if (Constants.IS_CUSTOM_APP) {
          Log.d(TAG_KIWIX, "Kiwix Custom App starting for the first time. Check Companion ZIM.");

          String currentLocaleCode = Locale.getDefault().toString();
          // Custom App recommends to start off a specific language
          if (Constants.CUSTOM_APP_ENFORCED_LANG.length() > 0 && !Constants.CUSTOM_APP_ENFORCED_LANG
              .equals(currentLocaleCode)) {

            // change the locale machinery
            LanguageUtils.handleLocaleChange(this, Constants.CUSTOM_APP_ENFORCED_LANG);

            // save new locale into preferences for next startup
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PREF_LANGUAGE_CHOOSER, Constants.CUSTOM_APP_ENFORCED_LANG);
            editor.apply();

            // restart activity for new locale to take effect
            this.setResult(1236);
            this.finish();
            this.startActivity(new Intent(this, this.getClass()));
          }

          String filePath = "";
          if (Constants.CUSTOM_APP_HAS_EMBEDDED_ZIM) {
            String appPath = getPackageResourcePath();
            File libDir = new File(appPath.substring(0, appPath.lastIndexOf("/")) + "/lib/");
            if (libDir.exists() && libDir.listFiles().length > 0) {
              filePath = libDir.listFiles()[0].getPath() + "/" + Constants.CUSTOM_APP_ZIM_FILE_NAME;
            }
            if (filePath.isEmpty() || !new File(filePath).exists()) {
              filePath = String.format("/data/data/%s/lib/%s", Constants.CUSTOM_APP_ID,
                  Constants.CUSTOM_APP_ZIM_FILE_NAME);
            }
          } else {
            String fileName = FileUtils.getExpansionAPKFileName(true);
            filePath = FileUtils.generateSaveFileName(fileName);
          }

          if (!FileUtils.doesFileExist(filePath, Constants.CUSTOM_APP_ZIM_FILE_SIZE, false)) {

            AlertDialog.Builder zimFileMissingBuilder =
                new AlertDialog.Builder(this, dialogStyle());
            zimFileMissingBuilder.setTitle(R.string.app_name);
            zimFileMissingBuilder.setMessage(R.string.customapp_missing_content);
            zimFileMissingBuilder.setIcon(R.mipmap.kiwix_icon);
            final Activity activity = this;
            zimFileMissingBuilder.setPositiveButton(getString(R.string.go_to_play_store),
                new OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                    String market_uri = "market://details?id=" + Constants.CUSTOM_APP_ID;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(market_uri));
                    startActivity(intent);
                    activity.finish();
                  }
                });
            zimFileMissingBuilder.setCancelable(false);
            AlertDialog zimFileMissingDialog = zimFileMissingBuilder.create();
            zimFileMissingDialog.show();
          } else {
            openZimFile(new File(filePath), true);
          }
        } else {
          Log.d(TAG_KIWIX,
              " Kiwix normal start, no zimFile loaded last time  -> display help page");
          showHelpPage();
        }
      }
    }
  }

  @Override
  public void onPause() {
    super.onPause();

    saveTabStates();
    refreshBookmarks();

    Log.d(TAG_KIWIX,
        "onPause Save currentzimfile to preferences:" + ZimContentProvider.getZimFile());
  }

  @Override public void webViewUrlLoading() {
    if (isFirstRun) {
      contentsDrawerHint();
      SharedPreferences.Editor editor = preferences.edit();
      editor.putBoolean("isFirstRun", false); // It is no longer the first run
      isFirstRun = false;
      editor.apply();
    }
  }

  @Override public void webViewUrlFinishedLoading() {
    updateTableOfContents();
    tabDrawerAdapter.notifyDataSetChanged();

    if (menu != null)
      refreshBookmarkSymbol();
  }

  @Override public void webViewFailedLoading(String url) {
    String error = String.format(getString(R.string.error_articleurlnotfound), url);
    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
  }

  @Override public void webViewProgressChanged(int progress) {
    progressBar.setProgress(progress);
    if (progress == 100) {
      Log.d(KiwixMobileActivity.TAG_KIWIX, "Loading article finished.");
      if (requestClearHistoryAfterLoad) {
        Log.d(KiwixMobileActivity.TAG_KIWIX,
            "Loading article finished and requestClearHistoryAfterLoad -> clearHistory");
        tabsPresenter.getCurrentWebView().clearHistory();
        requestClearHistoryAfterLoad = false;
      }

      Log.d(KiwixMobileActivity.TAG_KIWIX, "Loaded URL: " + tabsPresenter.getCurrentWebView()
          .getUrl());
    }
  }

  @Override public void refreshTabDrawerAdapter() {
    tabDrawerAdapter.notifyDataSetChanged();
  }

  @Override public int getSelectedTabDrawerPosition() {
    return tabDrawerAdapter.getSelectedPosition();
  }

  @Override public void initParser(KiwixWebView webView) {
    documentParser.initInterface(webView);
  }

  @Override public void webViewPageChanged(int page, int maxPages) {
    if (isBackToTopEnabled) {
      if (tabsPresenter.getCurrentWebView().getScrollY() > 200) {
        if (backToTopButton.getVisibility() == View.INVISIBLE) {
          backToTopButton.setText(R.string.button_backtotop);
          backToTopButton.setVisibility(View.VISIBLE);

          backToTopButton.startAnimation(
              AnimationUtils.loadAnimation(KiwixMobileActivityNew.this, android.R.anim.fade_in));
          backToTopButton.setVisibility(View.INVISIBLE);
          Animation fadeAnimation =
              AnimationUtils.loadAnimation(KiwixMobileActivityNew.this, android.R.anim.fade_out);
          fadeAnimation.setStartOffset(1200);
          backToTopButton.startAnimation(fadeAnimation);
        }
      } else {
        if (backToTopButton.getVisibility() == View.VISIBLE) {
          backToTopButton.setVisibility(View.INVISIBLE);

          backToTopButton.clearAnimation();
          backToTopButton.startAnimation(
              AnimationUtils.loadAnimation(KiwixMobileActivityNew.this, android.R.anim.fade_out));
        } else {
          backToTopButton.clearAnimation();
        }
      }
    }
  }

  @Override public void webViewLongClick(final String url) {
    boolean handleEvent = false;
    if (url.startsWith(ZimContentProvider.CONTENT_URI.toString())) {
      // This is my web site, so do not override; let my WebView load the page
      handleEvent = true;
    } else if (url.startsWith("file://")) {
      // To handle help page (loaded from resources)
      handleEvent = true;
    } else if (url.startsWith(ZimContentProvider.UI_URI.toString())) {
      handleEvent = true;
    }

    if (handleEvent) {
      AlertDialog.Builder builder =
          new AlertDialog.Builder(KiwixMobileActivityNew.this, dialogStyle());

      builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          if (isOpenNewTabInBackground) {
            tabsPresenter.newTabInBackground(url);
            Snackbar snackbar = Snackbar.make(snackbarLayout,
                getString(R.string.new_tab_snackbar),
                Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.open), v -> {
                  if (webViewManager.size() > 1) tabsPresenter.selectTab(webViewManager.size() - 1);
                });
            snackbar.setActionTextColor(getResources().getColor(R.color.white));
            snackbar.show();
          } else {
            tabsPresenter.newTab(url);
          }
        }
      });
      builder.setNegativeButton(android.R.string.no, null);
      builder.setMessage(getString(R.string.open_in_new_tab));
      AlertDialog dialog = builder.create();
      dialog.show();
    }
  }

  @Override public void showLoading() {

  }

  @Override public void hideLoading() {

  }

  @Override public void showError(String message) {

  }

  @Override public void showRateDialog(String title, String body,
      String positive, String negative, String neutral,
      OnClickListener positiveListener,
      OnClickListener negativeListener,
      OnClickListener neutralListener) {
    new AlertDialog.Builder(this, dialogStyle())
        .setTitle(title)
        .setMessage(body)
        .setPositiveButton(positive, positiveListener)
        .setNegativeButton(negative, negativeListener)
        .setNeutralButton(neutral, neutralListener)
        .setIcon(ContextCompat.getDrawable(this, R.mipmap.kiwix_icon))
        .show();
  }

  @Override public void openPlayStorePage() {
    Intent intent = new Intent(Intent.ACTION_VIEW, KIWIX_LOCAL_MARKET_URI);
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
        | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

    try {
      startActivity(intent);
    } catch (ActivityNotFoundException e) {
      startActivity(new Intent(Intent.ACTION_VIEW, KIWIX_BROWSER_MARKET_URI));
    }
  }
}
