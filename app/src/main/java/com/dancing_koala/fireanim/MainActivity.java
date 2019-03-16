package com.dancing_koala.fireanim;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private FireView mFireView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFireView = findViewById(R.id.fireview);

        findViewById(R.id.btn_anim_start).setOnClickListener(v -> mFireView.play());
        findViewById(R.id.btn_anim_stop).setOnClickListener(v -> mFireView.kill());
        findViewById(R.id.btn_anim_reset).setOnClickListener(v -> mFireView.reset());

        findViewById(R.id.btn_wind_left).setOnClickListener(v -> mFireView.wind(-1));
        findViewById(R.id.btn_wind_right).setOnClickListener(v -> mFireView.wind(1));
    }
}
