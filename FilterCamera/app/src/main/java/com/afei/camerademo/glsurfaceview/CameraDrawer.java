package com.afei.camerademo.glsurfaceview;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.afei.camerademo.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CameraDrawer {

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mBackTextureBuffer;
    private FloatBuffer mFrontTextureBuffer;
    private FloatBuffer mFboTextureBuffer;
    private ByteBuffer mDrawListBuffer;
    //private ByteBuffer mBuffer;
    private int mProgram;
    private int fboProgram;
    private int mPositionHandle;
    private int mTextureHandle;
    private int fboPositionHandle;
    private int fboTextureHandle;

    private static final float VERTEXES[] = {
            -1.0f, 1.0f,
            -1.0f,-1.0f,
            1.0f, -1.0f,
            1.0f,  1.0f,
    };

    // 后置摄像头使用的纹理坐标
    private static final float TEXTURE_BACK[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    // 前置摄像头使用的纹理坐标
    private static final float TEXTURE_FRONT[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    //fbo纹理坐标
    private static final float TEXTURE_FBO[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private static final byte VERTEX_ORDER[] = { 0, 1, 2, 3 }; // order to draw vertices

    private final int VERTEX_SIZE = 2;
    private final int VERTEX_STRIDE = VERTEX_SIZE * 4;

    public CameraDrawer(Context context) {
        // init float buffer for vertex coordinates
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEXES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(VERTEXES).position(0);

        // init float buffer for texture coordinates
        mBackTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_BACK.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mBackTextureBuffer.put(TEXTURE_BACK).position(0);
        mFrontTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_FRONT.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mFrontTextureBuffer.put(TEXTURE_FRONT).position(0);
        mFboTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_FBO.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mFboTextureBuffer.put(TEXTURE_FBO).position(0);

        // init byte buffer for draw list
        mDrawListBuffer = ByteBuffer.allocateDirect(VERTEX_ORDER.length).order(ByteOrder.nativeOrder());
        mDrawListBuffer.put(VERTEX_ORDER).position(0);

        String VERTEX_SHADER = OpenGLUtils.readRawTxt(context, R.raw.vertex_shader);
        String FRAGMENT_SHADER = OpenGLUtils.readRawTxt(context, R.raw.lut_fragment_shader);
        mProgram = OpenGLUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTextureHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");

        String FBO_FRAGMENT_SHADER = OpenGLUtils.readRawTxt(context, R.raw.fragment_shader);
        fboProgram = OpenGLUtils.createProgram(VERTEX_SHADER, FBO_FRAGMENT_SHADER);
        fboPositionHandle = GLES20.glGetAttribLocation(fboProgram, "vPosition");
        fboTextureHandle = GLES20.glGetAttribLocation(fboProgram, "inputTextureCoordinate");
    }

    public void draw(int texture, boolean isFrontCamera, Context context, int width, int height, int FilterImage, float strength) {

        //FBO离屏渲染
        int[] FBOId = new int[1];
        int[] FBOTextureId = new int[1];
        GLES20.glGenFramebuffers(1,FBOId,0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, FBOId[0]);
        GLES20.glGenTextures(1, FBOTextureId,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, FBOTextureId[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, FBOTextureId[0], 0);
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
                != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("fbo", "glFramebufferTexture2D error");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //mBuffer = ByteBuffer.allocate(width * height * 4);

        //清空颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //设置背景颜色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glUseProgram(mProgram); // 指定使用的program
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, FBOId[0]);
        GLES20.glEnable(GLES20.GL_CULL_FACE); // 启动剔除
        GLES20.glViewport(0, 0, width, height);

        int cameraTextureHandle = GLES20.glGetUniformLocation(mProgram, "s_Texture");
        GLES20.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture); // 绑定纹理
        GLES20.glUniform1i(cameraTextureHandle, 0);

        int[] LUTTextureId = OpenGLUtils.loadTextureFromRes(FilterImage, context);
        int hTextureLUT = GLES20.glGetUniformLocation(mProgram, "textureLUT");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, LUTTextureId[0]);
        GLES20.glUniform1i(hTextureLUT, 1);

        int controlindex = GLES20.glGetUniformLocation(mProgram, "strength");
        GLES20.glUniform1f(controlindex, strength);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mTextureHandle);
        if (isFrontCamera) {
            GLES20.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mFrontTextureBuffer);
        } else {
            GLES20.glVertexAttribPointer(mTextureHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mBackTextureBuffer);
        }
        // 真正绘制的操作
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, VERTEX_ORDER.length, GLES20.GL_UNSIGNED_BYTE, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glDeleteTextures(1, LUTTextureId, 0);

        //GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mBuffer);



        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //设置背景颜色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        //使用程序
        GLES20.glUseProgram(fboProgram);
        //设置纹理
        //绑定渲染纹理  默认是第0个位置

        int FBOTexture = GLES20.glGetUniformLocation(fboProgram, "s_texture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, FBOTextureId[0]);
        GLES20.glUniform1i(FBOTexture, 0);

        GLES20.glEnableVertexAttribArray(fboPositionHandle);
        GLES20.glVertexAttribPointer(fboPositionHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(fboTextureHandle);
        GLES20.glVertexAttribPointer(fboTextureHandle, VERTEX_SIZE, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mFboTextureBuffer);
        // 真正绘制的操作
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, VERTEX_ORDER.length, GLES20.GL_UNSIGNED_BYTE, mDrawListBuffer);

        GLES20.glDisableVertexAttribArray(fboPositionHandle);
        GLES20.glDisableVertexAttribArray(fboTextureHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDeleteTextures(1, FBOTextureId, 0);
        GLES20.glDeleteFramebuffers(1, FBOId, 0);

    }
}
