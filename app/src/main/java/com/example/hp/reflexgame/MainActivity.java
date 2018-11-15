package com.example.hp.reflexgame;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.example.hp.reflexgame.view.ReflexView;

public class MainActivity extends AppCompatActivity {

    private ReflexView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout layout = findViewById(R.id.relativeLayout);
        gameView = new ReflexView(getApplicationContext(),getPreferences(Context.MODE_PRIVATE),layout);
       //gameView = new ReflexView(getApplicationContext(),PreferenceManager.getDefaultSharedPreferences(this),layout);

        layout.addView(gameView, 0);
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameView.resume(this);
    }
}
