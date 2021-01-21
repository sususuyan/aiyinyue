package com.afei.camerademo.texturemap;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class mapSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] mMVPMatrix=new float[16];

    private mapDrawer mDrawer;
    private Context mContext;

    private int ScreenWidth;
    private int ScreenHeight;

    public mapSurfaceView(Context context){
        this(context, null);

    }

    public mapSurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);
        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        mDrawer = new mapDrawer(mContext);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        ScreenWidth = width;
        ScreenHeight = height;
        //计算宽高比
        float ratio=(float)width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //Matrix.perspectiveM(mProjectMatrix, 0, 45, (float)width/height, 2, 5);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 10.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        Matrix.rotateM(mMVPMatrix,0,0.3f,0,1,0);
        mDrawer.draw(mMVPMatrix, ScreenWidth, ScreenHeight);

    }
}
