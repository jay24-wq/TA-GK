package com.kelompok.bouncingball;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {
    private final GameWorld world;
    private final SphereMesh sphere = new SphereMesh(12, 18);
    private final ContainerMesh container = new ContainerMesh();
    private long previousFrameNanos;

    public GameRenderer(GameWorld world) {
        this.world = world;
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
        world.update(dt);

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        GLU.gluLookAt(gl, 3.8f, 3.2f, 5.8f, 0f, 0f, 0f, 0f, 1f, 0f);

        for (Body body : world.getBodies()) {
            if (!body.active) {
                continue;
            }
            gl.glPushMatrix();
            gl.glTranslatef(body.x, body.y, body.z);
            gl.glScalef(body.radius, body.radius, body.radius);
            gl.glColor4f(0.2f, 0.78f, 1f, 1f);
            sphere.draw(gl);
            gl.glPopMatrix();
        }

        container.draw(gl);
    }

    public void startGame() {
        world.start();
    }
}
