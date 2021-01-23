package com.zeusee.main.hyperlandmark;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.*;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
/*若出现androidx包迁移问题，可将以上四行import代码改为以下四行代码
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.appcompat.app.AppCompatActivity;
*/
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.zeusee.main.hyperlandmark.jni.Face;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    void InitModelFiles() {//将模型文件存储到本地sdcard中

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
        setContentView(R.layout.face_change);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Log.d("Tag1", "1");
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (PermissionChecker.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    //Log.d("Tag6", "6");
                    list.add(permissions[i]);
                }
            }
            if (list.size() != 0) {
                //Log.d("Tag3", "3");
                denied = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    denied[i] = list.get(i);
                }
                ActivityCompat.requestPermissions(this, denied, 5);
            } else {
                //Log.d("Tag4", "4");
                init();
            }
        } else {
            //Log.d("Tag2", "2");
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
                //Log.d("Tag5", "5");
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
    private GLFrame mFrame;
    private BeautyParam mBeauty;




    private GLPoints mPoints;


    private SeekBar seekBarA;//大眼
    private SeekBar seekBarB;//瘦颧骨
    private SeekBar seekBarC;//缩下巴
    private SeekBar seekBarD;//削脸
    private SeekBar seekBarE;//瘦鼻
    private SeekBar seekBarF;//嘴型
    private SeekBar seekBarG;//额头
    private SeekBar seekBarH;//人中


    private Switch aSwitch;//关键点显示控制

    private TextView textViewA;//大眼
    private TextView textViewB;//瘦颧骨
    private TextView textViewC;//缩下巴
    private TextView textViewD;//削脸
    private TextView textViewE;//瘦鼻
    private TextView textViewF;//嘴型
    private TextView textViewG;//额头
    private TextView textViewH;//人中

    ArrayList ms=new ArrayList();
    long sum=0;

    private void init() {
        InitModelFiles();
        //Log.d("myTag", FileUtil.modelPath);


        cameraOverlap = new CameraOverlap(this);
        // 根据数据长宽初始化模型
        FaceTracking.getInstance().FaceTrackingInit(FileUtil.modelPath + "/models", height, width);
        mNv21Data = new byte[CameraOverlap.PREVIEW_WIDTH * CameraOverlap.PREVIEW_HEIGHT * 2];

        mFramebuffer = new GLFramebuffer();
        mFrame = new GLFrame(this);
        mBeauty=new BeautyParam();


        mPoints = new GLPoints();

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
                        //根据seekbar设置美型参数
                        setBeautyParam();
                        //传入美型参数
                        mFrame.onBeauty(mBeauty);

                        isLandmark=aSwitch.isChecked();
                        bindViews();

                        long start = System.currentTimeMillis();
                        //检测帧数据并保存检测数据
                        FaceTracking.getInstance().Update(data, previewSize.height, previewSize.width);

                        long last=System.currentTimeMillis() - start;
                        if(FaceTracking.getInstance().getTrackingInfo().size()>0){

                            ms.add(last);
                            sum=sum+last;

                            long avg=sum/ms.size();
                            //Log.e("TAG", "====用时=====" + (System.currentTimeMillis() - start));
                            Log.e("TAG", "====用时avg=====" + avg);
                        }


                        boolean rotate270 = cameraOverlap.getOrientation() == 270;
                        //  获取封装脸部数据
                        List<Face> faceActions = FaceTracking.getInstance().getTrackingInfo();
                        //float[][] eyes = null;
                        float[] points = null;
                        float[] epoints =null;
                        for (Face r : faceActions) {
                            points = new float[106 * 2];
                            epoints = new float[109 * 2];
                           // eyes = new float[2][2];
                            for (int i = 0; i < 106; i++) {
                                float x;
                                if (rotate270) {
                                    x = r.landmarks[i * 2] * CameraOverlap.SCALLE_FACTOR;
                                } else {
                                    x = CameraOverlap.PREVIEW_HEIGHT - r.landmarks[i * 2];
                                }
                                float y = r.landmarks[i * 2 + 1] * CameraOverlap.SCALLE_FACTOR;
                                points[i * 2] = view2openglX(x, previewSize.height);
                                points[i * 2 + 1] = view2openglY(y, previewSize.width);
                            }
                            //计算额头三个点保存
                            epoints=FaceTracking.getInstance().calculateExtraFacePoints(points);
                            //点传入渲染类
                            mFrame.setpoints(epoints);
                        }

                        mFrame.drawFrame(mFramebuffer.drawFrameBuffer(), mFramebuffer.getMatrix());
                        if (points != null&&isLandmark) {
                            mPoints.setPoints(epoints);
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
                Log.d("=============", "surfaceChanged");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEglUtils != null) {
                            mEglUtils.release();
                        }
                        mEglUtils = new EGLUtils();
                        mEglUtils.initEGL(holder.getSurface());
                        mFramebuffer.initFramebuffer();
                        mFrame.initFrame();
                        mFrame.setSize(width, height, CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
                        mPoints.initPoints();
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
                        mFrame.release();
                        mPoints.release();
                        ms.clear();
                        sum=0;
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
                    mFrame.initFrame();
                    mFrame.setSize(mSurfaceView.getWidth(), mSurfaceView.getHeight(), CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
                    mPoints.initPoints();
                    //mBitmap.initFrame(CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);etSurfaceTexture());
                }
            });
        }
        seekBarA = findViewById(R.id.seek_bar_a);
        seekBarB = findViewById(R.id.seek_bar_b);
        seekBarC = findViewById(R.id.seek_bar_c);
        seekBarD = findViewById(R.id.seek_bar_d);
        seekBarE = findViewById(R.id.seek_bar_e);
        seekBarF = findViewById(R.id.seek_bar_f);
        seekBarG = findViewById(R.id.seek_bar_g);
        seekBarH = findViewById(R.id.seek_bar_h);
        aSwitch=findViewById(R.id.switch1);

        textViewA =findViewById(R.id.text_view_a);
        textViewB =findViewById(R.id.text_view_b);
        textViewC =findViewById(R.id.text_view_c);
        textViewD =findViewById(R.id.text_view_d);
        textViewE =findViewById(R.id.text_view_e);
        textViewF =findViewById(R.id.text_view_f);
        textViewG =findViewById(R.id.text_view_g);
        textViewH =findViewById(R.id.text_view_h);

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
        mBeauty.eyeEnlargeIntensity=seekBarA.getProgress()/(seekBarA.getMax()*1.0f);// 大眼 0.0f ~ 1.0f
        mBeauty.faceLift=seekBarB.getProgress()/(seekBarB.getMax()*1.0f);// 瘦脸程度 0.0 ~ 1.0f
        mBeauty.chinIntensity=(seekBarC.getProgress()-50)/(seekBarC.getMax()*1.0f);// 下巴-1.0f ~ 1.0f
        mBeauty.faceShave=seekBarD.getProgress()/(seekBarD.getMax()*1.0f);// 削脸程度 0.0 ~ 1.0f
        mBeauty.noseThinIntensity=seekBarE.getProgress()/(seekBarE.getMax()*1.0f);// 瘦鼻 0.0 ~ 1.0f
        mBeauty.mouthEnlargeIntensity=(seekBarF.getProgress()-50)/(seekBarF.getMax()*1.0f);// 嘴型-1.0f ~ 1.0f
        mBeauty.foreheadIntensity=(seekBarG.getProgress()-50)/(seekBarG.getMax()*1.0f);// 额头-1.0f ~ 1.0f
        mBeauty.philtrumIntensity=(seekBarH.getProgress()-50)/(seekBarH.getMax()*1.0f);// 人中-1.0f ~ 1.0f
    }

    private void bindViews() {
        seekBarA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewA.setText("大眼：" + progress);
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
                textViewB.setText("瘦脸：" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBarC.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewC.setText("下巴：" + (progress-50));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBarD.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewD.setText("削脸：" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarE.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewE.setText("瘦鼻：" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarF.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewF.setText("嘴型：" + (progress-50));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewG.setText("额头：" + (progress-50));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewH.setText("人中：" + (progress-50));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    public void doReset(View view) {
        seekBarA.setProgress(30);
        seekBarB.setProgress(30);
        seekBarC.setProgress(50);
        seekBarD.setProgress(30);
        seekBarE.setProgress(30);
        seekBarF.setProgress(50);
        seekBarG.setProgress(50);
        seekBarH.setProgress(50);
    }
}
