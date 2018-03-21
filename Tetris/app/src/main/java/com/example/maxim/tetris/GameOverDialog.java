package com.example.maxim.tetris;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Maxim on 8/9/2016.
 */
public class GameOverDialog extends Dialog {

    public static int RESTART = 0;
    public static int MENU = 1;

    private Button restart;
    private Button menu;
    public int choice;

    public GameOverDialog(Activity a) {
        super(a);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over_dialog);

        choice = -1;

        restart = (Button) findViewById(R.id.restart);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice = RESTART;
                dismiss();
            }
        });

        menu = (Button) findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice = MENU;
                dismiss();
            }
        });
    }

}
