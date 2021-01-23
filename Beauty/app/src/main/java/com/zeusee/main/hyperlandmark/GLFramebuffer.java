package com.zeusee.main.hyperlandmark;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;


public class GLFramebuffer {

    private float[] mSTMatrix = new float[16];

    private int[] textures;

    private SurfaceTexture surfaceTexture;
    public void initFramebuffer(){
        //创建纹理id
        textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public SurfaceTexture getSurfaceTexture(){
        //创建SurfaceTexture并传入textures[0]
        surfaceTexture = new SurfaceTexture(textures[0]);
        return surfaceTexture;
    }

    public void release(){
        GLES20.glDeleteTextures(1,textures,0);
        if(surfaceTexture != null ){
            surfaceTexture.release();
            surfaceTexture = null;
        }
    }

    public int drawFrameBuffer(){
        if(surfaceTexture != null){
            surfaceTexture.updateTexImage();//用updateTexImage将Camera中新的预览写入纹理
            surfaceTexture.getTransformMatrix(mSTMatrix);//纹理进行矩阵变换
        }
        return textures[0];
    }

    public float[] getMatrix() {
        return mSTMatrix;
    }

}
