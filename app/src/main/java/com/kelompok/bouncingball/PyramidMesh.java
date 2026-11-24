package com.kelompok.bouncingball;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/** Limas persegi dibuat dari empat titik alas dan satu titik puncak. */
public class PyramidMesh {
    private final FloatBuffer vertices;
    private final FloatBuffer normals;

    private static final float[] POINTS = {
            -0.55f, -0.45f, -0.55f,
             0.55f, -0.45f, -0.55f,
             0.55f, -0.45f,  0.55f,
            -0.55f, -0.45f,  0.55f,
             0f,     0.9f,    0f
    };
    private static final int[] TRIANGLE_INDICES = {
            0, 2, 1, 0, 3, 2,
            0, 4, 1, 1, 4, 2, 2, 4, 3, 3, 4, 0
    };
    private static final int VERTEX_COUNT = TRIANGLE_INDICES.length;
    // Titik berat volume limas dipakai untuk memastikan normal mengarah keluar.
    private static final float CENTER_OF_VOLUME_Y = -0.1125f;

    public PyramidMesh() {
        float[] faceVertices = new float[VERTEX_COUNT * 3];
        float[] faceNormals = new float[faceVertices.length];
        for (int triangle = 0; triangle < TRIANGLE_INDICES.length; triangle += 3) {
            addTriangle(faceVertices, faceNormals, triangle,
                    TRIANGLE_INDICES[triangle], TRIANGLE_INDICES[triangle + 1],
                    TRIANGLE_INDICES[triangle + 2]);
        }

        ByteBuffer vertexBytes = ByteBuffer.allocateDirect(faceVertices.length * 4);
        vertexBytes.order(ByteOrder.nativeOrder());
        vertices = vertexBytes.asFloatBuffer();
        vertices.put(faceVertices).position(0);
        ByteBuffer normalBytes = ByteBuffer.allocateDirect(faceNormals.length * 4);
        normalBytes.order(ByteOrder.nativeOrder());
        normals = normalBytes.asFloatBuffer();
        normals.put(faceNormals).position(0);
    }

    private static void addTriangle(float[] faceVertices, float[] faceNormals, int outputIndex,
                                    int firstIndex, int secondIndex, int thirdIndex) {
        int first = firstIndex * 3;
        int second = secondIndex * 3;
        int third = thirdIndex * 3;
        float edgeOneX = POINTS[second] - POINTS[first];
        float edgeOneY = POINTS[second + 1] - POINTS[first + 1];
        float edgeOneZ = POINTS[second + 2] - POINTS[first + 2];
        float edgeTwoX = POINTS[third] - POINTS[first];
        float edgeTwoY = POINTS[third + 1] - POINTS[first + 1];
        float edgeTwoZ = POINTS[third + 2] - POINTS[first + 2];

        float normalX = edgeOneY * edgeTwoZ - edgeOneZ * edgeTwoY;
        float normalY = edgeOneZ * edgeTwoX - edgeOneX * edgeTwoZ;
        float normalZ = edgeOneX * edgeTwoY - edgeOneY * edgeTwoX;
        float inverseLength = 1f / (float) Math.sqrt(
                normalX * normalX + normalY * normalY + normalZ * normalZ);
        normalX *= inverseLength;
        normalY *= inverseLength;
        normalZ *= inverseLength;

        float centerX = (POINTS[first] + POINTS[second] + POINTS[third]) / 3f;
        float centerY = (POINTS[first + 1] + POINTS[second + 1] + POINTS[third + 1]) / 3f;
        float centerZ = (POINTS[first + 2] + POINTS[second + 2] + POINTS[third + 2]) / 3f;
        float outwardDot = normalX * centerX + normalY * (centerY - CENTER_OF_VOLUME_Y)
                + normalZ * centerZ;
        // Cross product mengikuti winding; balik normal bila arahnya masuk ke dalam limas.
        if (outwardDot < 0f) {
            normalX = -normalX;
            normalY = -normalY;
            normalZ = -normalZ;
        }

        for (int corner = 0; corner < 3; corner++) {
            int source = corner == 0 ? first : corner == 1 ? second : third;
            int target = (outputIndex + corner) * 3;
            faceVertices[target] = POINTS[source];
            faceVertices[target + 1] = POINTS[source + 1];
            faceVertices[target + 2] = POINTS[source + 2];
            faceNormals[target] = normalX;
            faceNormals[target + 1] = normalY;
            faceNormals[target + 2] = normalZ;
        }
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, normals);
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, VERTEX_COUNT);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
