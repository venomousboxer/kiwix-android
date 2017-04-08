package org.kiwix.kiwixmobile;

import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.kiwix.kiwixmobile.views.web.KiwixWebView;

public class TabDrawerAdapter extends RecyclerView.Adapter<TabDrawerAdapter.ViewHolder> {
  private TabClickListener listener;
  private int selectedPosition = 0;
  private WebViewManager webViewManager;

  public TabDrawerAdapter(WebViewManager webViewManager) {
    this.webViewManager = webViewManager;
  }

  @Override
  public TabDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tabs_list, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    KiwixWebView webView = webViewManager.getTab(position);
    holder.title.setText(webView.getTitle());
    holder.exit.setOnClickListener(v -> listener.onCloseTab(v, position));
    holder.itemView.setOnClickListener(v -> {
      listener.onSelectTab(v, position);
      selectedPosition = holder.getAdapterPosition();
      notifyDataSetChanged();
      holder.itemView.setActivated(true);
    });
    holder.itemView.setActivated(holder.getAdapterPosition() == selectedPosition);
  }

  @Override
  public int getItemCount() {
    return webViewManager.size();
  }

  public void setSelected(int position) {
    this.selectedPosition = position;
  }

  public int getSelectedPosition() {
    return selectedPosition;
  }

  public void setTabClickListener(TabClickListener listener) {
    this.listener = listener;
  }

  public interface TabClickListener {
    void onSelectTab(View view, int position);

    void onCloseTab(View view, int position);
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public @BindView(R.id.titleText) TextView title;
    public @BindView(R.id.exitButton) ImageView exit;

    public ViewHolder(View v) {
      super(v);
      ButterKnife.bind(this, v);
    }
  }
}