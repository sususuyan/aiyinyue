package com.zeusee.main.hyperlandmark;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.text.TextUtils;
import android.util.Log;

//import com.zeusee.main.hyperlandmark.bean.BeautyParam;
import com.zeusee.main.hyperlandmark.utils.OpenGLUtils;
//import com.zeusee.main.hyperlandmark.bean.IBeautify;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLBeautyadjust{
    private Context mContext;
    protected String mVertexShader;
    protected String mFragmentShader;
    protected String mVertexShader2;
    protected String mFragmentShader2;


    // 句柄
    protected int mProgramHandle;
    protected int mProgramHandle2;
    protected int mPositionHandle;
    protected int mPositionHandle2;
    protected int mTextureCoordinateHandle;
    protected int mTextureCoordinateHandle2;
    protected int mInputTextureHandle;
    protected int mInputTextureHandle2;
    protected int mcutTextureHandle2;
    protected int uSTMMatrixHandle;
    protected int uSTMMatrixHandle2;
    //protected int uSTMMatrixHandle2;

    // 渲染的Image的宽高
    protected int mImageWidth;
    protected int mImageHeight;

    // 是否初始化成功
    protected boolean mIsInitialized;
    // 显示输出的宽高
    protected int mDisplayWidth;
    protected int mDisplayHeight;

    // 缩放
    private float mBlurScale = 0.5f;
    // 用于显示裁剪的纹理顶点缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    // FBO的宽高，可能跟输入的纹理大小不一致
    protected int mFrameWidth = -1;
    protected int mFrameHeight = -1;

    // FBO
    protected FloatBuffer mFrameBuffer;
    protected int[] mFrameBufferTextures;
    protected int[] mFrameBuffers;

    private int mGrayTexture;
    private int mLookupTexture;

    private int[] vertexBuffers;

    private int grayTextureLoc;
    private int lookupTextureLoc;

    private int levelRangeInvLoc;
    private int levelBlackLoc;
    private int alphaLoc;
    private int opacityLoc;
    private int intensityLoc;
    private float levelRangeInv;
    private float levelBlack;
    private float alpha;
    private float opacity;
    private float intensity;

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

    private final float[] fragementVertexData = {
            -1f,-1f,0,1,
            -1f,1f,0,0,
            1f,-1f,1,1,
            1f,1f,1,0
    };
    public GLBeautyadjust(Context context) {
        this(context, null, null);
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
        mFrameWidth = mDisplayWidth;
        mFrameHeight = mDisplayHeight;

        mFrameBuffer = ByteBuffer.allocateDirect(fragementVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragementVertexData);
        mFrameBuffer.position(0);
    }

    public GLBeautyadjust(Context context, String vertexShader, String fragmentShader) {
       // super(context, vertexShader, fragmentShader);
        mContext = context;
        mVertexShader = ShaderUtils.getShaderFromAssets(mContext, "beauty/vertex_gaussian_pass.glsl");
        mFragmentShader = ShaderUtils.getShaderFromAssets(mContext, "beauty/fragment_old_beauty.glsl");
        mVertexShader2 = ShaderUtils.getShaderFromAssets(mContext,"beauty/vertex_gaussian_pass.glsl");
        mFragmentShader2 = ShaderUtils.getShaderFromAssets(mContext, "beauty/fragment_beauty_blur.glsl");
    }

    public void initProgramHandle(){

        if (!TextUtils.isEmpty(mVertexShader) && !TextUtils.isEmpty(mFragmentShader)) {

            mProgramHandle = OpenGLUtils.createProgram(mVertexShader, mFragmentShader);
            mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "aPosition");
            mTextureCoordinateHandle = GLES30.glGetAttribLocation(mProgramHandle, "aTextureCoord");
            mInputTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "inputTexture");
            uSTMMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle,"uSTMatrix");
            mIsInitialized = true;
        } else {
            mPositionHandle = OpenGLUtils.GL_NOT_INIT;
            mTextureCoordinateHandle = OpenGLUtils.GL_NOT_INIT;
            mInputTextureHandle = OpenGLUtils.GL_NOT_TEXTURE;
            mIsInitialized = false;
        }

        grayTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "grayTexture");
        lookupTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "lookupTexture");
        levelRangeInvLoc = GLES30.glGetUniformLocation(mProgramHandle, "levelRangeInv");
        levelBlackLoc = GLES30.glGetUniformLocation(mProgramHandle, "levelBlack");
        alphaLoc = GLES30.glGetUniformLocation(mProgramHandle, "alpha");
        mGrayTexture = OpenGLUtils.createTextureFromAssets(mContext, "texture/skin_gray.png");
        mLookupTexture = OpenGLUtils.createTextureFromAssets(mContext, "texture/skin_lookup.png");
        levelRangeInv = 1.040816f;//
        levelBlack = 0.01960784f;

        //第二个滤镜
        mProgramHandle2 = OpenGLUtils.createProgram(mVertexShader2, mFragmentShader2);
        mPositionHandle2 = GLES30.glGetAttribLocation(mProgramHandle2, "aPosition");
        mTextureCoordinateHandle2 = GLES30.glGetAttribLocation(mProgramHandle2, "aTextureCoord");
        mInputTextureHandle2 = GLES30.glGetUniformLocation(mProgramHandle2, "inputTexture");
        mcutTextureHandle2 = GLES30.glGetUniformLocation(mProgramHandle2, "cutTexture");
        uSTMMatrixHandle2 = GLES30.glGetUniformLocation(mProgramHandle2,"uSTMatrix");
        opacityLoc = GLES30.glGetUniformLocation(mProgramHandle2, "opacity");
        intensityLoc = GLES30.glGetUniformLocation(mProgramHandle2, "intensity1");

    }

   // @Override
    public void onInputSizeChanged(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    //@Override
    public void onDisplaySizeChanged(int width, int height) {
        mDisplayWidth = width;
        mDisplayHeight = height;

    }

    //@Override
    public boolean drawFrame(int textureId,float[] STMatrix) {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE) {
            return false;
        }

        int currentTexture = textureId;

        mFrameBuffers = new int[1];
        mFrameBufferTextures = new int[1];
        OpenGLUtils.createFrameBuffer(mFrameBuffers, mFrameBufferTextures, mDisplayWidth, mDisplayHeight);
        GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers[0]);
        // 使用当前的program
        GLES30.glUseProgram(mProgramHandle);

        GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUseProgram(mProgramHandle);

        mVertexBuffer.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle, 2,
                GLES30.GL_FLOAT, false, 0, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(mPositionHandle);
            // 绑定纹理坐标缓冲
        mTextureBuffer.position(0);
        GLES30.glVertexAttribPointer(mTextureCoordinateHandle, 2,
                GLES30.GL_FLOAT, false, 0, mTextureBuffer);
        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, currentTexture);
        GLES30.glUniform1i(mInputTextureHandle, 0);
        //ondrawbegin

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + 1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mGrayTexture);
        GLES30.glUniform1i(grayTextureLoc, 1);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + 2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mLookupTexture);
        GLES30.glUniform1i(lookupTextureLoc, 2);
        GLES30.glUniform1f(levelRangeInvLoc, levelRangeInv);
        GLES30.glUniform1f(levelBlackLoc, levelBlack);
        GLES30.glUniform1f(alphaLoc, alpha);

            GLES30.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, STMatrix, 0);
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
            //解绑
        GLES30.glDisableVertexAttribArray(mPositionHandle);
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        //FBO解绑
        GLES30.glUseProgram(0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        int sourceTexture = mFrameBufferTextures[0];
        //GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);

        currentTexture = sourceTexture;
        int blurTexture = currentTexture;
        int highPassBlurTexture = currentTexture;

        //高斯模糊和磨皮美白shader

        GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
       // GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES30.glUseProgram(mProgramHandle2);
        // 绑定顶点坐标缓冲
        mVertexBuffer.position(0);
        GLES30.glVertexAttribPointer(mPositionHandle2, 2,
                GLES30.GL_FLOAT, false, 0, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(mPositionHandle2);
        // 绑定纹理坐标缓冲
        mTextureBuffer.position(0);
        GLES30.glVertexAttribPointer(mTextureCoordinateHandle2, 2,
                GLES30.GL_FLOAT, false, 0, mTextureBuffer);
        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle2);
        // 绑定纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0+3);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, currentTexture);
        GLES30.glUniform1i(mcutTextureHandle2, 3);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES30.glUniform1i(mInputTextureHandle2, 0);
        GLES30.glUniformMatrix4fv(uSTMMatrixHandle2, 1, false, STMatrix, 0);
        GLES30.glUniform1f(opacityLoc, opacity);
        GLES30.glUniform1f(intensityLoc, intensity);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glDisableVertexAttribArray(mPositionHandle2);
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle2);
        GLES30.glDisableVertexAttribArray(mcutTextureHandle2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        destroyFrameBuffer();

        return false;
    }


    public boolean isInitialized() {
        return mIsInitialized;
    }

    //@Override
    public void initFrameBuffer(int width, int height) {
       // super.initFrameBuffer(width, height);
        if (!isInitialized()) {
            return;
        }
        if (mFrameBuffers != null && (mFrameWidth != width || mFrameHeight != height)) {
            destroyFrameBuffer();
        }
        if (mFrameBuffers == null) {
            mFrameWidth = width;
            mFrameHeight = height;
         //   mFrameBuffers = new int[1];
            mFrameBufferTextures = new int[1];
        }
    }

   // @Override
    public void destroyFrameBuffer() {
        //super.destroyFrameBuffer();
        if (!mIsInitialized) {
            return;
        }
        if (mFrameBufferTextures != null) {
            GLES30.glDeleteTextures(1, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }

        if (mFrameBuffers != null) {
            GLES30.glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
        mFrameWidth = -1;

    }

   // @Override
    public void release() {
        //super.release();
        if (mIsInitialized) {
            GLES30.glDeleteProgram(mProgramHandle);
            mProgramHandle = OpenGLUtils.GL_NOT_INIT;
            GLES30.glDeleteProgram(mProgramHandle2);
            mProgramHandle2 = OpenGLUtils.GL_NOT_INIT;
        }
        destroyFrameBuffer();
    }


    public void onBeauty(BeautyParam beauty) {
        setSkinBeautyIntensity(beauty.beautyIntensity);
        setComplexionLevel(beauty.complexionIntensity);
    }

    public void setSkinBeautyIntensity(float intensity) {
        opacity = intensity;
    }
    public void setUSMIntensity(float USMintensity) {
        intensity = USMintensity*30;
    }
    public void setComplexionLevel(float intensity){ alpha = intensity;
        Log.d("=============", "drawfinish");}
}
