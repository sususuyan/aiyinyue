package com.zeusee.main.hyperlandmark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName sticker3D
 * @Description TODO
 * @Author : kevin
 * @Date 2021/03/06 20:16
 * @Version 1.0
 */
public class Sticker3D {
    private String stickerName;
    private List<Obj3D> objs;
    private List<FaceStickerJson> stickerJsonList;
    private int[] textures;

    private String vertexShaderCode;
    private String fragmentShaderCode;

    private int programId;
    private int mHPosition;
    private int mHNormal;
    private int mHMatrix;
    private int mHCoord;
    private int mHTexture;
    private int mHKa;
    private int mHKd;
    private int mHKs;
    private Context context;

    private int width;
    private int height;
    private boolean rotate270;

    private int[] fboTextureId;
    private int[] frameBufferId;
    private int[] renderBufferId;

    private float[] mModelMatrix = new float[16];

    private FloatBuffer mVerBuffer;
    private FloatBuffer mTexBuffer;
    private int textureType=0;
    private int textureId=0;

    //顶点坐标
    private float pos[] = {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f,  -1.0f,
    };

    //纹理坐标
    private float[] coord={
            0.0f, 0.0f,
            0.0f,  1.0f,
            1.0f,  0.0f,
            1.0f, 1.0f,
    };

    public Sticker3D(Context context){
        this.context = context;
        initBuffer();

    }

    public void initBuffer(){
        ByteBuffer a=ByteBuffer.allocateDirect(32);
        a.order(ByteOrder.nativeOrder());
        mVerBuffer=a.asFloatBuffer();
        mVerBuffer.put(pos);
        mVerBuffer.position(0);
        ByteBuffer b=ByteBuffer.allocateDirect(32);
        b.order(ByteOrder.nativeOrder());
        mTexBuffer=b.asFloatBuffer();
        mTexBuffer.put(coord);
        mTexBuffer.position(0);
    }

    public void onCreate(){
        vertexShaderCode = ShaderUtils.getShaderFromAssets(context, "shader/vertex_obj.glsl");
        fragmentShaderCode = ShaderUtils.getShaderFromAssets(context, "shader/fragment_obj.glsl");

        programId = ShaderUtils.createProgram(vertexShaderCode, fragmentShaderCode);
        mHPosition = GLES20.glGetAttribLocation(programId, "vPosition");
        mHNormal = GLES20.glGetAttribLocation(programId, "vNormal");
        mHMatrix=GLES20.glGetUniformLocation(programId,"vMatrix");
        mHCoord=GLES20.glGetAttribLocation(programId,"vCoord");
        mHTexture=GLES20.glGetUniformLocation(programId,"vTexture");
        mHKa=GLES20.glGetUniformLocation(programId,"vKa");
        mHKd=GLES20.glGetUniformLocation(programId,"vKd");
        mHKs=GLES20.glGetUniformLocation(programId,"vKs");

    }

    public void initFrame(){
//        fboTextureId = new int[1];
//        GLES20.glGenTextures(1, fboTextureId, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId[0]);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_MIRRORED_REPEAT);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_MIRRORED_REPEAT);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
//                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
//
//        renderBufferId = new int[1];
//        GLES20.glGenRenderbuffers(1, renderBufferId, 0);
//        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId[0]);
//        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,width, height);
//
//        frameBufferId = new int[1];
//        GLES20.glGenFramebuffers(1, frameBufferId, 0);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId[0]);
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureId[0], 0);
//        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBufferId[0]);
//        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
//            Log.i("fbo", "Framebuffer error");
//        }
    }

    public void drawFrame(){
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId[0]);
//        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        for(int i=0;i<objs.size();i++){
            Obj3D obj = objs.get(i);
            calculateVertex();
            draw(obj, textures[i], i);
        }
//        return fboTextureId[0];
    }

    public void draw(Obj3D obj, int tid, int textureType){
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId[0]);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glUseProgram(programId);
        GLES20.glEnableVertexAttribArray(mHPosition);
        GLES20.glVertexAttribPointer(mHPosition,3, GLES20.GL_FLOAT, false, 3*4,obj.vert);
        GLES20.glEnableVertexAttribArray(mHNormal);
        GLES20.glVertexAttribPointer(mHNormal,3, GLES20.GL_FLOAT, false, 3*4,obj.vertNorl);
        GLES20.glEnableVertexAttribArray(mHCoord);
        GLES20.glVertexAttribPointer(mHCoord,2,GLES20.GL_FLOAT,false,0,obj.vertTexture);
        GLES20.glUniformMatrix4fv(mHMatrix,1,false, mModelMatrix,0);
        if(obj!=null&&obj.mtl!=null){
            GLES20.glUniform3fv(mHKa,1,obj.mtl.Ka,0);
            GLES20.glUniform3fv(mHKd,1,obj.mtl.Kd,0);
            GLES20.glUniform3fv(mHKs,1,obj.mtl.Ks,0);
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+textureType);
        GLES20.glBindBuffer(GLES20.GL_TEXTURE_2D, tid);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,obj.vertCount);
        GLES20.glDisableVertexAttribArray(mHPosition);
        GLES20.glDisableVertexAttribArray(mHNormal);
        GLES20.glDisableVertexAttribArray(mHCoord);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    public void calculateVertex(){
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0,0.1f, 0.1f*width/height, 0.1f);
    }

    public void release(){
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteTextures(1, fboTextureId, 0);
        GLES20.glDeleteRenderbuffers(1, renderBufferId, 0);
        GLES20.glDeleteFramebuffers(1, frameBufferId, 0);
    }

    public void sizeChange(int width, int height, boolean rotate270){
        GLES20.glViewport(0,0,width,height);
        this.width = width;
        this.height = height;
        this.rotate270 = rotate270;
    }
    public void loadSticker(){
        String folderPath = "3DModel/"+stickerName;
        try{
            stickerJsonList = ResourceDecoder.decodeStickerData(context, folderPath+"/json");
            if(stickerJsonList == null){
                Log.e("Sticker3D", "tickerJsonList is null");
                return;
            }
        }catch (IOException | JSONException e){
            Log.e("Sticker3D", "IOException or JSONException", e);
        }
        String folderPath1 = "assets/"+folderPath;
        objs = ObjReader.readMultiObj(context, folderPath1+"/"+stickerJsonList.get(0).stickerName+".obj");
        textures = new int[objs.size()];
        Log.e("debug", "objs.size()"+objs.size());
        for(int i=0;i<objs.size();i++){
            if(objs.get(i).vertTexture == null || objs.get(i).mtl.map_Kd == null){
                textures[i]=0;
            }
            else {
                try{
                    Log.e("debug", "map_kd: "+ folderPath+"/"+objs.get(i).mtl.map_Kd);
                    Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(folderPath+"/"+objs.get(i).mtl.map_Kd));
                    textures[i] = createTexture(bitmap);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void setStickerName(String name) {
        stickerName = name;
        loadSticker();
    }

    private int createTexture(Bitmap bitmap){
        int[] texture=new int[1];
        if(bitmap!=null&&!bitmap.isRecycled()){
            //生成纹理
            GLES20.glGenTextures(1,texture,0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            return texture[0];
        }
        return 0;
    }
}
