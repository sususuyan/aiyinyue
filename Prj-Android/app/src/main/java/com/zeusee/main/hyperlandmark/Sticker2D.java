package com.zeusee.main.hyperlandmark;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.zeusee.main.hyperlandmark.jni.Face;

import org.json.JSONException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName FaceSticker2D
 * @Description TODO
 * @Author : kevin
 * @Date 2021/02/03 16:20
 * @Version 1.0
 */
public class Sticker2D {
    private static final String TAG = "FaceStickerBitmap";
    private static final float PROJECTION_SCALE = 1.0f;
    private List<Face> faces;
    private String vertexShader;
    private String fragmentShader;

    private FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            1f, -1f,
            -1f, -1f,
            1f, 1f,
            -1f, 1f
    };
    private FloatBuffer textureVertexBuffer;
    private final float[] textureVertexData = {
            1f, 0f,//右下
            0f, 0f,//左下
            1f, 1f,//右上
            0f, 1f//左上
    };

    private int[] frameBuffers;
    private int programId;
    private int aPositionHandle;
    private int uTextureSamplerHandle;
    private int aTextureCoordHandle;
    private int vPMatrixHandle;

    private int[] textures;

    private List<FaceStickerJson> mStickerList;
    private List<FaceStickerLoader> mStickerLoaderList;
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float mRatio;
    private float[] mStickerVertices = new float[8];

    private Context context;
    public Sticker2D(Context context, String folderPath){
        this.context = context;
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

        try{
            mStickerList = ResourceDecoder.decodeStickerData(this.context, folderPath+"/json");
            if(mStickerList ==  null){
                Log.e("debug", "mStickerList is null");
            }
        } catch (IOException | JSONException e){
            Log.e(TAG, "IOException or JSONException: ", e);
        }
        mStickerLoaderList = new ArrayList<>();
        if(mStickerList != null){
            Log.e("debug", "mStickerList is not null");
            for(int i=0;i<mStickerList.size();i++){
                if(mStickerList.get(i) instanceof FaceStickerJson) {
                    String path = folderPath + "/" + mStickerList.get(i).stickerName;
                    System.out.println("\t\t"+path);
                    mStickerLoaderList.add(new FaceStickerLoader(mStickerList.get(i), path));
                }
            }
        }
        initMatrix();
        vertexShader = ShaderUtils.getShaderFromAssets(context, "shader/vertex_sticker.glsl");
        fragmentShader = ShaderUtils.getShaderFromAssets(context, "shader/fragment_sticker.glsl");
        Log.e("debug: ", "vertexShader:\n"+vertexShader);
        Log.e("debug:", "fragmentShader:\n"+fragmentShader);
    }

    private int width;
    private int height;
    private boolean rotate270;
    public void iniFrame(){
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
        uTextureSamplerHandle=GLES20.glGetUniformLocation(programId,"sTexture");
        aTextureCoordHandle=GLES20.glGetAttribLocation(programId,"aTexCoord");
        vPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix");

        textures = new int[1];
        GLES20.glGenTextures(1, textures, 0); // 生成纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[0]); // 绑定纹理
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);//设置纹理参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,width,height,0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null); // 设置FBO分配内存大小
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0); // 操作完成后解绑纹理

        frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1,frameBuffers,0); // 生成FrameBuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,frameBuffers[0]); //绑定FrameBuffer
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textures[0], 0); // 绑定FrameBuffer 后的绘制会绘制到textures上
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0); // 操作完成之后解绑FBO
    }

    public void drawFrame(){
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT); // 清除预设值的缓冲区;
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        float[] points;
        for(Face r: faces){
            points = new float[106*2];
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
            for(int i =0;i<mStickerLoaderList.size();i++){
                mStickerLoaderList.get(i).updateStickerTexture(this.context);
                calculateVertex(mStickerLoaderList.get(i).getStickerData(), points, r);
                drawFrameBuffer(mStickerLoaderList.get(i).getStickerTexture());
            }
        }
    }

    public void drawFrameBuffer(int textureId){
        GLES20.glEnable(GLES20.GL_BLEND); // 启用混合功能，将片元颜色和颜色缓冲区的颜色进行混合
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA); // 定义启用时的混合操作
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,frameBuffers[0]); // 绑定frameBuffer
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(programId); // 使用shader程序

        GLES20.glEnableVertexAttribArray(aPositionHandle); // 允许使用顶点坐标数组
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false,
                8, vertexBuffer); // 将顶点位置数据传入着色器

        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle,2,GLES20.GL_FLOAT,false,8,textureVertexBuffer);
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0); // 选择活动纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId); // 绑定纹理
        GLES20.glUniform1i(uTextureSamplerHandle,0); // 对应纹理第一层

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4); // 画图
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        GLES20.glUseProgram(0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
    }

    public void calculateVertex(FaceStickerJson stickerData, float[] points, Face face){
        float centerX = 0.0f;
        float centerY = 0.0f;
        if(stickerData.centerIndexList == null){
            return;
        }

        for(int i=0;i<stickerData.centerIndexList.length;i++){
            centerX += (points[stickerData.centerIndexList[i]*2]);
            centerY += (points[stickerData.centerIndexList[i]*2+1]);
        }
        centerX /= (float) stickerData.centerIndexList.length;
        centerY /= (float) stickerData.centerIndexList.length;
        Log.e("debug", "centerX: "+centerX+" centerY: "+centerY);
        float stickerWidth = (float) getDistance(
                points[stickerData.startIndex*2],
                points[stickerData.startIndex*2+1],
                points[stickerData.endIndex*2],
                points[stickerData.endIndex*2+1]
        );
        float ndcStickerWidth = stickerWidth * PROJECTION_SCALE;
        float ndcStickerHeight = ndcStickerWidth * (float) stickerData.height / (float) stickerData.width;

        float offsetX = (stickerWidth * stickerData.offsetX) * PROJECTION_SCALE;
        float stickerHeight = stickerWidth*(float) stickerData.height / (float) stickerData.width;
        float offsetY = (stickerHeight * stickerData.offsetY)  * PROJECTION_SCALE;

        float anchorX = centerX + offsetX * PROJECTION_SCALE;
        float anchorY = centerY + offsetY * PROJECTION_SCALE;

        Log.e("debug", "anchorX: "+(anchorX)+" anchorY: "+(anchorY) + "  ndcStickerWidth: "+(ndcStickerWidth));
        Log.e("debug", "offsetX: "+offsetX+"  offsetY: "+offsetY);
        mStickerVertices[0] = anchorX + ndcStickerWidth;
        mStickerVertices[1] = anchorY - ndcStickerHeight;
        mStickerVertices[2] = anchorX - ndcStickerWidth;
        mStickerVertices[3] = anchorY - ndcStickerHeight;
        mStickerVertices[4] = anchorX + ndcStickerWidth;
        mStickerVertices[5] = anchorY + ndcStickerHeight;
        mStickerVertices[6] = anchorX - ndcStickerWidth;
        mStickerVertices[7] = anchorY + ndcStickerHeight;
        setPoints(mStickerVertices);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, centerX, centerY, 0);

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
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mModelMatrix, 0);
    }

    public void initMatrix(){
        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    public void release(){
        GLES20.glDeleteTextures(1,textures,0);
        for(int i=0;i<mStickerLoaderList.size();i++){
            int[] textureId = {mStickerLoaderList.get(i).getStickerTexture()};
            GLES20.glDeleteTextures(1, textureId, 0);
        }
        GLES20.glDeleteFramebuffers(1,frameBuffers,0);
        GLES20.glDeleteProgram(programId);
    }

    public void setPoints(float[] points){
        vertexBuffer.rewind();
        vertexBuffer.put(points);
        vertexBuffer.position(0);
    }

    public void setFace(List<Face> faces){
        this.faces = faces;
    }

    private double getDistance(float x1, float y1, float x2, float y2){
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }

    public int getTexture(){
        return textures[0];
    }

    public void inputSizeChanged(int width, int height, boolean rotate270){
        this.width = width;
        this.height = height;
        this.rotate270 = rotate270;
        mRatio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -mRatio, mRatio, -1.0f, 1.0f, 3.0f, 9.0f);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
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
}
