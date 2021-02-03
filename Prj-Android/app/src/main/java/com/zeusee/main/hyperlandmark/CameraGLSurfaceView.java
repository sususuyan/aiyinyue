package com.zeusee.main.hyperlandmark;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

import com.zeusee.main.hyperlandmark.jni.Face;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;

import java.io.File;
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
public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer{

    public CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private CameraOverlap cameraOverlap;
    private SurfaceView mSurfaceView;
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private int previewWidth = 0;
    private int previewHeight = 0;

    private GLFramebuffer mFramebuffer;
    private GLFrame mFrame;
    private GLPoints mPoints;
    private Cube cube;
    private Sticker3D sticker3D;
    private Sticker2D sticker2D;
    private int stickerType; // 1:2D 2:3D
    private final int  STICKER_2D = 1;
    private final int STICKER_3D = 2;

    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private Context mContext;
    public static int height = 480;
    public static int width = 640;
    float[] points = new float[106*2];
    boolean isFace = false;

    private void init(Context context){
        mContext = context;
        cameraOverlap = new CameraOverlap(context);
        setEGLContextClientVersion(2);
        setRenderer(this);
        stickerType = 1;
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        InitModelFiles();
        FaceTracking.getInstance().FaceTrackingInit(FileUtil.modelPath + "/models", height, width);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFramebuffer = new GLFramebuffer();
        mFrame = new GLFrame();
        mPoints = new GLPoints();
        cube = new Cube();
        sticker3D = new Sticker3D();
        sticker2D = new Sticker2D(mContext, "stickers");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //计算宽高比
        float ratio=(float)width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //Matrix.perspectiveM(mProjectMatrix, 0, 45, (float)width/height, 2, 5);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 10.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
        previewWidth = CameraOverlap.PREVIEW_WIDTH;
        previewHeight = CameraOverlap.PREVIEW_HEIGHT;
        mFramebuffer.initFramebuffer();
        mFrame.initFrame();
        mFrame.setSize(width, height, previewHeight, previewWidth);
        mPoints.initPoints();
        cube.sizeChange(width, height);
        sticker3D.inputSizeChanged(previewHeight, previewWidth, true);
        sticker3D.initFrame();
        sticker2D.inputSizeChanged(previewHeight, previewWidth, true);
        sticker2D.iniFrame();
        cameraOverlap.openCamera(mFramebuffer.getSurfaceTexture());
        Log.e("debug", "surfaceChange");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        cameraOverlap.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                final Camera.Size previewSize = camera.getParameters().getPreviewSize();

                long start = System.currentTimeMillis();
                FaceTracking.getInstance().Update(data, previewSize.height, previewSize.width);
                Log.e("TAG", "====用时=====" + (System.currentTimeMillis() - start));
                boolean rotate270 = cameraOverlap.getOrientation() == 270;
                List<Face> faceActions = FaceTracking.getInstance().getTrackingInfo();
                float[] p = null;
                if(faceActions.size()>0){
                    isFace = true;
                }else {
                    isFace = false;
                }
                sticker2D.setFace(faceActions);
                sticker3D.setFaces(faceActions);
            }
        });
        Log.e("debug", "draw");
        int tid = 0;
        if(isFace){
            if(stickerType == STICKER_2D){
                sticker2D.drawFrame();
                tid = sticker2D.getTexture();
            }
            else if(stickerType == STICKER_3D){
                sticker3D.drawFrame();
                tid = sticker3D.getTextureId();
            }
        }
        mFrame.drawFrame(tid, mFramebuffer.drawFrameBuffer(), mFramebuffer.getMatrix());
//        if(isFace){
//            mPoints.setPoints(points);
//            Log.e("debug", "draw points");
//            mPoints.drawPoints();
//        }

    }

    void InitModelFiles(){
        String assetPath = "ZeuseesFaceTracking";
        String sdcardPath = Environment.getExternalStorageDirectory()
                + File.separator + assetPath;
        FileUtil.copyFilesFromAssets(mContext, assetPath, sdcardPath);
    }
}
