package com.kelompok.bouncingball;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/** Bola tersusun dari strip titik hasil perhitungan garis lintang dan bujur. */
public class SphereMesh {
    private final FloatBuffer vertices;
    private final FloatBuffer normals;
    private final int slices;
    private final int stacks;

    public SphereMesh(int stacks, int slices) {
        this.stacks = stacks;
        this.slices = slices;
        float[] points = new float[stacks * (slices + 1) * 2 * 3];
        float[] normalValues = new float[points.length];
        int index = 0;
        for (int row = 0; row < stacks; row++) {
            for (int column = 0; column <= slices; column++) {
                double angle = 2.0 * Math.PI * column / slices;
                index = addPoint(points, normalValues, index, row, angle);
                index = addPoint(points, normalValues, index, row + 1, angle);
            }
        }
        ByteBuffer bytes = ByteBuffer.allocateDirect(points.length * 4);
        bytes.order(ByteOrder.nativeOrder());
        vertices = bytes.asFloatBuffer();
        vertices.put(points).position(0);
        ByteBuffer normalBytes = ByteBuffer.allocateDirect(normalValues.length * 4);
        normalBytes.order(ByteOrder.nativeOrder());
        normals = normalBytes.asFloatBuffer();
        normals.put(normalValues).position(0);
    }

    private int addPoint(float[] points, float[] normalValues, int index, int row, double angle) {
        double latitude = Math.PI * row / stacks;
        float x = (float) (Math.sin(latitude) * Math.cos(angle));
        float y = (float) Math.cos(latitude);
        float z = (float) (Math.sin(latitude) * Math.sin(angle));
        float inverseLength = 1f / (float) Math.sqrt(x * x + y * y + z * z);
        points[index] = x;
        normalValues[index++] = x * inverseLength;
        points[index] = y;
        normalValues[index++] = y * inverseLength;
        points[index] = z;
        normalValues[index++] = z * inverseLength;
        return index;
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, normals);
        int stripSize = (slices + 1) * 2;
        for (int row = 0; row < stacks; row++) {
            vertices.position(row * stripSize * 3);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, stripSize);
        }
        vertices.position(0);
        normals.position(0);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
