package com.scut.gesturerecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
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
    int widthSize ;
    int heightSize;
    int widthRealSize ;
    int heightRealSize;

    Matrix matrix=new Matrix();

    PointF mark1 = new PointF(0,0);
    PointF lastMid = new PointF(0,0);
    PointF nowMid = new PointF(0,0);
    PointF mark2 = new PointF(0,0);

    float oldDist = 0f;

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


    //指定图片
    private ImageView imageView;

    private static final String TAG = "MainActivity";
    private static final String BINARY_GRAPH_NAME = "hand_tracking_mobile_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    private static final int NUM_HANDS = 1;
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
    NormalizedLandmarkList HandLandmarks;
//    Iterator<NormalizedLandmarkList>  iteratorOfLandmarks;

    Bitmap bitmap;//计算初始图片位置用



    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x1233) {
                matrix.set(imageView.getImageMatrix());

                mark1.set(HandLandmarks.getLandmark(0).getX()*widthSize,HandLandmarks.getLandmark(0).getY()*heightSize);
                mark2.set(HandLandmarks.getLandmark(12).getX()*widthSize,HandLandmarks.getLandmark(12).getY()*heightSize);
                midPoint(nowMid,mark1,mark2);

                //完成显示，下一步是随手势移动
                imageView.setVisibility(View.VISIBLE);

                //移动
                matrix.postTranslate(nowMid.x-lastMid.x,nowMid.y-lastMid.y);

                //缩放
                float newDist=spacing(mark1, mark2);
                if(newDist>10){

                    float scale=newDist/oldDist;
                    matrix.postScale((float) Math.pow(scale,1f), (float) Math.pow(scale,1f),nowMid.x,nowMid.y);
                }

                //输出
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                imageView.setImageMatrix(matrix);


                for (NormalizedLandmark landmark : HandLandmarks.getLandmarkList()) {
                    if (landmark.getX()<0||landmark.getY()<0||landmark.getX()>1||landmark.getY()>1){
                        imageView.setVisibility(View.INVISIBLE);
                        break;
                    }
                }

                //为下一次做准备
                oldDist=newDist;
                lastMid.set(nowMid);
            }
            if (msg.what == 0x1234) {
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView =findViewById(R.id.imageview2);
        imageView.setVisibility(View.INVISIBLE);

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

        //获得ImageView中Image的真实宽高
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jay);
        mark1.x=widthSize/2f;
        mark2.x=widthSize/2f;
        mark1.y=heightSize/2f-bitmap.getHeight()/2f;
        mark2.y=heightSize/2f+bitmap.getHeight()/2f;
        midPoint(lastMid,mark1,mark2);
        oldDist=(float)bitmap.getHeight();


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
            if(handIndex==0) {
                if (HandGestureRecognition.handGestureRecognition(landmarks) == Gestures.FIVE) {
                    HandLandmarks=landmarks;
                    handler.sendEmptyMessage(0x1233);
                } else if (imageView.getVisibility() == View.VISIBLE && HandGestureRecognition.handGestureRecognition(landmarks) != Gestures.FIVE) {
                    handler.sendEmptyMessage(0x1234);
                }
            }
            ++handIndex;
        }

        return multiHandLandmarksStr;
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

}