package com.afei.camerademo.glsurfaceview;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.afei.camerademo.R;
import com.afei.camerademo.camera.CameraProxy;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "CameraGLSurfaceView";
    private CameraProxy mCameraProxy;
    private SurfaceTexture mSurfaceTexture;
    private CameraDrawer mDrawer;
    //private CameraFilter mFilter;
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private float mOldDistance;
    private int mTextureId = -1;

    private int previewWidth = 0;
    private int previewHeight = 0;

    private int Filter = 0;
    private int FilterImage;

    private float strength = 0.5f;

    private boolean takePicture = false;

    private Context mContext;

    public CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        mContext = context;
        mCameraProxy = new CameraProxy((Activity) context);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mTextureId = OpenGLUtils.getExternalOESTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mCameraProxy.openCamera();
        mDrawer = new CameraDrawer(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged. thread: " + Thread.currentThread().getName());
        Log.d(TAG, "onSurfaceChanged. width: " + width + ", height: " + height);
        previewWidth = mCameraProxy.getPreviewWidth();
        previewHeight = mCameraProxy.getPreviewHeight();
        if (width > height) {
            setAspectRatio(previewWidth, previewHeight);
        } else {
            setAspectRatio(previewHeight, previewWidth);
        }
        GLES20.glViewport(0, 0, width, height);
        mCameraProxy.startPreview(mSurfaceTexture);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        FilterImage = getFilter();
        mSurfaceTexture.updateTexImage();
        mDrawer.draw(mTextureId, mCameraProxy.isFrontCamera(), mContext, previewWidth, previewHeight, FilterImage, strength, takePicture);
        takePicture = false;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    private void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        //mRatioWidth = width;
        //mRatioHeight = height;
        previewWidth = width;
        previewHeight = height;
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout(); // must run in UI thread
            }
        });
    }

    public CameraProxy getCameraProxy() {
        return mCameraProxy;
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        /*
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }

         */
        if (0 == previewWidth || 0 == previewHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * previewWidth / previewHeight) {
                setMeasuredDimension(width, width * previewHeight / previewWidth);
            } else {
                setMeasuredDimension(height * previewWidth / previewHeight, height);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            // 点击聚焦
            mCameraProxy.focusOnPoint((int) event.getX(), (int) event.getY(), getWidth(), getHeight());
            return true;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                mOldDistance = getFingerSpacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                float newDistance = getFingerSpacing(event);
                if (newDistance > mOldDistance) {
                    mCameraProxy.handleZoom(true);
                } else if (newDistance < mOldDistance) {
                    mCameraProxy.handleZoom(false);
                }
                mOldDistance = newDistance;
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public void setFilter(){
        Filter = (Filter+1) % 12 ;
    }

    public int getFilter(){
        switch (Filter){
            case 0:
                //清新
                return R.drawable.slowlived;
            case 1:
                //黑白
                return R.drawable.heibai;
            case 2:
                //风景-鲜艳
                return R.drawable.hadean;
            case 3:
                //复古
                return R.drawable.fugu;
            case 4:
                //白皙
                return R.drawable.baixi;
            case 5:
                //初恋
                return R.drawable.chulian;
            case 6:
                //过往
                return R.drawable.guowang;
            case 7:
                //泡沫
                return R.drawable.paomose;
            case 8:
                //冷艳
                return R.drawable.lengyanruihua;
            case 9:
                //暖色
                return R.drawable.warm;
            case 10:
                //自然
                return R.drawable.mengjing;
            case 11:
                //富士山
                return R.drawable.fushi;
        }
        return R.drawable.slowlived;
    }

    public void setStrength(float num){
        strength = num;
    }

    public void setTakePicture(){
        takePicture = true;
    }

}
