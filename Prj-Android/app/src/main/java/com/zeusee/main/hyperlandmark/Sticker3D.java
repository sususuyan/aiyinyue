package com.zeusee.main.hyperlandmark;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.zeusee.main.hyperlandmark.jni.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import javax.microedition.khronos.opengles.GL;

/**
 * @ClassName Stacker3D
 * @Description TODO
 * @Author : kevin
 * @Date 2021/02/03 9:38
 * @Version 1.0
 */
public class Sticker3D {
    private List<Face> faces;
    private FloatBuffer vertexBuffer,colorBuffer;
    private ShortBuffer indexBuffer;
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;"+
                    "varying  vec4 vColor;"+
                    "attribute vec4 aColor;"+
                    "void main() {" +
                    "  gl_Position = vMatrix*vPosition;" +
                    "  vColor=aColor;"+
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    final int COORDS_PER_VERTEX = 3;
    final float cubePositions[] = {
            -1.0f,1.0f,1.0f,    //正面左上0
            -1.0f,-1.0f,1.0f,   //正面左下1
            1.0f,-1.0f,1.0f,    //正面右下2
            1.0f,1.0f,1.0f,     //正面右上3
            -1.0f,1.0f,-1.0f,    //反面左上4
            -1.0f,-1.0f,-1.0f,   //反面左下5
            1.0f,-1.0f,-1.0f,    //反面右下6
            1.0f,1.0f,-1.0f,     //反面右上7
    };
    final short index[]={
            6,7,4,6,4,5,    //后面
            6,3,7,6,2,3,    //右面
            6,5,1,6,1,2,    //下面
            0,3,2,0,2,1,    //正面
            0,1,5,0,5,4,    //左面
            0,7,3,0,4,7,    //上面
    };

    float color[] = {
            0f,1f,0f,1f,
            0f,1f,0f,1f,
            0f,1f,0f,1f,
            0f,1f,0f,1f,
            1f,0f,0f,1f,
            1f,0f,0f,1f,
            1f,0f,0f,1f,
            1f,0f,0f,1f,
    };

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;

    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] mMVPMatrix=new float[16];

    private int textureId;
    private int frameBufferId;
    private int renderBufferId;

    private int mMatrixHandler;
    //顶点个数
    private final int vertexCount = cubePositions.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    public Sticker3D(){
        vertexBuffer = ByteBuffer.allocateDirect(cubePositions.length*4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(cubePositions);
        vertexBuffer.position(0);

        colorBuffer = ByteBuffer.allocateDirect(color.length*4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(color);
        colorBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(index.length*2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(index);
        indexBuffer.position(0);

        mProgram = ShaderUtils.createProgram(vertexShaderCode, fragmentShaderCode);
        initMatrix();
    }

    public void initMatrix(){
        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    private int width;
    private int height;
    private boolean rotate270;
    public void initFrame(){
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        int[] renderBuffers = new int[1];
        GLES20.glGenRenderbuffers(1, renderBuffers, 0);
        renderBufferId = renderBuffers[0];
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,width, height);

        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);
        frameBufferId = frameBuffers[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBufferId);
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.i("fbo", "Framebuffer error");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0); // 解绑renderBuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        mMatrixHandler= GLES20.glGetUniformLocation(mProgram,"vMatrix");

    }

    public void drawFrame(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        float[] points;
        for(Face r : faces){
            points = new float[106 * 2];
            for (int i = 0; i < 106; i++) {
                int x;
                if (rotate270) {
                    x = r.landmarks[i * 2] * CameraOverlap.SCALLE_FACTOR;
                } else {
                    x = CameraOverlap.PREVIEW_HEIGHT - r.landmarks[i * 2];
                }
                int y = r.landmarks[i * 2 + 1] * CameraOverlap.SCALLE_FACTOR;
                points[i * 2] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                points[i * 2 + 1] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
            }
            calculateVertex(r, points);
            draw();
        }
    }

    private void draw(){
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,index.length, GLES20.GL_UNSIGNED_SHORT,indexBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void calculateVertex(Face face, float[] points){
        float centerX = points[23*2];
        float centerY = points[23*2+1];
        float centerZ = 0.0f;
        float faceWidth = (float) getDistance(
                points[7*2],
                points[7*2+1],
                points[17*2],
                points[17*2+1]
        )/2;
        float faceHeight = (float) getDistance(
                points[23*2],
                points[23*2+1],
                points[0*2],
                points[0*2+1]
        );
        float leftX = points[7*2];
        float rightX = points[17*2];
        float bottomY = points[0*2+1];
        float topY = centerY + faceHeight;
        float faceDeep = faceWidth*2;
        Log.e("debug", "centerX: "+centerX+"  centerY: "+centerY+"   centerZ: "+centerZ);
        Log.e("debug", "faceWidth: "+faceWidth+"   faceHeight: "+faceHeight+"   faceDeep: "+faceDeep);
        float[] vertices = new float[24];
        vertices[0] = leftX;
        vertices[1] = topY;
        vertices[2] = centerZ;

        vertices[3] = leftX;
        vertices[4] = bottomY;
        vertices[5] = centerZ;

        vertices[6] = rightX;
        vertices[7] = bottomY;
        vertices[8] = centerZ ;

        vertices[9] = rightX;
        vertices[10] = topY;
        vertices[11] = centerZ;

        vertices[12] = leftX;
        vertices[13] = topY;
        vertices[14] = centerZ - faceDeep;

        vertices[15] = leftX;
        vertices[16] = bottomY;
        vertices[17] = centerZ - faceDeep;

        vertices[18] = rightX;
        vertices[19] = bottomY;
        vertices[20] = centerZ - faceDeep;

        vertices[21] = rightX;
        vertices[22] = topY;
        vertices[23] = centerZ - faceDeep;

        setPoints(vertices);

        float[] mModelMatrix = new float[16];
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, centerX, centerY, centerZ);

        float pitchAngle = -face.pitch;
        float yawAngle = face.yaw;
        float rollAngle = -face.roll;
        if(Math.abs(yawAngle)>90){
            yawAngle = (yawAngle/Math.abs(yawAngle))*90;
        }
        if(Math.abs(pitchAngle)>90){
            pitchAngle = (pitchAngle / Math.abs(pitchAngle)) * 90;
        }

        Log.e("debug:", "rollAngle: "+rollAngle+"\tyawAngle: "+yawAngle+"\tpitchAngle: "+pitchAngle);
        Matrix.rotateM(mModelMatrix, 0, rollAngle, 0, 0, 1);
        Matrix.rotateM(mModelMatrix, 0, yawAngle, 0, 1, 0);
        Matrix.rotateM(mModelMatrix, 0, pitchAngle, 1, 0, 0);

        Matrix.translateM(mModelMatrix, 0, -centerX, -centerY, 0);

        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mModelMatrix, 0);
    }

    public void inputSizeChanged(int width, int height, boolean rotate270){
        this.width = width;
        this.height = height;
        this.rotate270 = rotate270;
        float Ratio = (float) width / height;
        Matrix.frustumM(mProjectMatrix, 0, -Ratio, Ratio, -1.0f, 1.0f, 3.0f, 9.0f);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    public void setPoints(float[] points){
        vertexBuffer.rewind();
        vertexBuffer.put(points);
        vertexBuffer.position(0);
    }

    public int getTextureId(){
        return textureId;
    }

    public void setFaces(List<Face> faces){
        this.faces = faces;
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

    private double getDistance(float x1, float y1, float x2, float y2){
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }
}
