package org.kiwix.kiwixmobile.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.soundcloud.lightcycle.ActivityLightCycleDispatcher;
import javax.inject.Inject;
import org.kiwix.kiwixmobile.presenter.callback.MainViewCallback;

public class MainViewPresenter extends ActivityLightCycleDispatcher<MainViewCallback> {

  @Inject MainViewPresenter () {
  }

  @Override public void onCreate(MainViewCallback host, @Nullable Bundle bundle) {
    super.onCreate(host, bundle);
  }
}
