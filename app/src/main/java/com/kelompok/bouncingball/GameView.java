package com.kelompok.bouncingball;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GameView extends GLSurfaceView {
    private final GameRenderer renderer;

    public GameView(Context context, AttributeSet attributes) {
        super(context, attributes);
        renderer = new GameRenderer(new GameWorld());
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void startGame() {
        // Simulasi hanya diubah pada thread GL, sama seperti update per frame.
        queueEvent(renderer::startGame);
    }
}
