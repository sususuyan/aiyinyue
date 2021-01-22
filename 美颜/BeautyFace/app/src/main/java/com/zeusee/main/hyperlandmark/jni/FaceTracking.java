package com.zeusee.main.hyperlandmark.jni;

import android.util.Log;

import com.zeusee.main.hyperlandmark.CameraOverlap;
import com.zeusee.main.hyperlandmark.FaceLandmark;
import com.zeusee.main.hyperlandmark.FacePointsUtils;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;


public class FaceTracking {

    static {
        System.loadLibrary("zeuseesTracking-lib");
    }


    private static class FaceTrackingHolder {
        private static final FaceTracking instance = new FaceTracking();
    }


    public static FaceTracking getInstance() {
        return FaceTrackingHolder.instance;
    }


    public native static void update(byte[] data, int height, int width, int angle, boolean mirror, long session);

    //scale  跟踪时缩小，对速度有要求可以考虑,1 为 原图， 2 为 缩小一般，以此类推
    public native static void initTracker(int height, int width, int scale, long session);

    public native static long createSession(String modelPath);

    public native static void releaseSession(long session);

    public native static int getTrackingNum(long session);

    public native static int[] getTrackingLandmarkByIndex(int index, long session);

    public native static int[] getTrackingLocationByIndex(int index, long session);

    public native static int[] getAttributeByIndex(int index, long session);

    public native static float[] getEulerAngleByIndex(int index, long session);

    public native static int getTrackingIDByIndex(int index, long session);

    private long session;
    private List<Face> faces;
    private int tracking_seq = 0;

    // 手机当前的方向，0表示正屏幕，3表示倒过来，1表示左屏幕，2表示右屏幕
    private float mOrientation=3;
    private boolean mNeedFlip=true;
    private int height;
    private int width;

    public FaceTracking() {
    }


//    public FaceTracking(String pathModel) {
//        session = createSession(pathModel);
//        faces = new ArrayList<Face>();
//    }

    public void release() {
        releaseSession(session);
    }


    public void FaceTrackingInit(String pathModel, int height, int width) {
        session = createSession(pathModel);
        faces = new ArrayList<Face>();
        initTracker(height, width, CameraOverlap.SCALLE_FACTOR, session);
    }

    public boolean postProcess(int[] landmark_prev,int[] landmark_curr) {
        int diff = 0;

        for (int i = 0; i < 106 * 2; i++) {
            diff += abs(landmark_curr[i] - landmark_prev[i]);

        }

        if (diff < 1.0 * 106 * 2) {
            for (int j = 0; j < 106 * 2; j++) {
                landmark_curr[j] = (landmark_curr[j] + landmark_prev[j]) / 2;
            }
            return true;
        } else if (diff < 2 * 106 * 2) {
            for (int j = 0; j < 106 * 2; j++) {
                landmark_curr[j] = (landmark_curr[j] + landmark_prev[j]) / 2;
            }
            return true;
        }
        return false;
    }

    public int find_id_face(List<Face> faces, int targetID) {
        for (int i = 0; i < faces.size(); i++) {
            if (faces.get(i).ID == targetID)
                return i;
        }
        return -1;
    }

    public void postProcess_aux(int[] landmark_prev, int[] landmark_curr) {

        for (int i = 0; i < 106 * 2; i++) {
            landmark_curr[i] = (landmark_curr[i]);

        }
    }


    public void Update(byte[] data, int height, int width) {
        update(data, height, width, 270, true, session);
        this.height=height;
        this.width=width;
        int numsFace = getTrackingNum(session);
        List<Face> _faces = new ArrayList<Face>();
        for (int i = 0; i < numsFace; i++) {
            int ID_GET = -1;
            int flag = -1;

            int[] faceRect = getTrackingLocationByIndex(i, session);
            int id = getTrackingIDByIndex(i, session);
//            Log.e("TAG","====id====="+id);
            int[] landmarks = getTrackingLandmarkByIndex(i, session);
            float[] attitudes = getEulerAngleByIndex(i, session);
            if (tracking_seq > 0) {
                ID_GET = find_id_face(faces, id);
                if (ID_GET != -1) {
                    boolean res = postProcess(faces.get(ID_GET).landmarks, landmarks);
                    if (res)
                        flag = -2;
                }
                if (ID_GET != -1) {
                    if (faces.get(ID_GET).isStable) {
                        postProcess_aux(faces.get(ID_GET).landmarks, landmarks);
                    }
                }
            }

            Face face = new Face(faceRect[0], faceRect[1], faceRect[2], faceRect[3], landmarks, id);
            face.pitch = attitudes[0];
            face.yaw = attitudes[1];
            face.roll = attitudes[2];
            if (flag == -2)
                face.isStable = true;
            else
                face.isStable = false;
            _faces.add(face);
//            faces.(i,face);
        }
        faces.clear();
        faces = _faces;
        tracking_seq += 1;

    }


