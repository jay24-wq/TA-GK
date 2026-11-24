package com.kelompok.bouncingball;

/** Ring buffer posisi dunia; operasi baca dan tulis tidak membuat objek baru. */
public final class TrailBuffer {
    private final float[] x;
    private final float[] y;
    private final float[] z;
    private int head = -1;
    private int size;

    public TrailBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity harus positif");
        }
        x = new float[capacity];
        y = new float[capacity];
        z = new float[capacity];
    }

    public void record(float positionX, float positionY, float positionZ) {
        head = (head + 1) % x.length;
        x[head] = positionX;
        y[head] = positionY;
        z[head] = positionZ;
        if (size < x.length) {
            size++;
        }
    }

    public void clear() {
        head = -1;
        size = 0;
    }

    public int size() {
        return size;
    }

    public float getX(int age) {
        return x[indexForAge(age)];
    }

    public float getY(int age) {
        return y[indexForAge(age)];
    }

    public float getZ(int age) {
        return z[indexForAge(age)];
    }

    private int indexForAge(int age) {
        if (age < 0 || age >= size) {
            throw new IndexOutOfBoundsException("age=" + age);
        }
        int index = head - age;
        return index < 0 ? index + x.length : index;
    }
}
