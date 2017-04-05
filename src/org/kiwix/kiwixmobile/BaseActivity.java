package org.kiwix.kiwixmobile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import org.kiwix.kiwixmobile.di.components.ApplicationComponent;

public abstract class BaseActivity extends AppCompatActivity {

  private Presenter presenter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupDagger(KiwixApplication.getInstance().getApplicationComponent());
    attachPresenter();
  }

  @Override protected void onResume() {
    super.onResume();
    if (presenter != null) presenter.resume();
  }

  @Override protected void onPause() {
    super.onPause();
    if (presenter != null) presenter.pause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (presenter != null) presenter.destroy();
  }

  protected abstract void setupDagger(ApplicationComponent appComponent);

  public abstract void attachPresenter();

  protected void attachPresenter(Presenter presenter) {
    this.presenter = presenter;
  }
}
