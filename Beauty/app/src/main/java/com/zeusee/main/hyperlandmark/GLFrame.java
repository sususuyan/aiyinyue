package com.zeusee.main.hyperlandmark;

import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
//import android.opengl.GLES30;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLFrame {

    private int width, height, screenWidth, screenHeight;

    // 109 个关键点
    private final int FacePoints = 109;
    // 笛卡尔坐标系
    private float[] mCartesianVertices = new float[109 * 2];
    // 脸型程度
    private float[] mReshapeIntensity = new float[12];

    private final float[] vertexData = {
            1f, -1f,
            -1f, -1f,
            1f, 1f,
            -1f, 1f,
    };
    private final float[] textureVertexData = {
            1f, 0f,
            0f, 0f,
            1f, 1f,
            0f, 1f
    };
    private FloatBuffer mVertexBuffer;

    private FloatBuffer mTextureBuffer;
    // 笛卡尔坐标缓冲
    private FloatBuffer mCartesianBuffer;

    private int programId = -1;
    private int aPositionHandle;
    private int uTextureSamplerHandle;
    private int aTextureCoordHandle;
    private int uSTMMatrixHandle;
    private int mReshapeIntensityHandle;
    private int mCartesianPointsHandle;

    private String fragmentShader;
    private String vertexShader;

    private int[] vertexBuffers;

    private Context mContext;

    private float[] points= new float[109*2];

    public GLFrame(Context context) {
        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        .put(vertexData);
        mVertexBuffer.position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        .put(textureVertexData);
        mTextureBuffer.position(0);

        mCartesianBuffer = ByteBuffer.allocateDirect(FacePoints * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mContext = context;
    }

    public void initFrame() {

        //加载顶点着色器的脚本内容
        vertexShader = ShaderUtils.getShaderFromAssets(mContext, "face/vertexShader.glsl");
        //加载片元着色器的脚本内容
        fragmentShader = ShaderUtils.getShaderFromAssets(mContext, "face/fragmentShader.glsl");


        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);

        //建立shader里参数对应的句柄
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");//顶点坐标
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord");//纹理坐标

        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix");//变换矩阵

        uTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "sTexture");//纹理
        mReshapeIntensityHandle=GLES20.glGetUniformLocation(programId, "ReshapeIntensity");//美型参数
        mCartesianPointsHandle = GLES20.glGetUniformLocation(programId, "cartesianPoints");//关键点坐标


        vertexBuffers = new int[2];
        GLES20.glGenBuffers(2, vertexBuffers, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, mVertexBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureVertexData.length * 4, mTextureBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }


    private Rect rect = new Rect();

    public void setSize(int screenWidth, int screenHeight, int videoWidth, int videoHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.width = videoWidth;
        this.height = videoHeight;
        rect();
    }

    private void rect() {
        int left, top, viewWidth, viewHeight;
        float sh = screenWidth * 1.0f / screenHeight;
        float vh = width * 1.0f / height;
        if (sh < vh) {
            left = 0;
            viewWidth = screenWidth;
            viewHeight = (int) (height * 1.0f / width * viewWidth);
            top = (screenHeight - viewHeight) / 2;
        } else {
            top = 0;
            viewHeight = screenHeight;
            viewWidth = (int) (width * 1.0f / height * viewHeight);
            left = (screenWidth - viewWidth) / 2;
        }
        rect.left = left;
        rect.top = top;
        rect.right = viewWidth;
        rect.bottom = viewHeight;
    }

    public void drawFrame(int textureId, float[] STMatrix) {
        //updateFaceVertices();
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(rect.left, rect.top, rect.right, rect.bottom);
        GLES20.glUseProgram(programId);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false,
                0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[1]);
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glUniform2fv(mCartesianPointsHandle,109,mCartesianBuffer);

        GLES20.glUniform1fv(mReshapeIntensityHandle, 12, FloatBuffer.wrap(mReshapeIntensity));

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glUniform1i(uTextureSamplerHandle, 0);

        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, STMatrix, 0);

        GLES20.glUniform1i(uTextureSamplerHandle, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void release() {
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteBuffers(2, vertexBuffers, 0);
    }


    public void setpoints(float[] points) {
        this.points=points;

        updateCartesianVertices();
    }

    /**
     * 更新坐标系，计算对应纹理坐标
     */
    private void updateCartesianVertices() {
        for (int i = 0; i < FacePoints; i++) {
            mCartesianVertices[i * 2] = points[i * 2]*0.5f+0.5f ;
            mCartesianVertices[i * 2 + 1] = points[i * 2 + 1]*0.5f+0.5f ;
        }
        mCartesianBuffer.clear();
        mCartesianBuffer.put(mCartesianVertices);
        mCartesianBuffer.position(0);
    }

    public void onBeauty(BeautyParam beauty) {
        if (beauty == null) {
            return;
        }
        mReshapeIntensity[0]  = beauty.faceLift;                // 瘦脸
        mReshapeIntensity[1]  = beauty.faceShave;               // 削脸
        mReshapeIntensity[2]  = beauty.faceNarrow;              // 小脸
        mReshapeIntensity[3]  = beauty.chinIntensity;           // 下巴
        mReshapeIntensity[4]  = beauty.foreheadIntensity;       // 额头
        mReshapeIntensity[5]  = beauty.eyeEnlargeIntensity;     // 大眼
        mReshapeIntensity[6]  = beauty.philtrumIntensity;       // 眼距
        mReshapeIntensity[7]  = beauty.eyeCornerIntensity;      // 眼角
        mReshapeIntensity[8]  = beauty.noseThinIntensity;       // 瘦鼻
        mReshapeIntensity[9]  = beauty.alaeIntensity;           // 鼻翼
        mReshapeIntensity[10] = beauty.proboscisIntensity;      // 长鼻
        mReshapeIntensity[11] = beauty.mouthEnlargeIntensity;   // 嘴型
    }
}