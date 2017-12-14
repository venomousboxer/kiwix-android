package org.kiwix.kiwixmobile.crash_handler;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.kiwix.kiwixmobile.KiwixMobileActivity;
import org.kiwix.kiwixmobile.R;

import butterknife.BindView;
import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class KiwixErrorActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.activity_error_report_btn)
    Button reportBtn;

    @BindView(R.id.activity_error_restart_btn)
    Button restartBtn;

    private String stackTrace;
    private CaocConfig crashReportConfig;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kiwix_error);


        stackTrace = CustomActivityOnCrash.getAllErrorDetailsFromIntent(this,getIntent());
        crashReportConfig = CustomActivityOnCrash.getConfigFromIntent(getIntent());

        restartBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_error_restart_btn: {
                Intent restartIntent = new Intent(this,KiwixMobileActivity.class);

                CustomActivityOnCrash.restartApplicationWithIntent(this, restartIntent
                        , crashReportConfig);
                break;
            }

            default:
                break;
        }
    }
}
