package com.kelompok.bouncingball;

/** Sudut tampilan untuk memutar adegan tanpa mengubah koordinat fisika. */
public final class ViewRotation {
    // Sepuluh piksel menghasilkan lima derajat, cukup responsif tanpa terlalu sensitif.
    static final float DEGREES_PER_PIXEL = 0.5f;
    // Menjaga pandangan tetap tegak, jauh dari posisi terbalik di sekitar 90 derajat.
    static final float MIN_PITCH_DEGREES = -85f;
    static final float MAX_PITCH_DEGREES = 85f;

    private float yawDegrees;
    private float pitchDegrees;

    public void rotateBy(float deltaXPixel, float deltaYPixel) {
        yawDegrees += deltaXPixel * DEGREES_PER_PIXEL;
        pitchDegrees = Math.max(MIN_PITCH_DEGREES,
                Math.min(MAX_PITCH_DEGREES, pitchDegrees + deltaYPixel * DEGREES_PER_PIXEL));
    }

    public float getYawDegrees() {
        return yawDegrees;
    }

    public float getPitchDegrees() {
        return pitchDegrees;
    }
}
