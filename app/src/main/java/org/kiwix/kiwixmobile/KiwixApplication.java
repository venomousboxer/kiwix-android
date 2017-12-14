package org.kiwix.kiwixmobile;

import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import org.kiwix.kiwixmobile.crash_handler.KiwixErrorActivity;
import org.kiwix.kiwixmobile.di.components.ApplicationComponent;
import org.kiwix.kiwixmobile.di.components.DaggerApplicationComponent;
import org.kiwix.kiwixmobile.di.modules.ApplicationModule;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class KiwixApplication extends MultiDexApplication {

  private static KiwixApplication application;
  private ApplicationComponent applicationComponent;

  @Override
  public void onCreate() {
    super.onCreate();

    CaocConfig.Builder.create()
            .enabled(true)
            .errorActivity(KiwixErrorActivity.class)
            .apply();

    if ( isExternalStorageWritable() ) {

      File appDirectory = new File( Environment.getExternalStorageDirectory() + "/KiwixApp" );
      File logDirectory = new File( appDirectory + "/log" );
      File logFile = new File( logDirectory, "logcat.txt" );


      // create app folder
      if ( !appDirectory.exists() ) {
        appDirectory.mkdir();
      }

      // create log folder
      if ( !logDirectory.exists() ) {
        logDirectory.mkdir();
      }

      if (logFile.exists() && logFile.isFile())
      {
        logFile.delete();
      }

      // clear the previous logcat and then write the new one to the file
      try {
        Process process = Runtime.getRuntime().exec("logcat -c");
        process = Runtime.getRuntime().exec("logcat -s kiwix -f " + logFile);
      } catch ( IOException e ) {
        e.printStackTrace();
      }

    } else if ( isExternalStorageReadable() ) {
      // only readable
    } else {
      // not accessible
    }
  }

  /* Checks if external storage is available for read and write */
  public boolean isExternalStorageWritable() {
    String state = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(state);
  }

  /* Checks if external storage is available to at least read */
  public boolean isExternalStorageReadable() {
    String state = Environment.getExternalStorageState();
    if ( Environment.MEDIA_MOUNTED.equals( state ) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
      return true;
    }
    return false;
  }



  public static KiwixApplication getInstance() {
    return application;
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    application = this;
    initializeInjector();
  }

  private void initializeInjector() {
    setApplicationComponent(DaggerApplicationComponent.builder()
        .applicationModule(new ApplicationModule(this))
        .build());
  }

  public ApplicationComponent getApplicationComponent() {
    return this.applicationComponent;
  }

  public void setApplicationComponent(ApplicationComponent applicationComponent) {
    this.applicationComponent = applicationComponent;
  }
}
