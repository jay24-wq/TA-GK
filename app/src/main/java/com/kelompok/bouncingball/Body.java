package com.kelompok.bouncingball;

/** Data gerak sederhana; tidak bergantung pada Android atau OpenGL. */
public class Body {
    public float x;
    public float y;
    public float z;
    public float velocityX;
    public float velocityY;
    public float velocityZ;
    public final float radius;

    public Body(float x, float y, float z, float radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }
}
