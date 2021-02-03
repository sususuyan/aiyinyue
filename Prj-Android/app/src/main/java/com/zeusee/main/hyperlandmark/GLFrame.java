package com.zeusee.main.hyperlandmark;

import android.graphics.Rect;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class GLFrame {

    private int width,height,screenWidth,screenHeight;

    private final float[] vertexData = {
            1f, -1f, 0f,
            -1f, -1f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f
    };
    private final float[] textureVertexData = {
            1f, 0f,
            0f, 0f,
            1f, 1f,
            0f, 1f
    };

    private FloatBuffer vertexBuffer;

    private FloatBuffer textureVertexBuffer;

    private FloatBuffer vertexBuffer1;

    private FloatBuffer textureVertexBuffer1;
    private int programId = -1;
    private int program1 = -1;
    private int aPositionHandle;
    private int uTextureSamplerHandle;
    private int iTextureSamplerHandle;
    private int aTextureCoordHandle;
    private int uSTMMatrixHandle;
    private int aPositionHandle1;
    private int iTextureSamplerHandle1;
    private int iTextureCoordHandle1;
    private int iHandle;

    private int[] vertexBuffers;



    private String fragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
            "varying highp vec2 vTexCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "uniform sampler2D iTexture;\n" +
            "uniform highp float i;\n"+
            "uniform highp mat4 uSTMatrix;\n" +
            "void main() {\n" +
            "    highp vec2 tx_transformed = (uSTMatrix * vec4(vTexCoord, 0, 1.0)).xy;\n" +
            "    highp vec4 video = texture2D(sTexture, tx_transformed);\n" +
            "    highp vec4 rgba;\n"+
            "    if(i == 0.0){\n" +
            "       rgba = video;\n" +
            "    }\n"+
            "    else{\n" +
            "       highp vec4 image = texture2D(iTexture, vTexCoord);\n" +
            "       rgba = mix(video,image,image.a);\n"+
            "    }\n"+
            "    gl_FragColor = rgba;\n" +
            "}";

    private  String vertexShader = "attribute vec4 aPosition;\n" +
            "attribute vec2 aTexCoord;\n" +
            "varying vec2 vTexCoord;\n" +
            "void main() {\n" +
            "    vTexCoord = aTexCoord;\n" +
            "    gl_Position = aPosition;\n" +
            "}";

//    private String cubeFragmentShader = "precision mediump float;\n"+
//            "varying vec2 vTexCoord;\n"+
//            "uniform sampler2D iTexture;\n"+
//            "void main() {\n"+
//            "    gl_FragColor = texture2D(iTexture, vTexCoord);\n"+
//            "}";

    public GLFrame(){
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

        vertexBuffer1 = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer1.position(0);

        textureVertexBuffer1 = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer1.position(0);
    }
    public void initFrame(){
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix");
        uTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "sTexture");
        iTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "iTexture");
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord");
        iHandle =GLES20.glGetUniformLocation(programId,"i");

    }

    private float s = 1.0f;
    public void setS(float s){
        this.s = s;
    }
    private float h = 0.0f;
    public void setH(float h) {
        this.h = h;
    }
    private float l = 1.0f;
    public void setL(float l) {
        this.l = l;
    }


    private Rect rect = new Rect();
    public void setSize(int screenWidth,int screenHeight,int videoWidth,int videoHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.width = videoWidth;
        this.height = videoHeight;
        rect();
    }

    private void rect(){
        int left,top,viewWidth,viewHeight;
        float sh = screenWidth*1.0f/screenHeight;
        float vh = width *1.0f/ height;
        if(sh < vh){
            left = 0;
            viewWidth = screenWidth;
            viewHeight = (int)(height *1.0f/ width *viewWidth);
            top = (screenHeight - viewHeight)/2;
        }else{
            top = 0;
            viewHeight = screenHeight;
            viewWidth = (int)(width *1.0f/ height *viewHeight);
            left = (screenWidth - viewWidth)/2;
        }
        rect.left = left;
        rect.top = top;
        rect.right = viewWidth;
        rect.bottom = viewHeight;
    }

    public void drawFrame(int tId,int textureId,float[] STMatrix){
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(rect.left, rect.top, rect.right, rect.bottom);
        GLES20.glUseProgram(programId);

        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureVertexBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(uTextureSamplerHandle,0);
        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, STMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tId);
        GLES20.glUniform1i(iTextureSamplerHandle,1);
        GLES20.glUniform1f(iHandle,tId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }

    public void release(){
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteBuffers(2,vertexBuffers,0);
    }
}
