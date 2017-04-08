package org.kiwix.kiwixmobile.readinglists;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.database.KiwixDatabase;
import org.kiwix.kiwixmobile.database.ReadingListFolderDao;
import org.kiwix.kiwixmobile.readinglists.entities.BookmarkArticle;
import org.kiwix.kiwixmobile.readinglists.entities.ReadinglistFolder;
import org.kiwix.kiwixmobile.readinglists.lists.ReadingListItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AddToReadingListDialog extends ExtendedBottomSheetDialogFragment implements FastAdapter.OnClickListener<ReadingListItem> {


  private FastAdapter<ReadingListItem> fastAdapter;
  private ItemAdapter<ReadingListItem> itemAdapter;
  private View mainContainer;
  private CreateButtonClickListener createClickListener = new CreateButtonClickListener();
  @Nullable
  private DialogInterface.OnDismissListener dismissListener;
  //    private ReadingListItemCallback listItemCallback = new ReadingListItemCallback();
  private String pageTitle;
  private String pageUrl;

  private ReadingListFolderDao readinglistDao;
  @BindView(R.id.snackbar_layout)
  public FrameLayout snackbarLayout;

  private BookmarkArticle currentArticle;
  private View onboardingButton;
  private View onboardingContainer;
  private ArrayList<ReadinglistFolder> folders;


  public static AddToReadingListDialog newInstance(String articleTitle, @NonNull String articleUrl) {
    return newInstance(articleTitle, articleUrl, null);
  }

  public static AddToReadingListDialog newInstance(@NonNull String articleTitle, String articleUrl,
                                                   @Nullable DialogInterface.OnDismissListener listener) {
    AddToReadingListDialog dialog = new AddToReadingListDialog();
    Bundle args = new Bundle();
    args.putString("articleTitle", articleTitle);
    args.putString("articleUrl", articleUrl);

    dialog.setArguments(args);
    dialog.setOnDismissListener(listener);
    return dialog;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    pageTitle = args.getString("articleTitle");
    pageUrl = args.getString("articleUrl");
    currentArticle = new BookmarkArticle(pageUrl, pageTitle);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    readinglistDao = new ReadingListFolderDao(KiwixDatabase.getInstance(getActivity()));
    folders = readinglistDao.getFolders();

    View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_to_readinglist, container);
    onboardingContainer = rootView.findViewById(R.id.onboarding_container);
    onboardingButton = rootView.findViewById(R.id.onboarding_button);
    mainContainer = rootView.findViewById(R.id.dialog_add_to_readinglist_main_content_container);

    RecyclerView readingListView = (RecyclerView) rootView.findViewById(R.id.list_of_lists);
    fastAdapter = new FastAdapter<>();
    itemAdapter = new ItemAdapter<>();

    fastAdapter.withSelectOnLongClick(false);
    fastAdapter.withSelectable(false);
    fastAdapter.withOnClickListener(this);
    readingListView.setLayoutManager(new LinearLayoutManager(getActivity()));

    readingListView.setAdapter(itemAdapter.wrap(fastAdapter));

    View createButton = rootView.findViewById(R.id.create_button);
    createButton.setOnClickListener(createClickListener);

    View closeButton = rootView.findViewById(R.id.close_button);
    closeButton.setOnClickListener(v -> dismiss());

    checkAndShowOnboarding();
    updateLists();
    return rootView;
  }


  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(getActivity());
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return super.onCreateDialog(savedInstanceState);
  }

  @Override
  public void dismiss() {
    super.dismiss();
    if (dismissListener != null) {
      dismissListener.onDismiss(null);
    }
  }

  public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
    dismissListener = listener;
  }


  private void updateLists() {
    for (ReadinglistFolder folder : folders) {
      itemAdapter.add(new ReadingListItem(folder.getFolderTitle()));
    }
  }

  @Override
  public boolean onClick(View v, IAdapter<ReadingListItem> adapter, ReadingListItem item, int position) {
    dismiss();
    currentArticle.setParentReadinglist(item.getTitle());
    readinglistDao.saveBookmark(currentArticle);
    showAddedToListSnackbar();
    return true;
  }

  private class CreateButtonClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      dismiss();
      showCreateListDialog();
    }
  }

  private void showCreateListDialog() {
    new MaterialDialog.Builder(getActivity())
        .title("Create a new reading list")
        .content("Name your folder:")
        .inputType(InputType.TYPE_CLASS_TEXT)
        .input(null, "My readinglist", (dialog, input) -> {
          ReadinglistFolder newReadinlistFolder = new ReadinglistFolder(input.toString());
          readinglistDao.saveFolder(newReadinlistFolder);
          currentArticle.setParentReadinglist(input.toString());
          readinglistDao.saveBookmark(currentArticle);
          showAddedToListSnackbar();
        }).show();

  }


  private void checkAndShowOnboarding() {
    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

    boolean isOnboarding = settings.getBoolean("runReadinglistDialogOnBoarding", true);

    onboardingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onboardingContainer.setVisibility(View.GONE);
        mainContainer.setVisibility(View.VISIBLE);

        // set tutorial off
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("runReadinglistDialogOnBoarding",false);
        editor.apply();
        if (folders.isEmpty()) {
          showCreateListDialog();
        }
      }
    });
    mainContainer.setVisibility(isOnboarding ? View.GONE : View.VISIBLE);
    onboardingContainer.setVisibility(isOnboarding ? View.VISIBLE : View.GONE);
  }


  private void showAddedToListSnackbar() {
    // TODO: after main activity's MVPed insert presenter on instantiation which will be able to trigger snackbar
//        Snackbar addedToListSnackbar =
//            Snackbar.make(, "Article added to list", Snackbar.LENGTH_LONG);
//        addedToListSnackbar.setActionTextColor(getResources().getColor(R.color.white));
//        addedToListSnackbar.show();
  }


}
