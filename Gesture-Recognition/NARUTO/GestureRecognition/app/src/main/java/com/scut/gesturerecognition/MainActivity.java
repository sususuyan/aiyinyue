package com.scut.gesturerecognition;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    //基本长宽
    int widthSize ;
    int heightSize;
    int widthRealSize ;
    int heightRealSize;

    //手势相关
    Gestures lastGestures =Gestures.UNKNOWN;
    Gestures nowGestures ;

    //按钮相关
    boolean isPlam=false;
    boolean isYeah=false;
    boolean isHeart=false;
    boolean isFight=false;
    boolean isOK=false;

    //指定图片
    private ImageView palmView2;
    private ImageView palmView1;
    private ImageView yeahView;
    private ImageView heartView;
    private ImageView fightView;
    private ImageView okView;
    private ImageView wuView;
    private ImageView chenView;

    //手掌相关
    boolean isFirst=true;
    boolean isBegin=true;

    Matrix palmMatrix2 =new Matrix();
    Matrix palmMatrix1 =new Matrix();
    Matrix firstMatrix=new Matrix();

    PointF palmMark1 = new PointF(0,0);
    PointF palmLastMid = new PointF(0,0);
    PointF palmNowMid = new PointF(0,0);
    PointF palmMark2 = new PointF(0,0);

    float palmOldDist = 1f;
    float allSacle=1f;


    //yeah相关
    Matrix yeahMatrix =new Matrix();

    PointF yeahMark1 = new PointF(0,0);
    PointF yeahLastMid = new PointF(0,0);
    PointF yeahNowMid = new PointF(0,0);
    PointF yeahMark2 = new PointF(0,0);

    float yeahOldDist = 1f;

    //ok相关
    boolean okIsFirst=true;
    Matrix okMatrix =new Matrix();

    PointF okMark1 = new PointF(0,0);
    PointF okLastMid = new PointF(0,0);
    PointF okNowMid = new PointF(0,0);
    PointF okMark2 = new PointF(0,0);

    float okOldDist = 1f;


    //fight相关
    boolean fightIsFirst=true;
    Matrix fightMatrix =new Matrix();

    PointF fightMark1 = new PointF(0,0);
    PointF fightLastMid = new PointF(0,0);
    PointF fightNowMid = new PointF(0,0);
    PointF fightMark2 = new PointF(0,0);

    float fightOldDist = 1f;

    //heart相关
    boolean heartIsFirst=true;
    Matrix heartMatrix =new Matrix();

    PointF heartMark1 = new PointF(0,0);
    PointF heartLastMid = new PointF(0,0);
    PointF heartNowMid = new PointF(0,0);
    PointF heartMark2 = new PointF(0,0);

    float heartOldDist = 1f;



    private float spacing(PointF point1,PointF point2) {
        float x = point1.x-point2.x;
        float y = point1.y-point2.y;
        return (float)Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point,PointF point1,PointF point2) {
        float x = point1.x + point2.x;
        float y = point1.y + point2.y;
        point.set(x / 2, y / 2);
    }




    private static final String TAG = "MainActivity";
    private static final String BINARY_GRAPH_NAME = "hand_tracking_mobile_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    private static final int NUM_HANDS = 2;
    private static final String INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands";

    // 垂直翻转相机预览帧，然后将它们发送到FrameProcessor中以在MediaPipe图形中进行处理，
    // 并在处理后的帧显示时将它们翻转回来。这是必需的，因为OpenGL表示图像，假设图像原点
    // 在左下角，而MediaPipe通常假设图像原点在左上角。
    private static final boolean FLIP_FRAMES_VERTICALLY = true;

    // 加载必要的本地库
    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }

    //
    private static final int NUM_BUFFERS = 2;//在ExternalTextureConverter中分配的输出帧数

    // 访问相机预览帧。
    private SurfaceTexture previewFrameTexture;
    // 显示由 MediaPipe 图形处理的相机预览帧。
    private SurfaceView previewDisplayView;
    // 创建和管理一个 EGL 管理器，是 OpenGL ES 渲染 API 和本地窗口系统之间的一个中间接口层。
    private EglManager eglManager;
    // 将相机预览帧发送到 MediaPipe 图形中进行处理，并将处理后的帧显示到 Surface 上。
    private FrameProcessor processor;
    // 将相机的 GL_TEXTURE_EXTERNAL_OES 纹理转换为 FrameProcessor 和底层 MediaPipe 图形使用的规则纹理。
    private ExternalTextureConverter converter;
    // CameraX 是新增库。利用该库，可以更轻松地向应用添加相机功能。
    private CameraXPreviewHelper cameraHelper;

    List<NormalizedLandmarkList> multiHandLandmarks;
    NormalizedLandmarkList HandLandmarks1;
    NormalizedLandmarkList HandLandmarks2;
