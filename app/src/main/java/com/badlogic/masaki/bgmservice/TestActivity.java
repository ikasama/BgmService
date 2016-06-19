package com.badlogic.masaki.bgmservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.badlogic.masaki.bgmservice.library.BgmService;
import com.badlogic.masaki.bgmservice.library.BgmSettings;

/**
 * Created by shojimasaki on 2016/05/24.
 */
public class TestActivity extends AppCompatActivity {
    private static final String BGM_FILE_NAME = "bgm1.mp3";

    private static int sCount = 0;

    private Button mChangeMusicButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mChangeMusicButton = (Button) findViewById(R.id.button_change_music);
        mChangeMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SampleBgmApplication bgmApp = ((SampleBgmApplication) getApplication());
                BgmService service = bgmApp.getBgmService();
                if (service != null && bgmApp.isBoundToService()) {
                    BgmSettings.setFileName(BGM_FILE_NAME);
                    service.start(BgmSettings.getFileName());
                }
            }
        });

        if (sCount++ < 5) {
            startActivity(new Intent(this, TestActivity.class));
            if (sCount % 2 == 0) {
                finish();
            }
        }
    }
}
