package com.kelompok.bouncingball;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {
    // Sudut vertikal memberi perspektif moderat untuk adegan 3D.
    private static final float VERTICAL_FIELD_OF_VIEW_DEGREES = 45f;
    // Radius bola yang membatasi kubus 2 x 2 x 2 di semua orientasi.
    private static final float CONTAINER_BOUNDING_RADIUS = (float) Math.sqrt(3f);
    // Jarak ekstra memberi ruang piksel agar rusuk tidak tepat menempel tepi.
    private static final float CAMERA_FIT_MARGIN = 1.05f;
    // Arah pandang lama dipertahankan; hanya jaraknya disesuaikan dengan layar.
    private static final float CAMERA_DIRECTION_X = 3.8f;
    private static final float CAMERA_DIRECTION_Y = 3.2f;
    private static final float CAMERA_DIRECTION_Z = 5.8f;
    private static final float CAMERA_DIRECTION_LENGTH = (float) Math.sqrt(
            CAMERA_DIRECTION_X * CAMERA_DIRECTION_X
                    + CAMERA_DIRECTION_Y * CAMERA_DIRECTION_Y
                    + CAMERA_DIRECTION_Z * CAMERA_DIRECTION_Z);
    // Ambient menjaga sisi gelap tetap berwarna; diffuse memberi arah cahaya utama.
    private static final float[] LIGHT_AMBIENT = {0.24f, 0.24f, 0.28f, 1f};
    private static final float[] LIGHT_DIFFUSE = {0.9f, 0.9f, 0.85f, 1f};
    // w=1 berarti sumber cahaya berada pada posisi, bukan arah tak berhingga.
    private static final float[] LIGHT_POSITION = {4f, 6f, 5f, 1f};
    // Alpha turun per umur supaya enam salinan membentuk ekor yang memudar.
    private static final float[] TRAIL_ALPHAS = {0.40f, 0.32f, 0.24f, 0.16f, 0.10f, 0.05f};
    // Skala salinan terbaru sampai yang terlama, agar ekor tampak menyusut.
    private static final float TRAIL_NEWEST_SCALE = 0.9f;
    private static final float TRAIL_OLDEST_SCALE = 0.4f;

    private GameWorld world;
    private final ViewRotation viewRotation = new ViewRotation();
    private final SphereMesh sphere = new SphereMesh(12, 18);
    private final PyramidMesh pyramid = new PyramidMesh();
    private final ContainerMesh container = new ContainerMesh();
    private long previousFrameNanos;
    private GameListener listener;
    private int lastBallScore = -1;
    private int lastPyramidScore = -1;
    private boolean finishNotified;
    private float cameraDistance;

    public void initializeWorld() {
        if (world == null) {
            world = new GameWorld();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_CULL_FACE);
        gl.glFrontFace(GL10.GL_CCW);
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);
        // Default GL_COLOR_MATERIAL pada GL10 melacak ambient+diffuse untuk kedua sisi.
        gl.glEnable(GL10.GL_COLOR_MATERIAL);
        gl.glEnable(GL10.GL_NORMALIZE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, LIGHT_AMBIENT, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, LIGHT_DIFFUSE, 0);
        previousFrameNanos = System.nanoTime();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        int safeWidth = Math.max(width, 1);
        int safeHeight = Math.max(height, 1);
        float aspectRatio = (float) safeWidth / safeHeight;
        gl.glViewport(0, 0, safeWidth, safeHeight);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, VERTICAL_FIELD_OF_VIEW_DEGREES, aspectRatio, 0.1f, 100f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        float verticalHalfFov = (float) Math.toRadians(VERTICAL_FIELD_OF_VIEW_DEGREES / 2f);
        float horizontalHalfFov = (float) Math.atan(Math.tan(verticalHalfFov) * aspectRatio);
        float limitingHalfFov = Math.min(verticalHalfFov, horizontalHalfFov);
        cameraDistance = CONTAINER_BOUNDING_RADIUS * CAMERA_FIT_MARGIN
                / (float) Math.sin(limitingHalfFov);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        long now = System.nanoTime();
        float dt = Math.min((now - previousFrameNanos) / 1_000_000_000f, 0.033f);
        previousFrameNanos = now;

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        // Posisi diatur dalam koordinat layar sebelum kamera dan rotasi model-view.
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, LIGHT_POSITION, 0);
        float cameraScale = cameraDistance / CAMERA_DIRECTION_LENGTH;
        GLU.gluLookAt(gl, CAMERA_DIRECTION_X * cameraScale, CAMERA_DIRECTION_Y * cameraScale,
                CAMERA_DIRECTION_Z * cameraScale, 0f, 0f, 0f, 0f, 1f, 0f);
        gl.glRotatef(viewRotation.getPitchDegrees(), 1f, 0f, 0f);
        gl.glRotatef(viewRotation.getYawDegrees(), 0f, 1f, 0f);
        if (world == null) {
            drawContainerUnlit(gl);
            return;
        }
        world.update(dt);
        notifyListener();

        List<Body> bodies = world.getBodies();
        for (int index = 0; index < bodies.size(); index++) {
            Body body = bodies.get(index);
            if (body.state == Body.State.REMOVED) {
                continue;
            }
            gl.glPushMatrix();
            gl.glTranslatef(body.x, body.y, body.z);
            float renderRadius = body.radius * body.visualScale;
            gl.glScalef(renderRadius, renderRadius, renderRadius);
            if (body.type == Body.Type.BALL) {
                gl.glColor4f(0.2f, 0.78f, 1f, 1f);
                sphere.draw(gl);
            } else {
                gl.glColor4f(1f, 0.55f, 0.18f, 1f);
                pyramid.draw(gl);
            }
            gl.glPopMatrix();
        }

        drawTrails(gl, bodies);
        drawContainerUnlit(gl);
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

    public void rotateViewBy(float deltaXPixel, float deltaYPixel) {
        viewRotation.rotateBy(deltaXPixel, deltaYPixel);
    }

    public GameWorld getWorld() {
        return world;
    }

    private void drawContainerUnlit(GL10 gl) {
        gl.glDisable(GL10.GL_LIGHTING);
        container.draw(gl);
        gl.glEnable(GL10.GL_LIGHTING);
    }

    private void drawTrails(GL10 gl, List<Body> bodies) {
        if (!GameWorld.TRAIL_ENABLED) {
            return;
        }
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDepthMask(false);
        for (int index = 0; index < bodies.size(); index++) {
            Body body = bodies.get(index);
            if (!body.active || body.state != Body.State.ACTIVE) {
                continue;
            }
            int trailSize = body.trail.size();
            for (int age = 0; age < trailSize; age++) {
                // Ghost tidak ikut cahaya agar warna dan alpha konsisten saat wadah diputar.
                gl.glDisable(GL10.GL_LIGHTING);
                float ageRatio = (float) age / (GameWorld.TRAIL_LENGTH - 1);
                float scale = TRAIL_NEWEST_SCALE
                        + (TRAIL_OLDEST_SCALE - TRAIL_NEWEST_SCALE) * ageRatio;
                float radius = body.radius * scale;
                gl.glPushMatrix();
                gl.glTranslatef(body.trail.getX(age), body.trail.getY(age), body.trail.getZ(age));
                gl.glScalef(radius, radius, radius);
                float alpha = TRAIL_ALPHAS[age];
                if (body.type == Body.Type.BALL) {
                    gl.glColor4f(0.2f, 0.78f, 1f, alpha);
                    sphere.draw(gl);
                } else {
                    gl.glColor4f(1f, 0.55f, 0.18f, alpha);
                    pyramid.draw(gl);
                }
                gl.glPopMatrix();
                gl.glEnable(GL10.GL_LIGHTING);
            }
        }
        gl.glDepthMask(true);
        gl.glDisable(GL10.GL_BLEND);
        gl.glEnable(GL10.GL_LIGHTING);
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
