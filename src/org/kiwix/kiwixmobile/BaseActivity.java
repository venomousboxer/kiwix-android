package org.kiwix.kiwixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.soundcloud.lightcycle.ActivityLightCycle;
import com.soundcloud.lightcycle.ActivityLightCycleDispatcher;
import com.soundcloud.lightcycle.LightCycleDispatcher;
import com.soundcloud.lightcycle.LightCycles;
import org.kiwix.kiwixmobile.di.components.ApplicationComponent;

public abstract class BaseActivity<HostType>
    extends AppCompatActivity
    implements LightCycleDispatcher<ActivityLightCycle<HostType>> {

  private final ActivityLightCycleDispatcher<HostType> lightCycleDispatcher;

  public BaseActivity() {
    lightCycleDispatcher = new ActivityLightCycleDispatcher<>();
  }

  @Override
  public void bind(ActivityLightCycle<HostType> lightCycle) {
    lightCycleDispatcher.bind(lightCycle);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView();
    setupDagger(KiwixApplication.getInstance().getApplicationComponent());

    LightCycles.bind(this);
    lightCycleDispatcher.onCreate(host(), savedInstanceState);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    lightCycleDispatcher.onNewIntent(host(), intent);
  }

  @Override
  protected void onStart() {
    super.onStart();
    lightCycleDispatcher.onStart(host());
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return lightCycleDispatcher.onOptionsItemSelected(host(), item);
  }

  @Override
  protected void onStop() {
    lightCycleDispatcher.onStop(host());
    super.onStop();
  }

  @Override
  protected void onResume() {
    super.onResume();
    lightCycleDispatcher.onResume(host());
  }

  @Override
  protected void onPause() {
    lightCycleDispatcher.onPause(host());
    super.onPause();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    lightCycleDispatcher.onSaveInstanceState(host(), outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    lightCycleDispatcher.onRestoreInstanceState(host(), savedInstanceState);
  }

  @Override
  protected void onDestroy() {
    lightCycleDispatcher.onDestroy(host());
    super.onDestroy();
  }

  @SuppressWarnings("unchecked")
  private HostType host() {
    return (HostType) this;
  }

  protected abstract void setContentView();

  protected abstract void setupDagger(ApplicationComponent appComponent);
}