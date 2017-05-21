package org.kiwix.kiwixmobile.readinglists;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter_extensions.ActionModeHelper;

import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.database.KiwixDatabase;
import org.kiwix.kiwixmobile.database.ReadingListFolderDao;
import org.kiwix.kiwixmobile.readinglists.entities.ReadinglistFolder;
import org.kiwix.kiwixmobile.readinglists.lists.ReadingListArticleItem;
import org.kiwix.kiwixmobile.readinglists.lists.ReadingListItem;

import java.util.ArrayList;
import java.util.Set;


public class ReadingFoldersFragment extends Fragment implements FastAdapter.OnClickListener<ReadingListItem>, FastAdapter.OnLongClickListener<ReadingListItem> {

    private FastAdapter<ReadingListItem> fastAdapter;
    private ItemAdapter<ReadingListItem> itemAdapter;
    private ReadingListFolderDao readinglistFoldersDao;
    private ArrayList<ReadinglistFolder> folders;
    private final String FRAGMENT_ARGS_FOLDER_TITLE = "requested_folder_title";
    private ActionMode actionMode;
    private RecyclerView foldersRecyclerview;

    public ReadingFoldersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reading_folders, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        foldersRecyclerview = (RecyclerView) view.findViewById(R.id.readinglist_folders_list);

        fastAdapter = new FastAdapter<>();
        itemAdapter = new ItemAdapter<>();

        setupFastAdapter();

        readinglistFoldersDao = new ReadingListFolderDao(KiwixDatabase.getInstance(getActivity()));

        refreshFolders();
    }

    private void setupFastAdapter() {


        fastAdapter.withOnClickListener(this);
        fastAdapter.withOnLongClickListener(this);
        fastAdapter.withSelectOnLongClick(false);
        fastAdapter.withSelectable(false);
        fastAdapter.withMultiSelect(true);
        foldersRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        foldersRecyclerview.setAdapter(itemAdapter.wrap(fastAdapter));
    }


    void refreshFolders() {
        folders = readinglistFoldersDao.getFolders();
        itemAdapter.clear();
        for (ReadinglistFolder folder: folders) {
            itemAdapter.add(new ReadingListItem(folder.getFolderTitle()));
        }
    }


    private void deleteSelectedItems() {
        Set<ReadingListItem> selectedItems = fastAdapter.getSelectedItems();
        readinglistFoldersDao.deleteFolders(selectedItems);

        for (ReadingListItem item : selectedItems) {
            itemAdapter.remove(itemAdapter.getAdapterPosition(item));
        }
    }

    @Override
    public boolean onClick(View v, IAdapter<ReadingListItem> adapter, ReadingListItem item, int position) {
        if (actionMode != null) {
            toggleSelection(position);
            return true;
        }
        ReadingListFragment readingListFragment = new ReadingListFragment();
        Bundle args = new Bundle();
        args.putString(FRAGMENT_ARGS_FOLDER_TITLE,item.getTitle());
        readingListFragment.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.readinglist_fragment_container, readingListFragment);
        transaction.addToBackStack(null);

        transaction.commit();
        return true;
    }


    @Override
    public boolean onLongClick(View v, IAdapter<ReadingListItem> adapter, ReadingListItem item, int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionModeCallback());
        }

        toggleSelection(position);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpToolbar();
    }

    private void setUpToolbar() {
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if ( toolbar != null) {
            toolbar.setTitle("Reading Lists");
        }
    }

    private void toggleSelection(int position) {
        fastAdapter.toggleSelection(position);
        int count = fastAdapter.getSelectedItems().size();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    /**
     * Our ActionBarCallBack to showcase the CAB
     */

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.actionmenu_readinglist_folders, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.actionmenu_readinglist_delete:
                    deleteSelectedItems();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for (ReadingListItem item:fastAdapter.getSelectedItems()) {
                fastAdapter.toggleSelection(fastAdapter.getPosition(item));
            }
            actionMode = null;
        }
    }



}
