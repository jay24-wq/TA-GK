package com.kelompok.bouncingball;

/** Simulasi fisika murni agar mudah dijelaskan dan diuji tanpa Android. */
public class GameWorld {
    private static final float GRAVITY_Y = -4.0f;
    private static final float RESTITUTION = 0.94f;
    private final Body ball = new Body(0f, 0f, 0f, 0.18f);

    public void update(float dt) {
        ball.velocityY += GRAVITY_Y * dt;
        ball.x += ball.velocityX * dt;
        ball.y += ball.velocityY * dt;
        ball.z += ball.velocityZ * dt;
        bounceAtWalls(ball);
    }

    public Body getBall() {
        return ball;
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
}
