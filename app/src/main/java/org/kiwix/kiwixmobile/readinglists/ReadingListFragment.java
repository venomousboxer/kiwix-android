package org.kiwix.kiwixmobile.readinglists;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter_extensions.ActionModeHelper;

import org.kiwix.kiwixmobile.KiwixMobileActivity;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.database.KiwixDatabase;
import org.kiwix.kiwixmobile.database.ReadingListFolderDao;
import org.kiwix.kiwixmobile.readinglists.entities.BookmarkArticle;
import org.kiwix.kiwixmobile.readinglists.entities.ReadinglistFolder;
import org.kiwix.kiwixmobile.readinglists.lists.ReadingListArticleItem;

import java.util.ArrayList;
import java.util.Set;

import static org.kiwix.kiwixmobile.R.id.toolbar;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReadingListFragment extends Fragment implements FastAdapter.OnClickListener<ReadingListArticleItem> {

    private FastAdapter<ReadingListArticleItem> fastAdapter;
    private ItemAdapter<ReadingListArticleItem> itemAdapter;
    private final String FRAGMENT_ARGS_FOLDER_TITLE = "requested_folder_title";
    private ReadingListFolderDao readinglistFoldersDao;
    private ArrayList<BookmarkArticle> articles;
    private RecyclerView readinglistRecyclerview;
    private String folderTitle = null;


    public ReadingListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_readinglists_articles, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setUpToolbar();
        folderTitle = this.getArguments().getString(FRAGMENT_ARGS_FOLDER_TITLE);
    }

    private void setUpToolbar() {
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (folderTitle != null && toolbar != null) {
            toolbar.setTitle(folderTitle);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reading_list, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        readinglistRecyclerview = (RecyclerView) view.findViewById(R.id.readinglist_articles_list);

        setupFastAdapter();

        // should be injected in presenter when moving to mvp
        readinglistFoldersDao = new ReadingListFolderDao(KiwixDatabase.getInstance(getActivity()));
        loadArticlesOfFolder();

    }


    private void setupFastAdapter() {

        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();

        fastAdapter.withOnClickListener(this);
        fastAdapter.withSelectOnLongClick(false);
        fastAdapter.withSelectable(false);
        fastAdapter.withMultiSelect(true);
        readinglistRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        readinglistRecyclerview.setAdapter(itemAdapter.wrap(fastAdapter));

    }


    void loadArticlesOfFolder() {
        articles = readinglistFoldersDao.getArticlesOfFolder(new ReadinglistFolder(folderTitle));
        for (BookmarkArticle article: articles) {
            itemAdapter.add(new ReadingListArticleItem(article.getArticleTitle(),article.getArticleUrl()));
        }
    }


    private void deleteSelectedItems() {
        Set<ReadingListArticleItem> selectedItems = fastAdapter.getSelectedItems();
        readinglistFoldersDao.deleteArticles(selectedItems);
        loadArticlesOfFolder();
    }

    @Override
    public boolean onClick(View v, IAdapter<ReadingListArticleItem> adapter, ReadingListArticleItem item, int position) {
        Intent intent = new Intent(getActivity(), KiwixMobileActivity.class);
        if (!item.getArticle_url().equals("null")) {
            intent.putExtra("choseXURL", item.getArticle_url());
        } else {
            intent.putExtra("choseXTitle", item.getTitle());
        }
        intent.putExtra("bookmarkClicked", true);
        startActivity(intent);
        getActivity().finish();
        return true;
    }



}