    public List<Face> getTrackingInfo() {
        return faces;

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


    /**
     * 计算额外人脸顶点，新增8个额外顶点坐标
     * @param vertexPoints
     * @param index
     */
    public void calculateExtraFacePoints(float[] vertexPoints, int index) {
        if (vertexPoints == null || index >= faces.size() || faces.get(index) == null
                || faces.get(index).landmarks.length + 8 * 2 > vertexPoints.length) {
            return;
        }
        Face oneFace = faces.get(index);
        float[] points = new float[106 * 2];
        for (int i = 0; i < 106; i++) {
            float x;
            boolean rotate270=true;
            if (rotate270) {
                x = oneFace.landmarks[i * 2] * CameraOverlap.SCALLE_FACTOR;
            } else {
                x = CameraOverlap.PREVIEW_HEIGHT - oneFace.landmarks[i * 2];
            }
            float y =oneFace.landmarks[i * 2 + 1] * CameraOverlap.SCALLE_FACTOR;
            points[i * 2] = view2openglX(x, height);
            points[i * 2 + 1] = view2openglY(y, width);
            //Log.e("i", String.valueOf(i));
            //Log.e("vertexpoints:", "("+String.valueOf(points[i*2])+","+String.valueOf(points[i*2+1])+")");
        }

        // 复制关键点的数据
        System.arraycopy(points, 0, vertexPoints, 0, oneFace.landmarks.length);
        // 新增的人脸关键点
        float[] point = new float[2];
        // 嘴唇中心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.mouthUpperLipBottom * 2],
                vertexPoints[FaceLandmark.mouthUpperLipBottom * 2 + 1],
                vertexPoints[FaceLandmark.mouthLowerLipTop * 2],
                vertexPoints[FaceLandmark.mouthLowerLipTop * 2 + 1]
        );
        vertexPoints[FaceLandmark.mouthCenter * 2] = point[0];
        vertexPoints[FaceLandmark.mouthCenter * 2 + 1] = point[1];

        // 左眉心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.leftEyebrowUpperMiddle * 2],
                vertexPoints[FaceLandmark.leftEyebrowUpperMiddle * 2 + 1],
                vertexPoints[FaceLandmark.leftEyebrowLowerMiddle * 2],
                vertexPoints[FaceLandmark.leftEyebrowLowerMiddle * 2 + 1]
        );
        vertexPoints[FaceLandmark.leftEyebrowCenter * 2] = point[0];
        vertexPoints[FaceLandmark.leftEyebrowCenter * 2 + 1] = point[1];

