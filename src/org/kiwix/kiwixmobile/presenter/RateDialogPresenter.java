package org.kiwix.kiwixmobile.presenter;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.soundcloud.lightcycle.ActivityLightCycleDispatcher;
import javax.inject.Inject;
import org.kiwix.kiwixmobile.R;
import org.kiwix.kiwixmobile.presenter.callback.MainViewCallback;
import org.kiwix.kiwixmobile.utils.NetworkUtils;
import org.kiwix.kiwixmobile.utils.RateAppCounter;

public class RateDialogPresenter extends ActivityLightCycleDispatcher<MainViewCallback> {

  private RateAppCounter rateAppCounter;
  private Context context;

  // TODO null this on onDestroy
  private MainViewCallback callback;

  @Inject public RateDialogPresenter(RateAppCounter rateAppCounter, Context context) {
    this.rateAppCounter = rateAppCounter;
    this.context = context;
  }

  @Override public void onCreate(MainViewCallback host, @Nullable Bundle bundle) {
    super.onCreate(host, bundle);
    this.callback = host;
    int count = rateAppCounter.getCount();
    rateAppCounter.setCount(++count);

    if (count >= 5
        && !rateAppCounter.getNoThanksState()
        && NetworkUtils.isNetworkAvailable(context)) {
      showRateDialog();
    }
  }

  private void showRateDialog() {
    String title = context.getString(R.string.rate_dialog_title);
    String body = String.format("%s %s %s",
        context.getString(R.string.rate_dialog_msg_1),
        context.getString(R.string.app_name),
        context.getString(R.string.rate_dialog_msg_2)
    );
    String positive = context.getString(R.string.rate_dialog_positive);
    String negative = context.getString(R.string.rate_dialog_negative);
    String neutral = context.getString(R.string.rate_dialog_neutral);

    OnClickListener positiveListener = (dialog, id) -> {
      rateAppCounter.setNoThanksState(true);
      callback.openPlayStorePage();
    };
    OnClickListener negativeListener = (dialog, id) -> rateAppCounter.setNoThanksState(true);
    OnClickListener neutralListener = (dialog, id) -> rateAppCounter.setCount(0);

    callback.showRateDialog(title, body, positive, negative, neutral, positiveListener, negativeListener, neutralListener);
  }
}
