package com.kelompok.bouncingball;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {
    private GameWorld world;
    private final SphereMesh sphere = new SphereMesh(12, 18);
    private final PyramidMesh pyramid = new PyramidMesh();
    private final ContainerMesh container = new ContainerMesh();
    private long previousFrameNanos;
    private GameListener listener;
    private int lastBallScore = -1;
    private int lastPyramidScore = -1;
    private boolean finishNotified;

    public void initializeWorld() {
        if (world == null) {
            world = new GameWorld();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.03f, 0.08f, 0.14f, 1f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glFrontFace(GL10.GL_CCW);
        previousFrameNanos = System.nanoTime();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        int safeHeight = Math.max(height, 1);
        gl.glViewport(0, 0, width, safeHeight);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45f, (float) width / safeHeight, 0.1f, 100f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        long now = System.nanoTime();
        float dt = Math.min((now - previousFrameNanos) / 1_000_000_000f, 0.033f);
        previousFrameNanos = now;

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        GLU.gluLookAt(gl, 3.8f, 3.2f, 5.8f, 0f, 0f, 0f, 0f, 1f, 0f);
        if (world == null) {
            container.draw(gl);
            return;
        }
        world.update(dt);
        notifyListener();

        for (Body body : world.getBodies()) {
            if (!body.active) {
                continue;
            }
            gl.glPushMatrix();
            gl.glTranslatef(body.x, body.y, body.z);
            gl.glScalef(body.radius, body.radius, body.radius);
            if (body.type == Body.Type.BALL) {
                gl.glColor4f(0.2f, 0.78f, 1f, 1f);
                sphere.draw(gl);
            } else {
                gl.glColor4f(1f, 0.55f, 0.18f, 1f);
                pyramid.draw(gl);
            }
            gl.glPopMatrix();
        }

        container.draw(gl);
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
        lastBallScore = -1;
        lastPyramidScore = -1;
    }

    public void resetNotifications() {
        lastBallScore = -1;
        lastPyramidScore = -1;
        finishNotified = false;
    }

    public GameWorld getWorld() {
        return world;
    }

    private void notifyListener() {
        if (listener == null) {
            return;
        }
        int ballScore = world.getBallScore();
        int pyramidScore = world.getPyramidScore();
        if (ballScore != lastBallScore || pyramidScore != lastPyramidScore) {
            lastBallScore = ballScore;
            lastPyramidScore = pyramidScore;
            listener.onScoreChanged(ballScore, pyramidScore);
        }
        if (world.getStatus() == GameStatus.FINISHED && !finishNotified) {
            finishNotified = true;
            listener.onGameFinished();
        }
    }
}
