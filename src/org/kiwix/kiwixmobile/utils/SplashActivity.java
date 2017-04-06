package org.kiwix.kiwixmobile.utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.kiwix.kiwixmobile.KiwixMobileActivityNew;

public class SplashActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = new Intent(this, KiwixMobileActivityNew.class);
    startActivity(intent);
    finish();
  }
}