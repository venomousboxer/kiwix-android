package org.kiwix.kiwixmobile;

import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import org.kiwix.kiwixmobile.di.components.ApplicationComponent;
import org.kiwix.kiwixmobile.di.components.DaggerApplicationComponent;
import org.kiwix.kiwixmobile.di.modules.ApplicationModule;

import java.io.File;
import java.io.IOException;

public class KiwixApplication extends MultiDexApplication {

    private static KiwixApplication application;
    private ApplicationComponent applicationComponent;
    private File logFile;

    @Override
    public void onCreate() {
        super.onCreate();

        if (isExternalStorageWritable()) {

            File appDirectory = new File(Environment.getExternalStorageDirectory() + "/KiwixApp");
            File logDirectory = new File(appDirectory + "/log");
            logFile = new File(logDirectory, "logcat.txt");


            // create app folder
            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }

            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }

            if (logFile.exists() && logFile.isFile()) {
                logFile.delete();
            }


            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -s kiwix -f " + logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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