//    Iterator<NormalizedLandmarkList>  iteratorOfLandmarks;

    Bitmap bitmap;//计算初始图片位置用

    private GLSurfaceView gLView;


    final Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x1233) {

                if(isBegin){
                    firstMatrix.set(palmView1.getImageMatrix());
                    isBegin=false;
                }

                if(isFirst){
                    allSacle=1f;
                    palmMatrix2.set(firstMatrix);
                    palmMatrix1.set(firstMatrix);
                    palmMatrix1.preTranslate(-9000f,-4500f);
                    palmMatrix2.preTranslate(-6000f,-2000f);
                    palmMatrix1.preScale(4f, 4f);
                    palmMatrix2.preScale(3f, 3f);
                    palmMark1.set(HandLandmarks1.getLandmark(0).getX()*widthSize,HandLandmarks1.getLandmark(0).getY()*heightSize);
                    palmMark2.set(HandLandmarks1.getLandmark(12).getX()*widthSize,HandLandmarks1.getLandmark(12).getY()*heightSize);
                    isFirst=false;
                    midPoint(palmLastMid, palmMark1, palmMark2);
                    palmOldDist =spacing(palmMark1, palmMark2);
                    lastGestures=nowGestures;
                    return;
                }
                palmMark1.set(HandLandmarks1.getLandmark(0).getX()*widthSize,HandLandmarks1.getLandmark(0).getY()*heightSize);
                palmMark2.set(HandLandmarks1.getLandmark(12).getX()*widthSize,HandLandmarks1.getLandmark(12).getY()*heightSize);
                midPoint(palmNowMid, palmMark1, palmMark2);



                //移动
                palmMatrix2.postTranslate(palmNowMid.x- palmLastMid.x, palmNowMid.y- palmLastMid.y);
                palmMatrix1.postTranslate((palmNowMid.x- palmLastMid.x)*2f,(palmNowMid.y- palmLastMid.y)*2f);

                //缩放
                float newDist=spacing(palmMark1, palmMark2);
                float scale=newDist/ palmOldDist;
                if(newDist>10&&(allSacle<2&&allSacle>0.5)){

                    palmMatrix2.postScale((float) Math.pow(scale,0.5f), (float) Math.pow(scale,0.5f), palmNowMid.x, palmNowMid.y);
                    palmMatrix1.postScale((float) Math.pow(scale,0.75f), (float) Math.pow(scale,0.75f), palmNowMid.x, palmNowMid.y);

                }
                allSacle*=scale;

                //输出
                palmView2.setScaleType(ImageView.ScaleType.MATRIX);
                palmView2.setImageMatrix(palmMatrix2);
                palmView1.setScaleType(ImageView.ScaleType.MATRIX);
                palmView1.setImageMatrix(palmMatrix1);

                //完成显示
                palmView2.setVisibility(View.VISIBLE);
                palmView1.setVisibility(View.VISIBLE);

                for (NormalizedLandmark landmark : HandLandmarks1.getLandmarkList()) {
                    if (landmark.getX()<0||landmark.getY()<0||landmark.getX()>1||landmark.getY()>1){
                        palmView2.setVisibility(View.INVISIBLE);
                        palmView2.setImageMatrix(firstMatrix);
                        palmView1.setVisibility(View.INVISIBLE);
                        palmView1.setImageMatrix(firstMatrix);
                        isFirst=true;
                        break;
                    }
                }

                //为下一次做准备
                palmOldDist =newDist;
                palmLastMid.set(palmNowMid);
                lastGestures=nowGestures;
            }
            if (msg.what == 0x1234) {

                yeahMark1.set(HandLandmarks1.getLandmark(8).getX()*widthSize,HandLandmarks1.getLandmark(8).getY()*heightSize);
                yeahMark2.set(HandLandmarks1.getLandmark(12).getX()*widthSize,HandLandmarks1.getLandmark(12).getY()*heightSize);
                midPoint(yeahNowMid,yeahMark1, yeahMark2);


                //TODO 移动
                yeahMatrix.postTranslate(yeahNowMid.x-yeahLastMid.x,yeahNowMid.y-yeahLastMid.y);

                //TODO 缩放
                float yeahNewDist=spacing(yeahMark1, yeahMark2);
                if(yeahNewDist>10){
                    float scale=yeahNewDist/yeahOldDist;
                    yeahMatrix.postScale((float) Math.pow(scale,1f), (float) Math.pow(scale,1f),yeahNowMid.x,yeahNowMid.y);
                }
                

                //输出
                yeahView.setScaleType(ImageView.ScaleType.MATRIX);
                yeahView.setImageMatrix(yeahMatrix);


                for (NormalizedLandmark landmark : HandLandmarks1.getLandmarkList()) {
                    if (landmark.getX()<0||landmark.getY()<0||landmark.getX()>1||landmark.getY()>1){
                        yeahView.setVisibility(View.INVISIBLE);

                        break;
                    }
                }


                //TODO 为下一次做准备
                yeahOldDist=yeahNewDist;
                yeahLastMid.set(yeahNowMid);
                lastGestures=nowGestures;



                //TODO 完成显示，下一步是随手势移动
                yeahView.setVisibility(View.VISIBLE);
            }
            if (msg.what == 0x1235) {

                okMark1.set(HandLandmarks1.getLandmark(6).getX()*widthSize,HandLandmarks1.getLandmark(6).getY()*heightSize);
                okMark2.set(HandLandmarks1.getLandmark(2).getX()*widthSize,HandLandmarks1.getLandmark(2).getY()*heightSize);
                midPoint(okNowMid,okMark1, okMark2);
                //TODO 完成显示，下一步是随手势移动
                okView.setVisibility(View.VISIBLE);


                //TODO 移动
                okMatrix.postTranslate(okNowMid.x-okLastMid.x,okNowMid.y-okLastMid.y);

                //TODO 缩放
                float okNewDist=spacing(okMark1, okMark2);
                if(okNewDist>10){
                    float scale=okNewDist/okOldDist;
                    okMatrix.postScale((float) Math.pow(scale,1f), (float) Math.pow(scale,1f),okNowMid.x,okNowMid.y);
                }


                //输出
                okView.setScaleType(ImageView.ScaleType.MATRIX);
                okView.setImageMatrix(okMatrix);


                for (NormalizedLandmark landmark : HandLandmarks1.getLandmarkList()) {
                    if (landmark.getX()<0||landmark.getY()<0||landmark.getX()>1||landmark.getY()>1){
                        okView.setVisibility(View.INVISIBLE);

                        break;
                    }
                }


                //TODO 为下一次做准备
                okOldDist=okNewDist;
                okLastMid.set(okNowMid);
                lastGestures=nowGestures;
            }
            if (msg.what == 0x1236) {

                fightMark1.set(HandLandmarks1.getLandmark(6).getX()*widthSize,HandLandmarks1.getLandmark(6).getY()*heightSize);
                fightMark2.set(HandLandmarks1.getLandmark(17).getX()*widthSize,HandLandmarks1.getLandmark(17).getY()*heightSize);
                midPoint(fightNowMid,fightMark1, fightMark2);
                //TODO 完成显示，下一步是随手势移动
                fightView.setVisibility(View.VISIBLE);


                //TODO 移动
                fightMatrix.postTranslate(fightNowMid.x-fightLastMid.x,fightNowMid.y-fightLastMid.y);

                //TODO 缩放
                float fightNewDist=spacing(fightMark1, fightMark2);
                if(fightNewDist>10){
                    float scale=fightNewDist/fightOldDist;
                    fightMatrix.postScale((float) Math.pow(scale,1f), (float) Math.pow(scale,1f),fightNowMid.x,fightNowMid.y);
                }


                //输出
                fightView.setScaleType(ImageView.ScaleType.MATRIX);
                fightView.setImageMatrix(fightMatrix);


                for (NormalizedLandmark landmark : HandLandmarks1.getLandmarkList()) {
                    if (landmark.getX()<0||landmark.getY()<0||landmark.getX()>1||landmark.getY()>1){
                        fightView.setVisibility(View.INVISIBLE);

                        break;
                    }
                }


                //TODO 为下一次做准备
                fightOldDist=fightNewDist;
                fightLastMid.set(fightNowMid);
                lastGestures=nowGestures;
            }
            if (msg.what == 0x1237) {

                heartMark1.set(HandLandmarks1.getLandmark(4).getX()*widthSize,HandLandmarks1.getLandmark(4).getY()*heightSize);
                heartMark2.set(HandLandmarks1.getLandmark(8).getX()*widthSize,HandLandmarks1.getLandmark(8).getY()*heightSize);
                midPoint(heartNowMid,heartMark1, heartMark2);
                //TODO 完成显示，下一步是随手势移动
                heartView.setVisibility(View.VISIBLE);


                //TODO 移动
                heartMatrix.postTranslate(heartNowMid.x-heartLastMid.x,heartNowMid.y-heartLastMid.y);

                //TODO 缩放
                float heartNewDist=spacing(heartMark1, heartMark2);
                if(heartNewDist>10){
                    float scale=heartNewDist/heartOldDist;
                    heartMatrix.postScale((float) Math.pow(scale,1f), (float) Math.pow(scale,1f),heartNowMid.x,heartNowMid.y);
                }


                //输出
                heartView.setScaleType(ImageView.ScaleType.MATRIX);
                heartView.setImageMatrix(heartMatrix);


                for (NormalizedLandmark landmark : HandLandmarks1.getLandmarkList()) {
                    if (landmark.getX()<0||landmark.getY()<0||landmark.getX()>1||landmark.getY()>1){
                        heartView.setVisibility(View.INVISIBLE);

                        break;
                    }
                }


                //TODO 为下一次做准备
                heartOldDist=heartNewDist;
                heartLastMid.set(heartNowMid);
                lastGestures=nowGestures;
            }
            if (msg.what == 0x1238) {
                wuView.setVisibility(View.VISIBLE);
            }
            if (msg.what == 0x1239) {
                chenView.setVisibility(View.VISIBLE);
            }
            if (msg.what == 0x1230) {
                palmView2.setVisibility(View.INVISIBLE);
                palmView2.setImageMatrix(firstMatrix);
                palmView1.setVisibility(View.INVISIBLE);
                palmView1.setImageMatrix(firstMatrix);
                yeahView.setVisibility(View.INVISIBLE);
                okView.setVisibility(View.INVISIBLE);
                heartView.setVisibility(View.INVISIBLE);
                fightView.setVisibility(View.INVISIBLE);
                wuView.setVisibility(View.INVISIBLE);
                chenView.setVisibility(View.INVISIBLE);
                isFirst=true;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        viewInit();


        previewDisplayView = new SurfaceView(this);//创建 SurfaceView 界面，主要用作视频输出
        setupPreviewDisplayView();//裁剪预览图形

        // 初始化 asset 管理器，以便 MediaPipe 本地库可以访问应用的 assets 文件夹内容,例如 binary 图
        AndroidAssetUtil.initializeNativeAssetManager(this);
        //创建 EGL 管理器，作为 OpenGL 和原生窗口系统之间的桥梁，控制 GPU 渲染视频
        eglManager = new EglManager(null);

        //创建视频帧处理
        processor =
                new FrameProcessor(
                        this,
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME);
        processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);//垂直翻转帧

        // 获取手的关键点模型输出
        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {

                    Log.d(TAG, "Received multi-hand landmarks packet.");
                    multiHandLandmarks =
                            PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());

                    Log.d(
                            TAG,
                            "[TS:"
                                    + packet.getTimestamp()
                                    + "] "
                                    + getMultiHandLandmarksDebugString(multiHandLandmarks));


                });

        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        Map<String, Packet> inputSidePackets = new HashMap<>();
        inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
        processor.setInputSidePackets(inputSidePackets);
        PermissionHelper.checkAndRequestCameraPermissions(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        converter = new ExternalTextureConverter(eglManager.getContext(),NUM_BUFFERS);//创建图形外部转换器
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);//垂直翻转帧
        converter.setConsumer(processor);//指定处理图形
        if (PermissionHelper.cameraPermissionsGranted(this)) {//如果已获取相机权限
            startCamera();//打开相机
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        converter.close();//停止转换
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // 裁剪预览图形
    private void setupPreviewDisplayView() {
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);
        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                            }

                            // (重新)计算相机预览显示的理想尺寸(相机预览帧渲染到的区域，可能通过缩放和旋转)基于包含显示的表面视图的尺寸。
                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                onPreviewDisplaySurfaceChanged(holder, format, width, height);
                            }


                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }

    private void startCamera() {
        cameraHelper = new CameraXPreviewHelper();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    previewFrameTexture = surfaceTexture;
                    // 使显示视图可见，以开始显示预览。这就触发了SurfaceHolder。回调添加到previewDisplayView。
                    previewDisplayView.setVisibility(View.VISIBLE);
                });
        cameraHelper.startCamera(this, CAMERA_FACING, /*surfaceTexture=*/ null, cameraTargetResolution());
    }

    // 字符串格式化，输出日志，用于调试
    private String getMultiHandLandmarksDebugString(List<NormalizedLandmarkList> multiHandLandmarks) {


        if (multiHandLandmarks.isEmpty()) {
            String noHandLandmark="No hand landmarks\n";
            return noHandLandmark;
        }
        String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
        int handIndex = 0;
        for (NormalizedLandmarkList landmarks :  multiHandLandmarks) {
            multiHandLandmarksStr +=
                    "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
            int landmarkIndex = 0;
            for (NormalizedLandmark landmark : landmarks.getLandmarkList()) {

                multiHandLandmarksStr +=
                        "\t\tLandmark ["
                                + landmarkIndex
                                + "]: ("
                                + landmark.getX()
                                + ", "
                                + landmark.getY()
                                + ", "
                                + landmark.getZ()
                                + ")\n";

                ++landmarkIndex;
            }
            //执行相关操作
            if(multiHandLandmarks.size()==1&&handIndex==0) {
                HandLandmarks1=landmarks;
                nowGestures=HandGestureRecognition.handGestureRecognition(landmarks);
                if(lastGestures!=nowGestures){
                    handler.sendEmptyMessage(0x1230);
                }
                if (nowGestures == Gestures.FIVE&&isPlam) {
                    handler.sendEmptyMessage(0x1233);
                }
                else if(nowGestures == Gestures.YEAH&&isYeah){
                    handler.sendEmptyMessage(0x1234);
                }
                else if(nowGestures == Gestures.OK&&isOK){
                    handler.sendEmptyMessage(0x1235);
                }
                else if(nowGestures == Gestures.FIGHT&&isFight){
                    handler.sendEmptyMessage(0x1236);
                }
                else if(nowGestures == Gestures.FINGER_HEART&&isHeart){
                    handler.sendEmptyMessage(0x1237);
                }
                else if (palmView2.getVisibility() == View.VISIBLE && nowGestures != Gestures.FIVE) {
                    handler.sendEmptyMessage(0x1230);
                }
                else if (yeahView.getVisibility() == View.VISIBLE && nowGestures != Gestures.YEAH) {
                    handler.sendEmptyMessage(0x1230);
                }
                else if (okView.getVisibility() == View.VISIBLE && nowGestures != Gestures.OK) {
                    handler.sendEmptyMessage(0x1230);
                }
                else if (fightView.getVisibility() == View.VISIBLE && nowGestures != Gestures.FIGHT) {
                    handler.sendEmptyMessage(0x1230);
                }
                else if (heartView.getVisibility() == View.VISIBLE && nowGestures != Gestures.FINGER_HEART) {
                    handler.sendEmptyMessage(0x1230);
                }
            }
            else if(multiHandLandmarks.size()==2&&handIndex==0){
                HandLandmarks1=landmarks;
            }
            else if(handIndex==1){
                HandLandmarks2=landmarks;
                nowGestures=(HandLandmarks1.getLandmark(0).getX()>HandLandmarks2.getLandmark(0).getX())?HandGestureRecognition.handGestureRecognition(HandLandmarks1,HandLandmarks2):HandGestureRecognition.handGestureRecognition(HandLandmarks2,HandLandmarks1);
                if(nowGestures==Gestures.WU){
                    handler.sendEmptyMessage(0x1238);
                }
                else if(nowGestures==Gestures.CHEN){
                    handler.sendEmptyMessage(0x1239);
                }
                else
                    handler.sendEmptyMessage(0x1230);
            }
            ++handIndex;
        }

//        return multiHandLandmarksStr;
        return Float.toString(allSacle);
    }
    // 设置相机大小
    protected Size cameraTargetResolution() {
        return null;
    }
    // 计算最佳的预览大小
    protected Size computeViewSize(int width, int height) {
        return new Size(width, height);
    }

    protected void onPreviewDisplaySurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 设置预览大小
        Size viewSize = computeViewSize(width, height);
        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
        // 根据是否旋转调整预览图像大小
        boolean isCameraRotated = cameraHelper.isCameraRotated();

        // 将转换器连接到相机预览帧作为其输入(通过previewFrameTexture)，
        // 并将输出宽度和高度配置为计算出的显示大小。
        converter.setSurfaceTextureAndAttachToGLContext(
                previewFrameTexture,
                isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
    }

    protected void viewInit(){
        palmView2 =findViewById(R.id.rainview2);
        palmView2.setVisibility(View.INVISIBLE);
        palmView1 =findViewById(R.id.rianview1);
        palmView1.setVisibility(View.INVISIBLE);
        yeahView =findViewById(R.id.yeahview);
        yeahView.setVisibility(View.INVISIBLE);
        okView=findViewById(R.id.okview);
        okView.setVisibility(View.INVISIBLE);
        fightView=findViewById(R.id.fightview);
        fightView.setVisibility(View.INVISIBLE);
        heartView=findViewById(R.id.heartview);
        heartView.setVisibility(View.INVISIBLE);
        wuView=findViewById(R.id.wuview);
        wuView.setVisibility(View.INVISIBLE);
        chenView=findViewById(R.id.chenview);
        chenView.setVisibility(View.INVISIBLE);

        //按键
        Button palmButton = findViewById(R.id.palmbutton);
        Button yeahButton = findViewById(R.id.yeahbutton);
        Button heartButton = findViewById(R.id.heartbutton);
        Button fightButton = findViewById(R.id.fightbutton);
        Button okButton = findViewById(R.id.okbutton);

        //按钮监听事件
        palmButton.setOnClickListener(view -> {
            isPlam=true;
            isYeah=false;
            isHeart=false;
            isFight=false;
            isOK=false;
            handler.sendEmptyMessage(0x1230);
            palmButton.setBackgroundColor(Color.parseColor("#66ccff"));
            yeahButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            heartButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            fightButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            okButton.setBackgroundColor(Color.parseColor("#bb86fc"));
        });
        yeahButton.setOnClickListener(view -> {
            isPlam=false;
            isYeah=true;
            isHeart=false;
            isFight=false;
            isOK=false;
            handler.sendEmptyMessage(0x1230);
            palmButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            yeahButton.setBackgroundColor(Color.parseColor("#66ccff"));
            heartButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            fightButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            okButton.setBackgroundColor(Color.parseColor("#bb86fc"));
        });
        heartButton.setOnClickListener(view -> {
            isPlam=false;
            isYeah=false;
            isHeart=true;
            isFight=false;
            isOK=false;
            handler.sendEmptyMessage(0x1230);
            palmButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            yeahButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            heartButton.setBackgroundColor(Color.parseColor("#66ccff"));
            fightButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            okButton.setBackgroundColor(Color.parseColor("#bb86fc"));

        });
        fightButton.setOnClickListener(view -> {
            isPlam=false;
            isYeah=false;
            isHeart=false;
            isFight=true;
            isOK=false;
            handler.sendEmptyMessage(0x1230);
            palmButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            yeahButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            heartButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            fightButton.setBackgroundColor(Color.parseColor("#66ccff"));
            okButton.setBackgroundColor(Color.parseColor("#bb86fc"));
        });
        okButton.setOnClickListener(view -> {
            isPlam=false;
            isYeah=false;
            isHeart=false;
            isFight=false;
            isOK=true;
            handler.sendEmptyMessage(0x1230);
            palmButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            yeahButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            heartButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            fightButton.setBackgroundColor(Color.parseColor("#bb86fc"));
            okButton.setBackgroundColor(Color.parseColor("#66ccff"));
        });


        //显示区域
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        widthSize = point.x;
        heightSize = point.y;

        //实际区域
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        widthRealSize = outMetrics.widthPixels;
        heightRealSize = outMetrics.heightPixels;

        //        获得ImageView中Image的真实宽高
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.champion);
        yeahMark1.x=widthSize/2f;
        yeahMark2.x=widthSize/2f;
        yeahMark1.y=heightSize/2f-bitmap.getHeight()/2f;
        yeahMark2.y=heightSize/2f+bitmap.getHeight()/2f;
        midPoint(yeahLastMid,yeahMark1, yeahMark2);
        yeahOldDist=(float)bitmap.getWidth();

        //        获得ImageView中Image的真实宽高
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ok);
        okMark1.x=widthSize/2f;
        okMark2.x=widthSize/2f;
        okMark1.y=heightSize/2f-bitmap.getHeight()/2f;
        okMark2.y=heightSize/2f+bitmap.getHeight()/2f;
        midPoint(okLastMid,okMark1, okMark2);
        okOldDist=(float)bitmap.getHeight();


        //        获得ImageView中Image的真实宽高
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fight);
        fightMark1.x=widthSize/2f;
        fightMark2.x=widthSize/2f;
        fightMark1.y=heightSize/2f-bitmap.getHeight()/2f;
        fightMark2.y=heightSize/2f+bitmap.getHeight()/2f;
        midPoint(fightLastMid,fightMark1, fightMark2);
        fightOldDist=(float)bitmap.getWidth();

        //        获得ImageView中Image的真实宽高
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heart);
        heartMark1.x=widthSize/2f;
        heartMark2.x=widthSize/2f;
        heartMark1.y=heightSize/2f-bitmap.getHeight()/2f;
        heartMark2.y=heightSize/2f+bitmap.getHeight()/2f;
        midPoint(heartLastMid,heartMark1, heartMark2);
        heartOldDist=(float)bitmap.getWidth();
    }

}