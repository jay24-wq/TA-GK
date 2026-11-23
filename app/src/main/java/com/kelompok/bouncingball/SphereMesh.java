package com.kelompok.bouncingball;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/** Bola tersusun dari strip titik hasil perhitungan garis lintang dan bujur. */
public class SphereMesh {
    private final FloatBuffer vertices;
    private final int slices;
    private final int stacks;

    public SphereMesh(int stacks, int slices) {
        this.stacks = stacks;
        this.slices = slices;
        float[] points = new float[stacks * (slices + 1) * 2 * 3];
        int index = 0;
        for (int row = 0; row < stacks; row++) {
            for (int column = 0; column <= slices; column++) {
                double angle = 2.0 * Math.PI * column / slices;
                index = addPoint(points, index, row, angle);
                index = addPoint(points, index, row + 1, angle);
            }
        }
        ByteBuffer bytes = ByteBuffer.allocateDirect(points.length * 4);
        bytes.order(ByteOrder.nativeOrder());
        vertices = bytes.asFloatBuffer();
        vertices.put(points).position(0);
    }

    private int addPoint(float[] points, int index, int row, double angle) {
        double latitude = Math.PI * row / stacks;
        points[index++] = (float) (Math.sin(latitude) * Math.cos(angle));
        points[index++] = (float) Math.cos(latitude);
        points[index++] = (float) (Math.sin(latitude) * Math.sin(angle));
        return index;
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
        int stripSize = (slices + 1) * 2;
        for (int row = 0; row < stacks; row++) {
            vertices.position(row * stripSize * 3);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, stripSize);
        }
        vertices.position(0);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
