package com.kelompok.bouncingball;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GameView extends GLSurfaceView {
    public GameView(Context context, AttributeSet attributes) {
        super(context, attributes);
        setRenderer(new GameRenderer(new GameWorld()));
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
}
