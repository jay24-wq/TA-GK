package com.kelompok.bouncingball;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameWorldVanishTest {
    private static final long SEED = 6406L;
    private static final float INITIAL_DT = 1f / 60f;

    @Test
    public void sameTypeCollisionScoresImmediatelyAndVanishingPairIsIgnored() {
        GameWorld world = collidingBallPair();
        Body first = world.getBodies().get(0);
        Body second = world.getBodies().get(1);

        assertEquals(1, world.getBallScore());
        assertEquals(Body.State.VANISHING, first.state);
        assertEquals(Body.State.VANISHING, second.state);
        assertFalse(first.active);
        assertFalse(second.active);
        assertEquals(1f, first.visualScale, 0f);
        assertEquals(GameStatus.PLAYING, world.getStatus());

        world.update(0.05f);

        assertEquals(1, world.getBallScore());
        assertEquals(Body.State.VANISHING, first.state);
        assertEquals(Body.State.VANISHING, second.state);
    }

    @Test
    public void visualScaleDecreasesUntilPairIsRemoved() {
        GameWorld world = collidingBallPair();
        Body body = world.getBodies().get(0);
        float previousScale = body.visualScale;

        for (int step = 0; step < 10 && body.state == Body.State.VANISHING; step++) {
            world.update(0.05f);
            assertTrue(body.visualScale <= previousScale);
            previousScale = body.visualScale;
        }

        assertEquals(Body.State.REMOVED, body.state);
        assertEquals(0f, body.visualScale, 0f);
    }

    @Test
    public void gameFinishesOnlyAfterLastVanishAnimationCompletes() {
        GameWorld world = collidingBallPair();
        Body body = world.getBodies().get(0);
        assertEquals(GameStatus.PLAYING, world.getStatus());

        world.update(GameWorld.VANISH_DURATION - 0.01f);
        assertEquals(Body.State.VANISHING, body.state);
        assertEquals(GameStatus.PLAYING, world.getStatus());

        world.update(0.02f);
        assertEquals(Body.State.REMOVED, body.state);
        assertEquals(GameStatus.FINISHED, world.getStatus());
    }

    private static GameWorld collidingBallPair() {
        GameWorld world = new GameWorld(SEED);
        Body first = world.getBodies().get(0);
        Body second = world.getBodies().get(1);
        first.x = second.x = 0f;
        first.z = second.z = 0f;
        world.start();
        first.velocityX = second.velocityX = 0f;
        first.velocityZ = second.velocityZ = 0f;
        world.update(INITIAL_DT);
        return world;
    }
}