        // 右眉心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.rightEyebrowUpperMiddle * 2],
                vertexPoints[FaceLandmark.rightEyebrowUpperMiddle * 2 + 1],
                vertexPoints[FaceLandmark.rightEyebrowLowerMiddle * 2],
                vertexPoints[FaceLandmark.rightEyebrowLowerMiddle * 2 + 1]
        );
        vertexPoints[FaceLandmark.rightEyebrowCenter * 2] = point[0];
        vertexPoints[FaceLandmark.rightEyebrowCenter * 2 + 1] = point[1];

        // 额头中心
        vertexPoints[FaceLandmark.headCenter * 2] = vertexPoints[FaceLandmark.eyeCenter * 2] * 2.0f - vertexPoints[FaceLandmark.noseLowerMiddle * 2];
        vertexPoints[FaceLandmark.headCenter * 2 + 1] = vertexPoints[FaceLandmark.eyeCenter * 2 + 1] * 2.0f - vertexPoints[FaceLandmark.noseLowerMiddle * 2 + 1];

        // 额头左侧，备注：这个点不太准确，后续优化
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.leftEyebrowLeftTopCorner * 2],
                vertexPoints[FaceLandmark.leftEyebrowLeftTopCorner * 2 + 1],
                vertexPoints[FaceLandmark.headCenter * 2],
                vertexPoints[FaceLandmark.headCenter * 2 + 1]
        );
        vertexPoints[FaceLandmark.leftHead * 2] = point[0];
        vertexPoints[FaceLandmark.leftHead * 2 + 1] = point[1];

        // 额头右侧，备注：这个点不太准确，后续优化
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.rightEyebrowRightTopCorner * 2],
                vertexPoints[FaceLandmark.rightEyebrowRightTopCorner * 2 + 1],
                vertexPoints[FaceLandmark.headCenter * 2],
                vertexPoints[FaceLandmark.headCenter * 2 + 1]
        );
        vertexPoints[FaceLandmark.rightHead * 2] = point[0];
        vertexPoints[FaceLandmark.rightHead * 2 + 1] = point[1];

        // 左脸颊中心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.leftCheekEdgeCenter * 2],
                vertexPoints[FaceLandmark.leftCheekEdgeCenter * 2 + 1],
                vertexPoints[FaceLandmark.noseLeft * 2],
                vertexPoints[FaceLandmark.noseLeft * 2 + 1]
        );
        vertexPoints[FaceLandmark.leftCheekCenter * 2] = point[0];
        vertexPoints[FaceLandmark.leftCheekCenter * 2 + 1] = point[1];

        // 右脸颊中心
        FacePointsUtils.getCenter(point,
                vertexPoints[FaceLandmark.rightCheekEdgeCenter * 2],
                vertexPoints[FaceLandmark.rightCheekEdgeCenter * 2 + 1],
                vertexPoints[FaceLandmark.noseRight * 2],
                vertexPoints[FaceLandmark.noseRight * 2 + 1]
        );
        vertexPoints[FaceLandmark.rightCheekCenter * 2] = point[0];
        vertexPoints[FaceLandmark.rightCheekCenter * 2 + 1] = point[1];
    }

    /**
     * 计算
     * @param vertexPoints
     */
    private void calculateImageEdgePoints(float[] vertexPoints) {
        if (vertexPoints == null || vertexPoints.length < 122 * 2) {
            return;
        }

        if (mOrientation == 0) {
            vertexPoints[114 * 2] = 0;
            vertexPoints[114 * 2 + 1] = 1;
            vertexPoints[115 * 2] = 1;
            vertexPoints[115 * 2 + 1] = 1;
            vertexPoints[116 * 2] = 1;
            vertexPoints[116 * 2 + 1] = 0;
            vertexPoints[117 * 2] = 1;
            vertexPoints[117 * 2 + 1] = -1;
        } else if (mOrientation == 1) {
            vertexPoints[114 * 2] = 1;
            vertexPoints[114 * 2 + 1] = 0;
            vertexPoints[115 * 2] = 1;
            vertexPoints[115 * 2 + 1] = -1;
            vertexPoints[116 * 2] = 0;
            vertexPoints[116 * 2 + 1] = -1;
            vertexPoints[117 * 2] = -1;
            vertexPoints[117 * 2 + 1] = -1;
        } else if (mOrientation == 2) {
            vertexPoints[114 * 2] = -1;
            vertexPoints[114 * 2 + 1] = 0;
            vertexPoints[115 * 2] = -1;
            vertexPoints[115 * 2 + 1] = 1;
            vertexPoints[116 * 2] = 0;
            vertexPoints[116 * 2 + 1] = 1;
            vertexPoints[117 * 2] = 1;
            vertexPoints[117 * 2 + 1] = 1;
        } else if (mOrientation == 3) {
            vertexPoints[114 * 2] = 0;
            vertexPoints[114 * 2 + 1] = -1;
            vertexPoints[115 * 2] = -1;
            vertexPoints[115 * 2 + 1] = -1;
            vertexPoints[116 * 2] = -1;
            vertexPoints[116 * 2 + 1] = 0;
            vertexPoints[117 * 2] = -1;
            vertexPoints[117 * 2 + 1] = 1;
        }
        // 118 ~ 121 与 114 ~ 117 的顶点坐标恰好反过来
        vertexPoints[118 * 2] = -vertexPoints[114 * 2];
        vertexPoints[118 * 2 + 1] = -vertexPoints[114 * 2 + 1];
        vertexPoints[119 * 2] = -vertexPoints[115 * 2];
        vertexPoints[119 * 2 + 1] = -vertexPoints[115 * 2 + 1];
        vertexPoints[120 * 2] = -vertexPoints[116 * 2];
        vertexPoints[120 * 2 + 1] = -vertexPoints[116 * 2 + 1];
        vertexPoints[121 * 2] = -vertexPoints[117 * 2];
        vertexPoints[121 * 2 + 1] = -vertexPoints[117 * 2 + 1];

        // 是否需要做翻转处理，前置摄像头预览时，关键点是做了翻转处理的，因此图像边沿的关键点也要做翻转能处理
        if (mNeedFlip) {
            for (int i = 0; i < 8; i++) {
                vertexPoints[(114 + i) * 2] = -vertexPoints[(114 + i) * 2];
                vertexPoints[(114 + i) * 2 + 1] = -vertexPoints[(114 + i) * 2 + 1];
            }
        }

    }
    /**
     * 获取用于美型处理的坐标
     * @param vertexPoints  顶点坐标，一共122个顶点
     * @param texturePoints 纹理坐标，一共122个顶点
     * @param faceIndex     人脸索引
     */
    public void updateFaceAdjustPoints(float[] vertexPoints, float[] texturePoints, int faceIndex) {
        if (vertexPoints == null || vertexPoints.length != 122 * 2
                || texturePoints == null || texturePoints.length != 122 * 2) {
            return;
        }
        // 计算额外的人脸顶点坐标
        calculateExtraFacePoints(vertexPoints, faceIndex);
        // 计算图像边沿顶点坐标
        calculateImageEdgePoints(vertexPoints);
        // 计算纹理坐标
        for (int i = 0; i < vertexPoints.length; i++) {
            texturePoints[i] = vertexPoints[i] * 0.5f + 0.5f;
        }
    }
}
