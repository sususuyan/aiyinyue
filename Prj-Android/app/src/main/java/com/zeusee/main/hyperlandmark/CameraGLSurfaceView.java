package com.zeusee.main.hyperlandmark;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.zeusee.main.hyperlandmark.jni.Face;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @ClassName CameraGLSurfaceView
 * @Description TODO
 * @Author : kevin
 * @Date 2021/02/02 20:06
 * @Version 1.0
 */
public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener{

    public CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private CameraProxy mCameraProxy;
    private SurfaceTexture mSurfaceTexture;
    private int previewWidth = 0;
    private int previewHeight = 0;

    private GLFramebuffer mFramebuffer;
    private GLFrame mFrame;
    private GLPoints mPoints;
    private Sticker3D sticker3D;
    private Sticker2D sticker2D;
    private StickerObj stickerObj;
    private int stickerType ; // 1:2D 2:3D
    private final int  STICKER_2D = 1;
    private final int STICKER_3D = 2;

    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] STMatrix = new float[16];
    private Context mContext;
    public static int height = 480;
    public static int width = 640;
    boolean isFace = false;

    private void init(Context context){
        mContext = context;
        mCameraProxy = new CameraProxy((Activity) context);
        setEGLContextClientVersion(2);
        setRenderer(this);
        stickerType = STICKER_2D;
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        InitModelFiles();
        FaceTracking.getInstance().FaceTrackingInit(FileUtil.modelPath + "/models", height, width);
        stickerObj = new StickerObj(context);
        stickerObj.loadModel("3DModel/hat/hat.obj");
        sticker2D = new Sticker2D(mContext);
        sticker3D = new Sticker3D(context);
        sticker3D.setStickerName("pikachu");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFramebuffer = new GLFramebuffer();
        mFramebuffer.initFramebuffer();
        mSurfaceTexture = mFramebuffer.getSurfaceTexture();
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mCameraProxy.openCamera();
        mFrame = new GLFrame();
        mPoints = new GLPoints();
        sticker2D.setStickerName("cat");
        stickerObj.onCreate();
        sticker3D.onCreate();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        previewWidth = CameraOverlap.PREVIEW_WIDTH;
        previewHeight = CameraOverlap.PREVIEW_HEIGHT;
//        mFramebuffer.initFramebuffer();
        mFrame.initFrame();
        mFrame.setSize(width, height, previewHeight, previewWidth);
        mPoints.initPoints();
        boolean rotate270 = mCameraProxy.getOrientation() == 270;
        sticker2D.inputSizeChanged(previewHeight, previewWidth, rotate270);
        sticker2D.iniFrame();

        mCameraProxy.startPreview(mSurfaceTexture);
        stickerObj.sizeChange(width, height, rotate270);
        stickerObj.initFrame();
        sticker3D.sizeChange(width, height, rotate270);
        sticker3D.initFrame();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mCameraProxy.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                final Camera.Size previewSize = camera.getParameters().getPreviewSize();

                long start = System.currentTimeMillis();
                FaceTracking.getInstance().Update(data, previewSize.height, previewSize.width);
                Log.e("TAG", "====用时=====" + (System.currentTimeMillis() - start));
                List<Face> faceActions = FaceTracking.getInstance().getTrackingInfo();
                if(faceActions.size()>0){
                    isFace = true;
                }else {
                    isFace = false;
                }
                if(isFace){
                    mPoints.setFace(faceActions);
                    sticker2D.setFace(faceActions);
//                    sticker3D.setFaces(faceActions);
                    stickerObj.setFaces(faceActions);
                }
            }

        });
        int tid = 0;
        if(isFace){
            if(stickerType == STICKER_2D){
                tid = sticker2D.drawFrame();
            }
            else if(stickerType == STICKER_3D){
                tid = stickerObj.drawFrame();
            }
        }
        Log.e("debug", "draw test");
        mFrame.drawFrame(tid, mFramebuffer.drawFrameBuffer(), mFramebuffer.getMatrix());
//        sticker3D.drawFrame();
        if(isFace){
            mPoints.drawPoints();
        }
    }

    void InitModelFiles(){
        String assetPath = "ZeuseesFaceTracking";
        String sdcardPath = Environment.getExternalStorageDirectory()
                + File.separator + assetPath;
        FileUtil.copyFilesFromAssets(mContext, assetPath, sdcardPath);
    }

    public void setStickerType(int stickerType){
        this.stickerType = stickerType;
    }

    public void loadSticker2D(String stickerName){
        sticker2D.setStickerName(stickerName);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }
}
