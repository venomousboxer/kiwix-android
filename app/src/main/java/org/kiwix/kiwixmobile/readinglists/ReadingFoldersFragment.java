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


public class ReadingFoldersFragment extends Fragment implements FastAdapter.OnClickListener<ReadingListItem> {

    private FastAdapter<ReadingListItem> fastAdapter;
    private ItemAdapter<ReadingListItem> itemAdapter;
    private ReadingListFolderDao readinglistFoldersDao;
    private ArrayList<ReadinglistFolder> folders;
    private final String FRAGMENT_ARGS_FOLDER_TITLE = "requested_folder_title";
    private ActionModeHelper mActionModeHelper;
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

        mActionModeHelper = new ActionModeHelper(fastAdapter, R.menu.actionmenu_readinglist, new ReadingFoldersFragment.ActionBarCallBack());



        setupFastAdapter();

        readinglistFoldersDao = new ReadingListFolderDao(KiwixDatabase.getInstance(getActivity()));

        refreshFolders();
    }

    private void setupFastAdapter() {



        fastAdapter.withOnClickListener(this);
        fastAdapter.withSelectOnLongClick(false);
        fastAdapter.withSelectable(false);
        fastAdapter.withMultiSelect(true);
        foldersRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        foldersRecyclerview.setAdapter(itemAdapter.wrap(fastAdapter));


        fastAdapter.withOnPreClickListener((v, adapter, item, position) -> {
            //we handle the default onClick behavior for the actionMode. This will return null if it didn't do anything and you can handle a normal onClick
            Boolean res = mActionModeHelper.onClick(item);
            return mActionModeHelper.getActionMode() != null;
        });

        fastAdapter.withOnPreLongClickListener((v, adapter, item, position) -> {
            ActionMode actionMode = mActionModeHelper.onLongClick((AppCompatActivity)getActivity(),position);

            if (actionMode != null) {

            }

            //if we have no actionMode we do not consume the event
            return actionMode != null;
        });
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

    /**
     * Our ActionBarCallBack to showcase the CAB
     */
    class ActionBarCallBack implements ActionMode.Callback {

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
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    }



}
