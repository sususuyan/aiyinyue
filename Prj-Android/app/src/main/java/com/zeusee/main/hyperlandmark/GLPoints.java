package com.zeusee.main.hyperlandmark;

import android.opengl.GLES20;
import android.util.Log;

import com.zeusee.main.hyperlandmark.jni.Face;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

public class GLPoints {
    private FloatBuffer vertexBuffer;
    private int bufferLength = 109*2*4;
    private int programId = -1;
    private int aPositionHandle;

    private int[] vertexBuffers;

    private String fragmentShader =
            "void main() {\n" +
            "    gl_FragColor = vec4(1.0,0.0,0.0,1.0);\n" +
            "}";
    private  String vertexShader = "attribute vec2 aPosition;\n" +
            "void main() {\n" +
            "    gl_Position = vec4(aPosition,0.0,1.0);\n" +
            "    gl_PointSize = 10.0;\n"+
            "}";
    public GLPoints(){
        vertexBuffer = ByteBuffer.allocateDirect(bufferLength)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.position(0);
    }
    public void initPoints(){
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");

        vertexBuffers = new int[1];
        GLES20.glGenBuffers(1,vertexBuffers,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferLength, vertexBuffer,GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }
    public void setPoints(float[] points){
        vertexBuffer.rewind();
        vertexBuffer.put(points);
        vertexBuffer.position(0);
    }

    public void setFace(List<Face> face){
        float[] points;
        float[] epoints;
        for (Face r : face){
            points = new float[106*2];
            epoints = new float[109*2];
            for (int i = 0; i < 106; i++) {
                int x;
                if (true) {
                    x = r.landmarks[i * 2] * CameraOverlap.SCALLE_FACTOR;
                } else {
                    x = CameraOverlap.PREVIEW_HEIGHT - r.landmarks[i * 2];
                }
                int y = r.landmarks[i * 2 + 1] * CameraOverlap.SCALLE_FACTOR;
                points[i * 2] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                points[i * 2 + 1] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
            }
            epoints = FaceTracking.getInstance().calculateExtraFacePoints(points);
            setPoints(epoints);
        }
    }
    public void drawPoints(){
        Log.e("debug", "GLPoint draw points");
        GLES20.glUseProgram(programId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,0,bufferLength,vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false,
                0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 109);
    }

    public void release(){
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteBuffers(1,vertexBuffers,0);
    }

    private float view2openglX(int x, int width) {
        float centerX = width / 2.0f;
        float t = x - centerX;
        return t / centerX;
    }

    private float view2openglY(int y, int height) {
        float centerY = height / 2.0f;
        float s = centerY - y;
        return s / centerY;
    }
}
