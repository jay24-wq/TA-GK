package com.kelompok.bouncingball;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GameView extends GLSurfaceView {
    private static final int INVALID_POINTER_ID = -1;

    private final GameRenderer renderer;
    private int activePointerId = INVALID_POINTER_ID;
    private float lastTouchX;
    private float lastTouchY;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(0);
                lastTouchX = event.getX(0);
                lastTouchY = event.getY(0);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (activePointerId != INVALID_POINTER_ID) {
                    int pointerIndex = event.findPointerIndex(activePointerId);
                    if (pointerIndex >= 0) {
                        float x = event.getX(pointerIndex);
                        float y = event.getY(pointerIndex);
                        float deltaX = x - lastTouchX;
                        float deltaY = y - lastTouchY;
                        lastTouchX = x;
                        lastTouchY = y;
                        if (deltaX != 0f || deltaY != 0f) {
                            // ViewRotation hanya diubah di thread GL, sama seperti status game.
                            queueEvent(() -> renderer.rotateViewBy(deltaX, deltaY));
                        }
                    }
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerId(event.getActionIndex()) == activePointerId) {
                    activePointerId = INVALID_POINTER_ID;
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointerId = INVALID_POINTER_ID;
                return true;
            default:
                return true;
        }
    }
}
