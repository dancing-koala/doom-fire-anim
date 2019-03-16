package com.dancing_koala.fireanim;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private FireView mFireView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFireView = findViewById(R.id.fireview);
        mFireView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFireView.play();
            }
        });

        mFireView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mFireView.reset();
                return true;
            }
        });

        findViewById(R.id.btn_wind_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFireView.wind(-1);
            }
        });

        findViewById(R.id.btn_wind_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFireView.wind(1);
            }
        });
    }
}
