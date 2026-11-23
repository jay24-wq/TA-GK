package com.kelompok.bouncingball;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/** Sisi tipis transparan digambar sebelum rusuk agar wadah tetap terbaca. */
public class ContainerMesh {
    private final FloatBuffer vertices;
    private final ByteBuffer faces;
    private final ByteBuffer edges;

    public ContainerMesh() {
        vertices = floatBuffer(new float[] {
                -1, -1, -1,  1, -1, -1,  1,  1, -1, -1,  1, -1,
                -1, -1,  1,  1, -1,  1,  1,  1,  1, -1,  1,  1
        });
        faces = byteBuffer(new byte[] {
                0, 1, 2, 0, 2, 3, 4, 6, 5, 4, 7, 6,
                0, 4, 5, 0, 5, 1, 3, 2, 6, 3, 6, 7,
                0, 3, 7, 0, 7, 4, 1, 5, 6, 1, 6, 2
        });
        edges = byteBuffer(new byte[] {
                0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4,
                0, 4, 1, 5, 2, 6, 3, 7
        });
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDepthMask(false);
        gl.glColor4f(0.12f, 0.48f, 0.72f, 0.08f);
        gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, faces);
        gl.glLineWidth(2f);
        gl.glColor4f(0.35f, 0.78f, 1f, 0.8f);
        gl.glDrawElements(GL10.GL_LINES, 24, GL10.GL_UNSIGNED_BYTE, edges);
        gl.glDepthMask(true);
        gl.glDisable(GL10.GL_BLEND);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    private static FloatBuffer floatBuffer(float[] values) {
        ByteBuffer bytes = ByteBuffer.allocateDirect(values.length * 4);
        bytes.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bytes.asFloatBuffer();
        buffer.put(values).position(0);
        return buffer;
    }

    private static ByteBuffer byteBuffer(byte[] values) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(values.length);
        buffer.put(values).position(0);
        return buffer;
    }
}
