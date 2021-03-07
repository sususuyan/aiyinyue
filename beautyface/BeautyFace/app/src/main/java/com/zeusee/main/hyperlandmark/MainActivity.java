package com.zeusee.main.hyperlandmark;
//-*-coding:utf-8 -*-
//by 陈金鹏 2021.3.7
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.zeusee.main.hyperlandmark.jni.Face;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;
import com.zeusee.main.hyperlandmark.utils.OpenGLUtils;


import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
//Copyright Hyperlandmark
//
//Licensed under the Apache License, Version 2.0(the"License")
//you may not use this file except in compliance with the Lincense.
//You may obtain  a copy  of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by application law or agreed to in writing, software
//distributed under the Lincense is distributed on an "AS IS"  BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

public class MainActivity extends AppCompatActivity {
    void InitModelFiles() {

        String assetPath = "ZeuseesFaceTracking";
        String sdcardPath = Environment.getExternalStorageDirectory()
                + File.separator + assetPath;

        Log.d("sdcardPath：", sdcardPath);
        Log.d("assetPath：", assetPath);
        FileUtil.copyFilesFromAssets(this, assetPath, sdcardPath);

    }


    private String[] denied;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    public static int height = 480;
    public static int width = 640;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("Tag1", "1");
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (PermissionChecker.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    Log.d("Tag6", "6");
                    list.add(permissions[i]);
                }
            }
            if (list.size() != 0) {
                Log.d("Tag3", "3");
                denied = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    denied[i] = list.get(i);
                }
                ActivityCompat.requestPermissions(this, denied, 5);
            } else {
                Log.d("Tag4", "4");
                init();
            }
        } else {
            Log.d("Tag2", "2");
            init();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 5) {
            boolean isDenied = false;
            for (int i = 0; i < denied.length; i++) {
                String permission = denied[i];
                for (int j = 0; j < permissions.length; j++) {
                    if (permissions[j].equals(permission)) {
                        if (grantResults[j] != PackageManager.PERMISSION_GRANTED) {
                            isDenied = true;
                            break;
                        }
                    }
                }
            }
            if (isDenied) {
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Tag5", "5");
                init();

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private byte[] mNv21Data;
    private CameraOverlap cameraOverlap;
    private final Object lockObj = new Object();
    private boolean isLandmark=false;

    private SurfaceView mSurfaceView;

    private EGLUtils mEglUtils;
    private GLFramebuffer mFramebuffer;

    //private GLFrame mFrame;
    private GLBeautyadjust madjust;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    private GLPoints mPoints;
    private Context mcontext;
    // private GLBitmap mBitmap;
    private BeautyParam mBeauty;
    // 输入OES纹理
    private int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;

    private SeekBar seekBarA;//大眼
    private SeekBar seekBarB;//瘦颧骨
    private SeekBar seekBarC;//缩下巴
    private SeekBar seekBarD;//削脸
    private SeekBar seekBarE;//瘦鼻
    private Switch aSwitch;//关键点显示控制

    private TextView textViewA;//大眼
    private TextView textViewB;//瘦颧骨
    private TextView textViewC;//缩下巴
    private TextView textViewD;//削脸
    private TextView textViewE;//瘦鼻

    private void init() {
        InitModelFiles();
        Log.d("myTag", FileUtil.modelPath);
        FaceTracking.getInstance().FaceTrackingInit(FileUtil.modelPath + "/models", height, width);

        cameraOverlap = new CameraOverlap(this);
        mNv21Data = new byte[CameraOverlap.PREVIEW_WIDTH * CameraOverlap.PREVIEW_HEIGHT * 2];
        mFramebuffer = new GLFramebuffer();

        //mFrame = new GLFrame(this);
        madjust = new GLBeautyadjust(this);

        mcontext = this;
        mPoints = new GLPoints();
        mBeauty=new BeautyParam();
        //mBitmap = new GLBitmap(this, R.drawable.ic_logo);
        mHandlerThread = new HandlerThread("DrawFacePointsThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        cameraOverlap.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] data, Camera camera) {

                final Camera.Size previewSize = camera.getParameters().getPreviewSize();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEglUtils == null) {
                            return;
                        }
                        //mFrame.setI(seekBarA.getProgress()/seekBarA.getMax());

                        setBeautyParam();
                        //定义美型参数
                        //mFrame.onBeauty(mBeauty);
                        //定义美颜参数
                        madjust.onBeauty(mBeauty);

                        isLandmark=aSwitch.isChecked();
                        bindViews();

                        long start = System.currentTimeMillis();

                        FaceTracking.getInstance().Update(data, previewSize.height, previewSize.width);


                        // Log.e("TAG", "====用时=====" + (System.currentTimeMillis() - start));

                        boolean rotate270 = cameraOverlap.getOrientation() == 270;

                        List<Face> faceActions = FaceTracking.getInstance().getTrackingInfo();
                        float[][] eyes = null;
                        float[] points = null;
                        for (Face r : faceActions) {
                            points = new float[106 * 2];
                            eyes = new float[2][2];
                            for (int i = 0; i < 106; i++) {
                                float x;
                                if (rotate270) {
                                    x = r.landmarks[i * 2] * CameraOverlap.SCALLE_FACTOR;
                                } else {
                                    x = CameraOverlap.PREVIEW_HEIGHT - r.landmarks[i * 2];
                                }
                                float y = r.landmarks[i * 2 + 1] * CameraOverlap.SCALLE_FACTOR;
                                points[i * 2] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                                points[i * 2 + 1] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);

                                //Log.e("i", String.valueOf(i));
                                //Log.e("points1:", "("+String.valueOf(points[i*2])+","+String.valueOf(points[i*2+1])+")");
                                if (i == 53) {
                                    eyes[0][0] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                                    eyes[0][1] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                                }
                                if (i == 56) {
                                    eyes[1][0] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                                    eyes[1][1] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                                }
                            }
                            //mFrame.setpoints(points);

                        }

                        madjust.onBeauty(mBeauty);
                        madjust.setUSMIntensity(mBeauty.faceLift);
                        madjust.initProgramHandle();
                        //mFrame.drawFrame(mFramebuffer.drawFrameBuffer(), mFramebuffer.getMatrix());
                        madjust.drawFrame(mFramebuffer.drawFrameBuffer(),mFramebuffer.getMatrix());
                        if (points != null&&isLandmark) {
                            mPoints.setPoints(points);
                            mPoints.drawPoints();
                        }
                        mEglUtils.swap();
                    }
                });
            }
        });

        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(final SurfaceHolder holder, int format, final int width, final int height) {
                //Log.d("=============", "surfaceChanged");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEglUtils != null) {
                            mEglUtils.release();
                        }
                        mEglUtils = new EGLUtils();
                        mEglUtils.initEGL(holder.getSurface());
                        mFramebuffer.initFramebuffer();
                        //mFrame.initFrame();
                        madjust.initProgramHandle();
                        //madjust.initFilters();

                        //mFrame.setSize(width, height, CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
                        madjust.onInputSizeChanged(CameraOverlap.PREVIEW_WIDTH,CameraOverlap.PREVIEW_HEIGHT);
                        madjust.onDisplaySizeChanged( width,height);
                        mPoints.initPoints();
                        //mBitmap.initFrame(CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
                        cameraOverlap.openCamera(mFramebuffer.getSurfaceTexture());
                    }
                });

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cameraOverlap.release();
                        mFramebuffer.release();
                        //mFrame.release();
                        madjust.release();
                        mPoints.release();
                        //mBitmap.release();
                        if (mEglUtils != null) {
                            mEglUtils.release();
                            mEglUtils = null;
                        }
                    }
                });

            }
        });
        if (mSurfaceView.getHolder().getSurface() != null && mSurfaceView.getWidth() > 0) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mEglUtils != null) {
                        mEglUtils.release();
                    }
                    mEglUtils = new EGLUtils();
                    mEglUtils.initEGL(mSurfaceView.getHolder().getSurface());
                    mFramebuffer.initFramebuffer();
                    //mFrame.initFrame();
                    madjust.initProgramHandle();
                    //mFrame.setSize(mSurfaceView.getWidth(), mSurfaceView.getHeight(), CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
                    madjust.onInputSizeChanged(mSurfaceView.getWidth(),mSurfaceView.getHeight());
                    madjust.onDisplaySizeChanged(CameraOverlap.PREVIEW_WIDTH, CameraOverlap.PREVIEW_HEIGHT );
                    mPoints.initPoints();
                    //mBitmap.initFrame(CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);etSurfaceTexture());
                }
            });
        }
        seekBarA = findViewById(R.id.seek_bar_a);
        seekBarB = findViewById(R.id.seek_bar_b);
        //seekBarC = findViewById(R.id.seek_bar_c);
        seekBarD = findViewById(R.id.seek_bar_d);
        //seekBarE = findViewById(R.id.seek_bar_e);
        aSwitch=findViewById(R.id.switch1);

        textViewA =findViewById(R.id.text_view_a);
        textViewB =findViewById(R.id.text_view_b);
        //textViewC =findViewById(R.id.text_view_c);
        textViewD =findViewById(R.id.text_view_d);
        //textViewE =findViewById(R.id.text_view_e);

    }

    private float view2openglX(float x, int width) {//计算opengl对应点x坐标
        float centerX = width / 2.0f;
        float t = x - centerX;
        return t / centerX;
    }

    private float view2openglY(float y, int height) {//计算opengl对应点y坐标
        float centerY = height / 2.0f;
        float s = centerY - y;
        return s / centerY;
    }
    private void setBeautyParam(){
        //mBeauty.eyeEnlargeIntensity=seekBarA.getProgress()/(seekBarA.getMax()*1.0f);// 大眼 0.0f ~ 1.0f
        mBeauty.faceLift=seekBarB.getProgress()/(seekBarB.getMax()*1.0f);// 瘦脸程度 0.0 ~ 1.0f
        //mBeauty.chinIntensity=(seekBarC.getProgress()-50)/(seekBarC.getMax()*1.0f);// 下巴-1.0f ~ 1.0f
        mBeauty.complexionIntensity=seekBarD.getProgress()/(seekBarD.getMax()*1.0f);// 美白程度 0.0 ~ 1.0f
        //mBeauty.noseThinIntensity=seekBarE.getProgress()/(seekBarE.getMax()*1.0f);// 瘦鼻 0.0 ~ 1.0f
        mBeauty.beautyIntensity = seekBarA.getProgress()/(seekBarA.getMax()*1.0f);//磨皮 0.0 ~ 1.0f
    }

    private void bindViews() {
        seekBarA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //textViewA.setText("大眼:" + (progress));
                 textViewA.setText("磨皮:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBarB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewB.setText("锐化:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
//        seekBarC.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                textViewC.setText("下巴:" + (progress-50));
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
        seekBarD.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewD.setText("美白:" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

//        seekBarE.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                textViewE.setText("瘦鼻:" + progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
    }

}
