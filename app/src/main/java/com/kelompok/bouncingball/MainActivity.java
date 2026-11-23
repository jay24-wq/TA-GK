package com.kelompok.bouncingball;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends android.app.Activity implements GameListener {
    private GameView gameView;
    private TextView scoreText;
    private TextView statusText;
    private Button startButton;
    private Button addBallButton;
    private Button addPyramidButton;
    private Button retryButton;
    private int readyObjectCount = 2;
    private boolean canEditObjects = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameView = findViewById(R.id.game_view);
        scoreText = findViewById(R.id.score_text);
        statusText = findViewById(R.id.status_text);
        startButton = findViewById(R.id.start_button);
        addBallButton = findViewById(R.id.add_ball_button);
        addPyramidButton = findViewById(R.id.add_pyramid_button);
        retryButton = findViewById(R.id.retry_button);
        gameView.setGameListener(this);

        startButton.setOnClickListener(view -> {
            gameView.startGame();
            setReadyControlsEnabled(false);
        });
        addBallButton.setOnClickListener(view -> addPair(Body.Type.BALL));
        addPyramidButton.setOnClickListener(view -> addPair(Body.Type.PYRAMID));
        retryButton.setOnClickListener(view -> resetGame());
    }

    private void addPair(Body.Type type) {
        if (!canEditObjects || readyObjectCount + 2 > 12) {
            return;
        }
        gameView.addPair(type);
        readyObjectCount += 2;
        updateAddButtons();
    }

    private void resetGame() {
        gameView.resetGame();
        readyObjectCount = 2;
        scoreText.setText("Skor Bola = 0\nSkor Limas = 0");
        statusText.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        setReadyControlsEnabled(true);
    }

    private void setReadyControlsEnabled(boolean enabled) {
        canEditObjects = enabled;
        startButton.setEnabled(enabled);
        updateAddButtons();
    }

    private void updateAddButtons() {
        boolean canAdd = canEditObjects && readyObjectCount + 2 <= 12;
        addBallButton.setEnabled(canAdd);
        addPyramidButton.setEnabled(canAdd);
    }

    // Callback berasal dari thread GL, jadi TextView hanya disentuh di thread UI.
    @Override
    public void onScoreChanged(int bola, int limas) {
        runOnUiThread(() -> scoreText.setText("Skor Bola = " + bola + "\nSkor Limas = " + limas));
    }

    @Override
    public void onGameFinished() {
        runOnUiThread(() -> {
            statusText.setText("Selesai");
            statusText.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
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
