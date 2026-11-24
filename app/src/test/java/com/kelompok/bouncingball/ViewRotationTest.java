package com.kelompok.bouncingball;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ViewRotationTest {
    @Test
    public void dragChangesYawAndPitchByPixelScale() {
        ViewRotation rotation = new ViewRotation();

        rotation.rotateBy(10f, -6f);

        assertEquals(10f * ViewRotation.DEGREES_PER_PIXEL, rotation.getYawDegrees(), 0.0001f);
        assertEquals(-6f * ViewRotation.DEGREES_PER_PIXEL, rotation.getPitchDegrees(), 0.0001f);
    }

    @Test
    public void pitchIsClampedAtBothLimits() {
        ViewRotation rotation = new ViewRotation();

        rotation.rotateBy(0f, 1000f);
        assertEquals(ViewRotation.MAX_PITCH_DEGREES, rotation.getPitchDegrees(), 0f);

        rotation.rotateBy(0f, -1000f);
        assertEquals(ViewRotation.MIN_PITCH_DEGREES, rotation.getPitchDegrees(), 0f);
    }

    @Test
    public void yawCanPassThreeHundredSixtyDegrees() {
        ViewRotation rotation = new ViewRotation();

        rotation.rotateBy(800f, 0f);

        assertEquals(800f * ViewRotation.DEGREES_PER_PIXEL, rotation.getYawDegrees(), 0.0001f);
    }
}
