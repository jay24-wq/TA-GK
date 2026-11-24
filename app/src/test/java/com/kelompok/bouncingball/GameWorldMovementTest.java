package com.kelompok.bouncingball;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameWorldMovementTest {
    private static final long SEED = 20261124L;
    private static final float DT = 1f / 60f;
    private static final String[] WALLS = {"kiri", "kanan", "bawah", "atas", "belakang", "depan"};

    @Test
    public void startFallsStraightUntilLandingThenLaunchesUpward() {
        GameWorld world = new GameWorld(SEED);
        world.getBodies().remove(1);
        Body body = world.getBodies().get(0);
        world.start();
        assertFalse(body.hasLanded);
        assertEquals(0f, body.velocityY, 0f);
        assertTrue(Math.abs(body.velocityX) <= 0.1f);
        assertTrue(Math.abs(body.velocityZ) <= 0.1f);

        float previousY = body.y;
        for (int step = 0; step < 180 && !body.hasLanded; step++) {
            world.update(DT);
            if (!body.hasLanded) {
                assertTrue("y naik sebelum mendarat", body.y <= previousY);
                assertTrue("velocityY positif sebelum mendarat", body.velocityY <= 0f);
            }
            previousY = body.y;
        }
        assertTrue("belum mendarat dalam 3 detik", body.hasLanded);
        assertTrue(body.velocityY >= 0.4f * GameWorld.MIN_SPEED);
        assertSpeedInRange(body);
    }

    @Test
    public void everyBodyLandsWithinThreeSeconds() {
        GameWorld world = fourBodies();
        world.start();
        for (int step = 0; step < 180; step++) {
            world.update(DT);
        }
        for (Body body : world.getBodies()) {
            assertTrue(body.hasLanded);
        }
    }

    @Test
    public void landedBodyKeepsSpeedWithoutGravity() {
        GameWorld world = new GameWorld(SEED);
        world.getBodies().remove(1);
        Body body = world.getBodies().get(0);
        world.start();
        for (int step = 0; step < 180 && !body.hasLanded; step++) {
            world.update(DT);
        }
        assertTrue(body.hasLanded);
        for (int step = 0; step < 600; step++) {
            world.update(DT);
            assertSpeedInRange(body);
        }
    }

    @Test
    public void fourBodiesReachTheContainerWallsAndStayInside() {
        // Dua dunia masing-masing berisi satu bola dan satu limas, jadi
        // tumbukan sejenis tidak ikut memengaruhi pengujian pantulan dinding.
        GameWorld[] worlds = {
                oneBallAndPyramid(SEED, 0, 2),
                oneBallAndPyramid(SEED + 1, 1, 3)
        };
        int[][][] wallHits = new int[worlds.length][2][WALLS.length];
        int[][] lowYSteps = new int[worlds.length][2];
        int steps = 10 * 60;
        for (GameWorld world : worlds) world.start();

        for (int step = 0; step < steps; step++) {
            for (int worldIndex = 0; worldIndex < worlds.length; worldIndex++) {
                GameWorld world = worlds[worldIndex];
                world.update(DT);
                for (int index = 0; index < world.getBodies().size(); index++) {
                    Body body = world.getBodies().get(index);
                    assertTrue(body.active);
                    float limit = 1f - body.radius;
                    assertTrue(body.x >= -limit - 0.0001f && body.x <= limit + 0.0001f);
                    assertTrue(body.y >= -limit - 0.0001f && body.y <= limit + 0.0001f);
                    assertTrue(body.z >= -limit - 0.0001f && body.z <= limit + 0.0001f);
                    if (body.y < -0.5f) lowYSteps[worldIndex][index]++;
                    recordWallHit(body, limit, wallHits[worldIndex][index]);
                }
            }
        }

        for (int worldIndex = 0; worldIndex < worlds.length; worldIndex++) {
            for (int index = 0; index < 2; index++) {
                int count = 0;
                Set<String> hits = new HashSet<>();
                for (int wall = 0; wall < WALLS.length; wall++) {
                    if (wallHits[worldIndex][index][wall] > 0) {
                        count++;
                        hits.add(WALLS[wall]);
                    }
                }
                float lowPercent = 100f * lowYSteps[worldIndex][index] / steps;
                assertTrue("atap belum tersentuh", wallHits[worldIndex][index][3] > 0);
                assertTrue("lantai belum tersentuh", wallHits[worldIndex][index][2] > 0);
                assertTrue("kurang dari 5 dinding: " + hits, count >= 5);
                assertTrue("terlalu lama dekat lantai: " + lowPercent, lowPercent <= 40f);
            }
        }
    }

    @Test
    public void sameTypeCollisionScoresAndFinishesThenResetCanStartAgain() {
        GameWorld world = new GameWorld(SEED);
        Body first = world.getBodies().get(0);
        Body second = world.getBodies().get(1);
        first.x = second.x = 0f;
        first.z = second.z = 0f;
        world.start();
        first.velocityX = second.velocityX = 0f;
        first.velocityZ = second.velocityZ = 0f;
        world.update(DT);

        assertEquals(1, world.getBallScore());
        assertEquals(GameStatus.FINISHED, world.getStatus());
        world.reset();
        assertEquals(GameStatus.READY, world.getStatus());
        assertEquals(0, world.getBallScore());
        assertFalse(world.getBodies().get(0).hasLanded);
        world.start();
        assertEquals(GameStatus.PLAYING, world.getStatus());
    }

    @Test
    public void sameTypePyramidCollisionKeepsItsScoreRule() {
        GameWorld world = fourBodies();
        world.getBodies().get(0).active = false;
        world.getBodies().get(1).active = false;
        Body first = world.getBodies().get(2);
        Body second = world.getBodies().get(3);
        first.x = second.x = 0f;
        first.z = second.z = 0f;
        world.start();
        first.velocityX = second.velocityX = 0f;
        first.velocityZ = second.velocityZ = 0f;
        world.update(DT);

        assertEquals(1, world.getPyramidScore());
        assertEquals(GameStatus.FINISHED, world.getStatus());
    }

    @Test
    public void differentTypeCollisionKeepsLandedBodiesMoving() {
        GameWorld world = fourBodies();
        Body ball = world.getBodies().get(0);
        Body pyramid = world.getBodies().get(2);
        world.getBodies().get(1).active = false;
        world.getBodies().get(3).active = false;
        ball.x = -0.1f;
        pyramid.x = 0.1f;
        ball.y = pyramid.y = 0f;
        ball.z = pyramid.z = 0f;
        ball.hasLanded = pyramid.hasLanded = true;
        world.start();
        ball.hasLanded = pyramid.hasLanded = true;
        ball.velocityX = 1f;
        pyramid.velocityX = -1f;
        ball.velocityY = pyramid.velocityY = 0f;
        ball.velocityZ = pyramid.velocityZ = 0f;
        world.update(DT);

        assertTrue(ball.active);
        assertTrue(pyramid.active);
        assertEquals(0, world.getBallScore());
        assertEquals(0, world.getPyramidScore());
        assertSpeedInRange(ball);
        assertSpeedInRange(pyramid);
    }

    @Test
    public void addPairAndTwelveBodyLimitRemainUnchanged() {
        GameWorld world = new GameWorld(SEED);
        assertTrue(world.addPair(Body.Type.PYRAMID));
        for (int pair = 0; pair < 4; pair++) {
            assertTrue(world.addPair(Body.Type.BALL));
        }
        assertEquals(12, world.getBodies().size());
        assertFalse(world.addPair(Body.Type.BALL));
        world.start();
        assertFalse(world.addPair(Body.Type.PYRAMID));
    }

    private static GameWorld fourBodies() {
        GameWorld world = new GameWorld(SEED);
        assertTrue(world.addPair(Body.Type.PYRAMID));
        return world;
    }

    private static GameWorld oneBallAndPyramid(long seed, int ballIndex, int pyramidIndex) {
        GameWorld world = new GameWorld(seed);
        assertTrue(world.addPair(Body.Type.PYRAMID));
        Body ball = world.getBodies().get(ballIndex);
        Body pyramid = world.getBodies().get(pyramidIndex);
        world.getBodies().clear();
        world.getBodies().add(ball);
        world.getBodies().add(pyramid);
        return world;
    }

    private static void assertSpeedInRange(Body body) {
        float speed = (float) Math.sqrt(body.velocityX * body.velocityX
                + body.velocityY * body.velocityY + body.velocityZ * body.velocityZ);
        assertTrue("kecepatan di bawah minimum: " + speed, speed >= GameWorld.MIN_SPEED - 0.0001f);
        assertTrue("kecepatan di atas maksimum: " + speed, speed <= GameWorld.MAX_SPEED + 0.0001f);
    }

    private static void recordWallHit(Body body, float limit, int[] hits) {
        if (body.x <= -limit + 0.0001f) hits[0]++;
        if (body.x >= limit - 0.0001f) hits[1]++;
        if (body.y <= -limit + 0.0001f) hits[2]++;
        if (body.y >= limit - 0.0001f) hits[3]++;
        if (body.z <= -limit + 0.0001f) hits[4]++;
        if (body.z >= limit - 0.0001f) hits[5]++;
    }
}
