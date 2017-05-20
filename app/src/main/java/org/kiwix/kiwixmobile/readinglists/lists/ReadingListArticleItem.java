package org.kiwix.kiwixmobile.readinglists.lists;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;

import org.kiwix.kiwixmobile.R;

import java.util.List;

/**
 * Created by EladKeyshawn on 04/04/2017.
 */

public class ReadingListArticleItem extends AbstractItem<ReadingListArticleItem, ReadingListArticleItem.ViewHolder>{
    public String title;
    public String article_url;

    public ReadingListArticleItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getArticle_url() {
        return article_url;
    }

    //The unique ID for this type of item
    @Override
    public int getType() {
        return 0;
    }

    //The layout to be used for this type of item
    @Override
    public int getLayoutRes() {
        return R.layout.item_reading_list_article;
    }

    //The logic to bind your data to the view

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);

        if (holder.title != null) {
            holder.title.setText(this.title);
        }
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;

        public ViewHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.readinglist_article_item_title);
        }
    }
}
