package com.zeusee.main.hyperlandmark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @ClassName FaceStickerLoader
 * @Description TODO
 * @Author : kevin
 * @Date 2021/02/03 16:18
 * @Version 1.0
 */
public class FaceStickerLoader {
    private static final String TAG = "FaceStickerLoader";
    private int mStickerTexture;

    // texture id
    private int mRestoreTexture;
    // faceSticker path
    private String mFolderPath;
    // faceSticker data
    private FaceStickerJson mStickerData;
    private ResourceDecoder resourceDecoder;
    private int mFrameIndex = -1;
    private long mCurrentTime = -1L;
    private Context mContext;
    private ArrayList<Bitmap> bitmaps;

    public FaceStickerLoader(FaceStickerJson stickerJson, String folderPath, Context context){
        mContext = context;
        mFolderPath = folderPath.startsWith("file://")? folderPath.substring("file://".length()) : folderPath;
        mStickerData = stickerJson;
        Pair pair = ResourceDecoder.getResourceFile(mFolderPath);
        if(pair != null){
            resourceDecoder = new ResourceDecoder(mFolderPath+"/"+String.valueOf(pair.second));
        }
        if(resourceDecoder != null){
            try{
                resourceDecoder.init();
            } catch (IOException e){
                Log.e(TAG, "init merge res reader failed", e);
                resourceDecoder = null;
            }
        }
        mStickerTexture = -1;
        mRestoreTexture = -1;
        bitmaps = new ArrayList<>();
        loadBitmap();
    }

    public void updateStickerTexture(){
        if (mCurrentTime == -1L) {
            mCurrentTime = System.currentTimeMillis();
        }
        int frameIndex = (int) ((System.currentTimeMillis() - mCurrentTime) / mStickerData.duration);
        if (frameIndex >= mStickerData.frames) {
            if (!mStickerData.stickerLooping) {
                mCurrentTime = -1L;
                mRestoreTexture = mStickerTexture;
                mStickerTexture = -1;
                mFrameIndex = -1;
                return;
            }
            frameIndex = 0;
            mCurrentTime = System.currentTimeMillis();
        }
        if (frameIndex < 0) {
            frameIndex = 0;
        }
        if (mFrameIndex == frameIndex) {
            return;
        }
        Bitmap bitmap = null;
        Log.e("debug", "frameIndex: "+frameIndex);
        if(frameIndex>=bitmaps.size()){
            frameIndex = 0;
        }
        bitmap = bitmaps.get(frameIndex);
        if (bitmap != null) {
            if (mStickerTexture == -1 && mRestoreTexture != -1){
                mStickerTexture = mRestoreTexture;
            }
            if (mStickerTexture == -1) {
                mStickerTexture = createTexture(bitmap);
            }
            else {
                mStickerTexture = createTexture(bitmap, mRestoreTexture);
            }
            mRestoreTexture = mStickerTexture;
            mFrameIndex = frameIndex;
            bitmap.recycle();
        } else {
            mRestoreTexture = mStickerTexture;
            mStickerTexture = -1;
            mFrameIndex = -1;
        }
    }

    public int getStickerTexture(){
        return mStickerTexture;
    }

    public int getMaxCount(){
        return mStickerData == null? 0 : mStickerData.maxCount;
    }

    public FaceStickerJson getStickerData() {
        return mStickerData;
    }

    public int createTexture(Bitmap bitmap){
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR); // 对纹理进行设置 三个参数为：活动纹理的目标、纹理参数的标记名、设置的值
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,bitmap,0); // 定义一个二维纹理映射 参数：纹理目标、纹理的级别、图像、边框
        return textures[0];
    }

    public int createTexture(Bitmap bitmap, int texture){
        int[] result = new int[1];
        if(texture == -1){
            result[0] = createTexture(bitmap);
        }
        else {
            result[0] = texture;
            if(bitmap != null && !bitmap.isRecycled()){
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, result[0]);
                GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
            }
        }
        return result[0];
    }

    public void release(){
        if(mStickerTexture == -1){
            mStickerTexture = mRestoreTexture;
        }
        int[] textures = new int[1];
        textures[0] = mStickerTexture;
        GLES20.glDeleteTextures(1, textures, 0);
        mStickerTexture = -1;
        mRestoreTexture = -1;
    }

    private void loadBitmap(){
        int length = mStickerData.frames;
        for(int i=0;i<length;i++){
            String path = String.format(Locale.ENGLISH, mStickerData.stickerName+"_%03d.png", new Object[] {i});
            Bitmap bitmap = null;
            Log.e(TAG, "path: "+mFolderPath+"/"+path);
            try{
                InputStream bit = mContext.getAssets().open(mFolderPath+"/"+path);
                bitmap = BitmapFactory.decodeStream(bit);
            }catch (IOException e){
                Log.d(TAG, "IOException: ");
            }
            if(bitmap !=null){
                bitmaps.add(bitmap);
            }
        }
    }
}
