package com.kelompok.bouncingball;

/** Callback hasil permainan; Activity akan meneruskannya ke thread UI. */
public interface GameListener {
    void onScoreChanged(int bola, int limas);

    void onGameFinished();
}
