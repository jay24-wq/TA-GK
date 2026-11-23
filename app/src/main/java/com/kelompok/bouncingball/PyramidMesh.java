package com.kelompok.bouncingball;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/** Limas persegi dibuat dari empat titik alas dan satu titik puncak. */
public class PyramidMesh {
    private final FloatBuffer vertices;
    private final ByteBuffer triangles;

    public PyramidMesh() {
        float[] points = {
                -0.55f, -0.45f, -0.55f,
                 0.55f, -0.45f, -0.55f,
                 0.55f, -0.45f,  0.55f,
                -0.55f, -0.45f,  0.55f,
                 0f,     0.9f,   0f
        };
        byte[] indices = {
                0, 2, 1, 0, 3, 2,
                0, 4, 1, 1, 4, 2, 2, 4, 3, 3, 4, 0
        };
        ByteBuffer vertexBytes = ByteBuffer.allocateDirect(points.length * 4);
        vertexBytes.order(ByteOrder.nativeOrder());
        vertices = vertexBytes.asFloatBuffer();
        vertices.put(points).position(0);
        triangles = ByteBuffer.allocateDirect(indices.length);
        triangles.put(indices).position(0);
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
        gl.glDrawElements(GL10.GL_TRIANGLES, 18, GL10.GL_UNSIGNED_BYTE, triangles);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
