package org.kiwix.kiwixmobile;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.kiwix.kiwixmobile.di.components.ApplicationComponent;
import org.kiwix.kiwixmobile.di.components.DaggerApplicationComponent;
import org.kiwix.kiwixmobile.di.modules.ApplicationModule;

public class KiwixApplication extends Application {

  private static KiwixApplication application;
  private ApplicationComponent applicationComponent;
  private Tracker tracker;

  public static KiwixApplication getInstance() {
    return application;
  }

  synchronized public Tracker getDefaultTracker () {
    if (tracker == null) {
      GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
      // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
      tracker = analytics.newTracker(R.xml.global_tracker);
    }
    return tracker;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    application = this;
    initializeInjector();
  }

  private void initializeInjector() {
    this.applicationComponent = DaggerApplicationComponent.builder()
        .applicationModule(new ApplicationModule(this))
        .build();
  }

  public ApplicationComponent getApplicationComponent() {
    return this.applicationComponent;
  }
}
