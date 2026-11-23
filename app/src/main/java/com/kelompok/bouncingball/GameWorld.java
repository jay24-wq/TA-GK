package com.kelompok.bouncingball;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Simulasi fisika murni agar mudah dijelaskan dan diuji tanpa Android. */
public class GameWorld {
    private static final float GRAVITY_Y = -4.0f;
    private static final float RESTITUTION = 0.94f;
    private final Random random;
    private final List<Body> bodies = new ArrayList<>();
    private GameStatus status;

    public GameWorld() {
        this(System.nanoTime());
    }

    public GameWorld(long seed) {
        random = new Random(seed);
        reset();
    }

    public void update(float dt) {
        if (status != GameStatus.PLAYING) {
            return;
        }
        for (Body body : bodies) {
            body.velocityY += GRAVITY_Y * dt;
            body.x += body.velocityX * dt;
            body.y += body.velocityY * dt;
            body.z += body.velocityZ * dt;
            bounceAtWalls(body);
        }
    }

    public void start() {
        if (status != GameStatus.READY) {
            return;
        }
        status = GameStatus.PLAYING;
        for (Body body : bodies) {
            body.velocityX = randomBetween(-0.7f, 0.7f);
            body.velocityZ = randomBetween(-0.7f, 0.7f);
        }
    }

    public void reset() {
        bodies.clear();
        bodies.add(new Body(-0.25f, 0f, 0f, 0.18f, Body.Type.BALL));
        bodies.add(new Body(0.25f, 0f, 0f, 0.18f, Body.Type.BALL));
        status = GameStatus.READY;
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public GameStatus getStatus() {
        return status;
    }

    private void bounceAtWalls(Body body) {
        float limit = 1f - body.radius;
        if (body.x < -limit) {
            body.x = -limit;
            body.velocityX = Math.abs(body.velocityX) * RESTITUTION;
        } else if (body.x > limit) {
            body.x = limit;
            body.velocityX = -Math.abs(body.velocityX) * RESTITUTION;
        }
        if (body.y < -limit) {
            body.y = -limit;
            body.velocityY = Math.abs(body.velocityY) * RESTITUTION;
            // Dorongan ringan menjaga bola tidak kehilangan seluruh gerak di lantai.
            if (body.velocityY < 0.7f) {
                body.velocityY = randomBetween(1.2f, 2.0f);
                body.velocityX += randomBetween(-0.25f, 0.25f);
                body.velocityZ += randomBetween(-0.25f, 0.25f);
            }
        } else if (body.y > limit) {
            body.y = limit;
            body.velocityY = -Math.abs(body.velocityY) * RESTITUTION;
        }
        if (body.z < -limit) {
            body.z = -limit;
            body.velocityZ = Math.abs(body.velocityZ) * RESTITUTION;
        } else if (body.z > limit) {
            body.z = limit;
            body.velocityZ = -Math.abs(body.velocityZ) * RESTITUTION;
        }
    }

    private float randomBetween(float minimum, float maximum) {
        return minimum + random.nextFloat() * (maximum - minimum);
    }
}
