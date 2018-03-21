package com.example.maxim.tetris;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Pause extends Activity{
    public final static int RESUME = 0;
    public final static int RESTART = 1;
    public final static int MENU = 2;

    private Button resume;
    private Button restart;
    private Button menuMain;
    public static int REQ_CODE = 1;
    public static String EXTRA_CHOICE = "choice";
    public int chosenAction;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chosenAction = RESUME;//by default in case user uses resume button
        setContentView(R.layout.activity_pause);
        menuMain = (Button) findViewById(R.id.menu_btn);
        menuMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMenuClick();
            }
        });

        restart = (Button) findViewById(R.id.restart_btn);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRestartClick() ;
            }
        });

        resume = (Button) findViewById(R.id.resume_btn);
        resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResumeClick();
            }
        });
        intent = new Intent();
        intent.putExtra(EXTRA_CHOICE, chosenAction);
        setResult(RESULT_OK, intent);
    }

    public void onResumeClick() {
        chosenAction = RESUME;
        intent.putExtra(EXTRA_CHOICE, chosenAction);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onMenuClick() {
        chosenAction = MENU;
        intent.putExtra(EXTRA_CHOICE, chosenAction);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onRestartClick() {
        chosenAction = RESTART;
        intent.putExtra(EXTRA_CHOICE, chosenAction);
        setResult(RESULT_OK, intent);
        finish();
    }

}
