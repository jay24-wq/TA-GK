package com.kelompok.bouncingball;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameWorldTrailTest {
    private static final long SEED = 6526L;

    @Test
    public void oneSecondSamplingUsesTimeAndKeepsTrailAtCapacityForDifferentDt() {
        assertEquals(GameWorld.TRAIL_INTERVAL, sampleForOneSecond(1f / 60f), 0.005f);
        assertEquals(GameWorld.TRAIL_INTERVAL, sampleForOneSecond(1f / 30f), 0.005f);
    }

    @Test
    public void startAndResetClearOldTrailAndAccumulator() {
        GameWorld world = new GameWorld(SEED);
        Body oldBody = world.getBodies().get(0);
        oldBody.trail.record(0.2f, 0.3f, 0.4f);
        oldBody.trailTimeAccumulator = 0.02f;
        world.update(1f);
        assertEquals(1, oldBody.trail.size());

        world.start();

        assertEquals(0, oldBody.trail.size());
        assertEquals(0f, oldBody.trailTimeAccumulator, 0f);
        oldBody.trail.record(0.5f, 0.6f, 0.7f);
        oldBody.trailTimeAccumulator = 0.02f;
        world.reset();

        assertEquals(0, oldBody.trail.size());
        assertEquals(0f, oldBody.trailTimeAccumulator, 0f);
        assertEquals(0, world.getBodies().get(0).trail.size());
    }

    @Test
    public void twentySecondsKeepsCoordinatesAndRecordedPositionsFiniteAndInside() {
        GameWorld world = new GameWorld(SEED);
        assertTrue(world.addPair(Body.Type.PYRAMID));
        world.start();
        for (int step = 0; step < 20 * 60; step++) {
            world.update(1f / 60f);
            for (Body body : world.getBodies()) {
                float limit = 1f - body.radius;
                assertFiniteAndInside(body.x, limit);
                assertFiniteAndInside(body.y, limit);
                assertFiniteAndInside(body.z, limit);
                assertTrue(body.trail.size() <= GameWorld.TRAIL_LENGTH);
                for (int age = 0; age < body.trail.size(); age++) {
                    assertFiniteAndInside(body.trail.getX(age), limit);
                    assertFiniteAndInside(body.trail.getY(age), limit);
                    assertFiniteAndInside(body.trail.getZ(age), limit);
                }
            }
        }
    }

    private static float sampleForOneSecond(float dt) {
        GameWorld world = new GameWorld(SEED);
        world.getBodies().remove(1);
        Body body = world.getBodies().get(0);
        world.start();
        float limit = 1f - body.radius;
        body.hasLanded = true;
        body.x = -limit;
        body.y = -limit;
        body.z = -limit;
        float componentSpeed = GameWorld.MIN_SPEED / (float) Math.sqrt(3f);
        body.velocityX = componentSpeed;
        body.velocityY = componentSpeed;
        body.velocityZ = componentSpeed;

        int steps = Math.round(1f / dt);
        for (int step = 0; step < steps; step++) {
            world.update(dt);
        }

        assertEquals(GameWorld.TRAIL_LENGTH, body.trail.size());
        float estimatedIntervalSum = 0f;
        for (int age = 0; age < body.trail.size() - 1; age++) {
            float dx = body.trail.getX(age) - body.trail.getX(age + 1);
            float dy = body.trail.getY(age) - body.trail.getY(age + 1);
            float dz = body.trail.getZ(age) - body.trail.getZ(age + 1);
            float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            float estimatedInterval = distance / GameWorld.MIN_SPEED;
            // Posisi hanya bisa diambil pada frame berikutnya; toleransi satu frame.
            assertEquals(GameWorld.TRAIL_INTERVAL, estimatedInterval, dt);
            estimatedIntervalSum += estimatedInterval;
        }
        float averageInterval = estimatedIntervalSum / (body.trail.size() - 1);
        assertEquals(GameWorld.TRAIL_INTERVAL, averageInterval, 0.005f);
        return averageInterval;
    }

    private static void assertFiniteAndInside(float coordinate, float limit) {
        assertTrue("koordinat NaN atau tak hingga", !Float.isNaN(coordinate)
                && !Float.isInfinite(coordinate));
        assertTrue("koordinat keluar wadah", coordinate >= -limit - 0.0001f
                && coordinate <= limit + 0.0001f);
    }
}
