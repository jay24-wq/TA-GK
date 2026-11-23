package com.kelompok.bouncingball;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GameView extends GLSurfaceView {
    private final GameRenderer renderer;

    public GameView(Context context, AttributeSet attributes) {
        super(context, attributes);
        renderer = new GameRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        // World dibuat di thread GL agar sejak awal hanya thread itu yang mengubahnya.
        queueEvent(renderer::initializeWorld);
    }

    public void startGame() {
        // Simulasi hanya diubah pada thread GL, sama seperti update per frame.
        queueEvent(() -> renderer.getWorld().start());
    }

    public void addPair(Body.Type type) {
        queueEvent(() -> renderer.getWorld().addPair(type));
    }

    public void resetGame() {
        queueEvent(() -> {
            renderer.getWorld().reset();
            renderer.resetNotifications();
        });
    }

    public void setGameListener(GameListener listener) {
        queueEvent(() -> renderer.setListener(listener));
    }
}
