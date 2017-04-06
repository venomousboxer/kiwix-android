package org.kiwix.kiwixmobile.di.modules;

import android.content.Context;
import android.content.SharedPreferences;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import org.kiwix.kiwixmobile.utils.files.FileReader;

import static org.kiwix.kiwixmobile.constants.PreferenceTag.PREF_FILE_KIWIX;

@Module public class MainViewModule {

  @Provides @Named("ParserJS") String provideParserJS(Context context) {
    return new FileReader().readFile("js/documentParser.js", context);
  }

  @Provides SharedPreferences provideSharedPreferences(Context context){
    return context.getSharedPreferences(PREF_FILE_KIWIX, 0);
  }
}
