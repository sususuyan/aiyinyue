package com.afei.camerademo.texturemap;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.afei.camerademo.R;
import com.afei.camerademo.glsurfaceview.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class mapDrawer {
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ShortBuffer mIndexBuffer;

    private FloatBuffer fboVertexBuffer;
    private FloatBuffer fboTextureBuffer;
    private ByteBuffer fboIndexBuffer;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMatrixHandler;

    private int fboPositionHandle;
    private int fboTextureHandle;

    private int mProgram;

    private int fboProgram;

    /*

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
            6,7,3,6,3,2,    //右面
            6,5,1,6,1,2,    //下面
            0,3,2,0,2,1,    //正面
            0,1,5,0,5,4,    //左面
            0,3,7,0,7,4,    //上面
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

     */

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

    private static final float VERTEXES_FBO[] = {
            -1.0f, 1.0f,
            -1.0f,-1.0f,
            1.0f, -1.0f,
            1.0f,  1.0f,
    };
    private static final float TEXTURE_FBO[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };
    private static final byte VERTEX_ORDER[] = { 0, 1, 2, 3 }; // order to draw vertices

    public mapDrawer(Context context){
        mVertexBuffer = ByteBuffer.allocateDirect(cubePositions.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(cubePositions).position(0);
        mColorBuffer = ByteBuffer.allocateDirect(color.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mColorBuffer.put(color).position(0);
        //mIndexBuffer = ByteBuffer.allocateDirect(index.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        //mIndexBuffer.put(index).position(0);

        fboVertexBuffer = ByteBuffer.allocateDirect(VERTEXES_FBO.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fboVertexBuffer.put(VERTEXES_FBO).position(0);
        fboTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_FBO.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        fboTextureBuffer.put(TEXTURE_FBO).position(0);
        fboIndexBuffer = ByteBuffer.allocateDirect(VERTEX_ORDER.length).order(ByteOrder.nativeOrder());
        fboIndexBuffer.put(VERTEX_ORDER).position(0);

        String VERTEX_SHADER = OpenGLUtils.readRawTxt(context, R.raw.cube_vertex_shader);
        String FRAGMENT_SHADER = OpenGLUtils.readRawTxt(context, R.raw.cube_fragment_shader);
        mProgram = OpenGLUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");

        String FBO_VERTEX_SHADER = OpenGLUtils.readRawTxt(context, R.raw.fbo_vertex_shader);
        String FBO_FRAGMENT_SHADER = OpenGLUtils.readRawTxt(context, R.raw.fbo_fragment_shader);
        fboProgram = OpenGLUtils.createProgram(FBO_VERTEX_SHADER, FBO_FRAGMENT_SHADER);
        fboPositionHandle = GLES20.glGetAttribLocation(fboProgram, "vPosition");
        fboTextureHandle = GLES20.glGetAttribLocation(fboProgram, "inputTextureCoordinate");
    }

    public void draw(float[] mMVPMatrix, int width, int height) {
        // 生成FrameBuffer
        int [] FBOId = new int[1];
        GLES20.glGenFramebuffers(1, FBOId, 0);
        int framebufferId = FBOId[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
        // 生成Texture
        int [] FBOTextureId = new int[1];
        GLES20.glGenTextures(1, FBOTextureId, 0);
        int textureId = FBOTextureId[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, width, height, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, null);
        // 生成Renderbuffer
        int [] RBOId = new int[1];
        GLES20.glGenRenderbuffers(1, RBOId, 0);
        int renderId = RBOId[0];
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderId);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,width, height);
        // 关联FrameBuffer和Texture、RenderBuffer
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderId);
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.i("fbo", "Framebuffer error");
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);
        //设置背景颜色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler= GLES20.glGetUniformLocation(mProgram,"vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler,1,false,mMVPMatrix,0);
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, mVertexBuffer);
        //设置绘制三角形的颜色
//        GLES20.glUniform4fv(mColorHandle, 2, color, 0);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle,4,
                GLES20.GL_FLOAT,false,
                0,mColorBuffer);
        //索引法绘制正方体
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES,index.length, GLES20.GL_UNSIGNED_SHORT,mIndexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,  36);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glDisableVertexAttribArray(mMatrixHandler);


        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //设置背景颜色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glUseProgram(fboProgram);
        int FBOTexture = GLES20.glGetUniformLocation(fboProgram, "s_texture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, FBOTextureId[0]);
        GLES20.glUniform1i(FBOTexture, 0);

        GLES20.glEnableVertexAttribArray(fboPositionHandle);
        GLES20.glVertexAttribPointer(fboPositionHandle, 2, GLES20.GL_FLOAT, false, 0, fboVertexBuffer);
        GLES20.glEnableVertexAttribArray(fboTextureHandle);
        GLES20.glVertexAttribPointer(fboTextureHandle, 2, GLES20.GL_FLOAT, false, 0, fboTextureBuffer);
        // 真正绘制的操作
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, VERTEX_ORDER.length, GLES20.GL_UNSIGNED_BYTE, fboIndexBuffer);

        GLES20.glDisableVertexAttribArray(fboPositionHandle);
        GLES20.glDisableVertexAttribArray(fboTextureHandle);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(1, FBOTextureId, 0);
        GLES20.glDeleteFramebuffers(1, FBOId, 0);
        GLES20.glDeleteRenderbuffers(1,RBOId,0);

    }
}
