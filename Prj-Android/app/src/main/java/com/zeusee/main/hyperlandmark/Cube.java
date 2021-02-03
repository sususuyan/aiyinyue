package com.zeusee.main.hyperlandmark;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @ClassName Cube
 * @Description TODO
 * @Author : kevin
 * @Date 2021/02/02 14:01
 * @Version 1.0
 */
public class Cube {
    private final float cubePositions[] = {
            -1.0f,1.0f,1.0f,
            1.0f,1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f,1.0f,
            1.0f,-1.0f,1.0f,
            -1.0f, -1.0f,1.0f,
            -1.0f,1.0f, 1.0f,
            -1.0f,-1.0f,1.0f,
            -1.0f,-1.0f,-1.0f,
            -1.0f, 1.0f,1.0f,
            -1.0f,-1.0f,-1.0f,
            -1.0f,1.0f,-1.0f,
            -1.0f,1.0f,1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, 1.0f,1.0f,
            -1.0f,1.0f, 1.0f,
            -1.0f,1.0f, -1.0f,
            1.0f,1.0f,-1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f,1.0f, -1.0f,
            -1.0f,1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f,1.0f,-1.0f,
            -1.0f, -1.0f,-1.0f,
            1.0f,-1.0f,-1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f,1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f,1.0f,
            1.0f, 1.0f,1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,-1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f,-1.0f, 1.0f

    };
    private float color[] = {
            0.583f,  0.771f,  0.014f, 1.0f,
            0.609f,  0.115f,  0.436f, 1.0f,
            0.327f,  0.483f,  0.844f, 1.0f,
            0.822f,  0.569f,  0.201f, 1.0f,
            0.435f,  0.602f,  0.223f, 1.0f,
            0.310f,  0.747f,  0.185f, 1.0f,
            0.597f,  0.770f,  0.761f, 1.0f,
            0.559f,  0.436f,  0.730f, 1.0f,
            0.359f,  0.583f,  0.152f, 1.0f,
            0.483f,  0.596f,  0.789f, 1.0f,
            0.559f,  0.861f,  0.639f, 1.0f,
            0.195f,  0.548f,  0.859f, 1.0f,
            0.014f,  0.184f,  0.576f, 1.0f,
            0.771f,  0.328f,  0.970f, 1.0f,
            0.406f,  0.615f,  0.116f, 1.0f,
            0.676f,  0.977f,  0.133f, 1.0f,
            0.971f,  0.572f,  0.833f, 1.0f,
            0.140f,  0.616f,  0.489f, 1.0f,
            0.997f,  0.513f,  0.064f, 1.0f,
            0.945f,  0.719f,  0.592f, 1.0f,
            0.543f,  0.021f,  0.978f, 1.0f,
            0.279f,  0.317f,  0.505f, 1.0f,
            0.167f,  0.620f,  0.077f, 1.0f,
            0.347f,  0.857f,  0.137f, 1.0f,
            0.055f,  0.953f,  0.042f, 1.0f,
            0.714f,  0.505f,  0.345f, 1.0f,
            0.783f,  0.290f,  0.734f, 1.0f,
            0.722f,  0.645f,  0.174f, 1.0f,
            0.302f,  0.455f,  0.848f, 1.0f,
            0.225f,  0.587f,  0.040f, 1.0f,
            0.517f,  0.713f,  0.338f, 1.0f,
            0.053f,  0.959f,  0.120f, 1.0f,
            0.393f,  0.621f,  0.362f, 1.0f,
            0.673f,  0.211f,  0.457f, 1.0f,
            0.820f,  0.883f,  0.371f, 1.0f,
            0.982f,  0.099f,  0.879f, 1.0f

    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private int mProgram;
    private int textureId;
    private int frameBufferId;
    private int renderBufferId;

    private int mPositionHandler;
    private int mColorHandler;
    private int mMatrixHandler;

    private String vertexShader = "attribute vec3 vPosition;\n"+
            "uniform mat4 vMatrix;\n"+
            "varying vec4 vColor;\n"+
            "attribute vec4 aColor;\n"+
            "void main() {\n"+
            "    gl_Position = vMatrix * vec4(vPosition, 1.0);\n"+
            "    vColor = aColor;\n"+
            "}";

    private String fragmentShader = "precision mediump float;\n"+
            "varying vec4 vColor;\n"+
            "void main() {\n"+
            "    gl_FragColor = vColor;\n"+
            "}";

    public Cube(){
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

        mProgram = ShaderUtils.createProgram(vertexShader, fragmentShader);

        mPositionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandler = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    int width;
    int height;
    public void draw(float[] mMVPMatrix){
        Log.e("debug", "cube draw");
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

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);
//        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(mProgram);

        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        GLES20.glEnableVertexAttribArray(mPositionHandler);
        GLES20.glVertexAttribPointer(mPositionHandler, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mColorHandler);
        GLES20.glVertexAttribPointer(mColorHandler, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glDisableVertexAttribArray(mPositionHandler);
        GLES20.glDisableVertexAttribArray(mColorHandler);
        GLES20.glDisableVertexAttribArray(mMatrixHandler);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    public int getTextureId(){
        return textureId;
    }

    public void sizeChange(int width, int height){
        this.width  = width;
        this.height = height;
    }
}
