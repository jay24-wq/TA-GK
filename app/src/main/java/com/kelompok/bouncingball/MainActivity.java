package com.kelompok.bouncingball;

import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends android.app.Activity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameView = findViewById(R.id.game_view);
        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(view -> {
            gameView.startGame();
            startButton.setEnabled(false);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.onResume();
        }
    }
}
