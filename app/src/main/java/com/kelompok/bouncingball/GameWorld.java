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
            if (!body.active) {
                continue;
            }
            body.velocityY += GRAVITY_Y * dt;
            body.x += body.velocityX * dt;
            body.y += body.velocityY * dt;
            body.z += body.velocityZ * dt;
            bounceAtWalls(body);
        }
        checkCollisions();
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

    private void checkCollisions() {
        for (int firstIndex = 0; firstIndex < bodies.size(); firstIndex++) {
            Body first = bodies.get(firstIndex);
            if (!first.active) {
                continue;
            }
            for (int secondIndex = firstIndex + 1; secondIndex < bodies.size(); secondIndex++) {
                Body second = bodies.get(secondIndex);
                if (!second.active) {
                    continue;
                }
                float dx = second.x - first.x;
                float dy = second.y - first.y;
                float dz = second.z - first.z;
                float radiusSum = first.radius + second.radius;
                float distanceSquared = dx * dx + dy * dy + dz * dz;
                if (distanceSquared >= radiusSum * radiusSum) {
                    continue;
                }
                if (first.type == second.type) {
                    // Pasangan dengan bentuk sama langsung hilang sebagai satu skor.
                    first.active = false;
                    second.active = false;
                    break;
                } else {
                    bounceApart(first, second, dx, dy, dz, distanceSquared, radiusSum);
                }
            }
        }
    }

    private void bounceApart(Body first, Body second, float dx, float dy, float dz,
                             float distanceSquared, float radiusSum) {
        float distance = (float) Math.sqrt(distanceSquared);
        float normalX = distance > 0f ? dx / distance : 1f;
        float normalY = distance > 0f ? dy / distance : 0f;
        float normalZ = distance > 0f ? dz / distance : 0f;
        float inverseFirstMass = 1f / first.mass;
        float inverseSecondMass = 1f / second.mass;
        float inverseMassSum = inverseFirstMass + inverseSecondMass;

        float relativeSpeed = (second.velocityX - first.velocityX) * normalX
                + (second.velocityY - first.velocityY) * normalY
                + (second.velocityZ - first.velocityZ) * normalZ;
        if (relativeSpeed < 0f) {
            float impulse = -(1f + RESTITUTION) * relativeSpeed / inverseMassSum;
            first.velocityX -= impulse * inverseFirstMass * normalX;
            first.velocityY -= impulse * inverseFirstMass * normalY;
            first.velocityZ -= impulse * inverseFirstMass * normalZ;
            second.velocityX += impulse * inverseSecondMass * normalX;
            second.velocityY += impulse * inverseSecondMass * normalY;
            second.velocityZ += impulse * inverseSecondMass * normalZ;
        }

        float overlap = radiusSum - distance;
        float correction = Math.max(overlap - 0.001f, 0f) * 0.8f / inverseMassSum;
        first.x -= correction * inverseFirstMass * normalX;
        first.y -= correction * inverseFirstMass * normalY;
        first.z -= correction * inverseFirstMass * normalZ;
        second.x += correction * inverseSecondMass * normalX;
        second.y += correction * inverseSecondMass * normalY;
        second.z += correction * inverseSecondMass * normalZ;
    }
}
