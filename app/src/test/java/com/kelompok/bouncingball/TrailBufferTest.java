package com.kelompok.bouncingball;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrailBufferTest {
    @Test
    public void recordReturnsNewestToOldestAndFullBufferOverwritesOldest() {
        TrailBuffer trail = new TrailBuffer(3);
        for (int position = 1; position <= 3; position++) {
            trail.record(position, position * 10f, position * 100f);
            assertEquals(Math.min(position, 3), trail.size());
            assertTrue(trail.size() <= 3);
        }

        trail.record(4f, 40f, 400f);

        assertEquals(3, trail.size());
        assertPosition(trail, 0, 4f);
        assertPosition(trail, 1, 3f);
        assertPosition(trail, 2, 2f);
    }

    @Test
    public void clearEmptiesBuffer() {
        TrailBuffer trail = new TrailBuffer(2);
        trail.record(1f, 2f, 3f);
        trail.clear();

        assertEquals(0, trail.size());
    }

    private static void assertPosition(TrailBuffer trail, int age, float expected) {
        assertEquals(expected, trail.getX(age), 0f);
        assertEquals(expected * 10f, trail.getY(age), 0f);
        assertEquals(expected * 100f, trail.getZ(age), 0f);
    }
}
