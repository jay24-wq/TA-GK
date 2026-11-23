package com.kelompok.bouncingball;

/** Data gerak sederhana; tidak bergantung pada Android atau OpenGL. */
public class Body {
    public enum Type { BALL, PYRAMID }

    public float x;
    public float y;
    public float z;
    public float velocityX;
    public float velocityY;
    public float velocityZ;
    public final float radius;
    public final float mass;
    public final Type type;
    public boolean active = true;

    public Body(float x, float y, float z, float radius, Type type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.mass = 1f;
        this.type = type;
    }
}
