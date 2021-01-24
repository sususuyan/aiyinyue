package com.zeusee.main.hyperlandmark;

import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;

import com.zeusee.main.hyperlandmark.jni.Face;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;


public class GLFrame {

    private int width, height, screenWidth, screenHeight;

    // 106 个关键点
    private final int FacePoints = 106;
    // 笛卡尔坐标系
    private float[] mCartesianVertices = new float[106 * 2];
    // 脸型程度
    private float[] mReshapeIntensity = new float[14];

    private final float[] vertexData = {
            1f, -1f,
            -1f, -1f,
            1f, 1f,
            -1f, 1f,
    };
    private final float[] textureVertexData = {
            1f, 0f,
            0f, 0f,
            1f, 1f,
            0f, 1f
    };
    private FloatBuffer mVertexBuffer;

    private FloatBuffer mTextureBuffer;
    // 笛卡尔坐标缓冲
    private FloatBuffer mCartesianBuffer;

    private FloatBuffer left;
    private FloatBuffer right;

    private int programId = -1;
    private int aPositionHandle;
    private int uTextureSamplerHandle;
    private int aTextureCoordHandle;
    private int uSTMMatrixHandle;

    private int aHandle;
    private int mleHandle;
    private int mreHandle;
    private int mReshapeIntensityHandle;
    //private int mReshapeHandle;
    private int mCartesianPointsHandle;

    private String fragmentShader;
    private String vertexShader;

    private int[] vertexBuffers;


    private Context mContext;
    private float[] eye = {
            0.5f, 0f,
            0f, 0.5f
    };
    private float[] points= new float[106*2];


    // 大眼 0.0f ~ 1.0f
    public float eyeEnlargeIntensity;

    public GLFrame(Context context) {
        mVertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        .put(vertexData);
        mVertexBuffer.position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        .put(textureVertexData);
        mTextureBuffer.position(0);

//        mCartesianBuffer = ByteBuffer.allocateDirect(FacePoints * 2 * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();

        left = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();


        mContext = context;
    }

    public void initFrame() {

        //加载顶点着色器的脚本内容
        vertexShader = ShaderUtils.getShaderFromAssets(mContext, "face/vertexShader.glsl");
        //加载片元着色器的脚本内容
        fragmentShader = ShaderUtils.getShaderFromAssets(mContext, "face/fragmentShader.glsl");
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix");
        uTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "sTexture");
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord");

//        aHandle=GLES20.glGetUniformLocation(programId, "a");
//        mleHandle=GLES20.glGetUniformLocation(programId, "le");
//        mreHandle=GLES20.glGetUniformLocation(programId, "re");
        mReshapeIntensityHandle=GLES20.glGetUniformLocation(programId, "ReshapeIntensity");
//        mCartesianPointsHandle = GLES30.glGetUniformLocation(programId, "cartesianPoints");



    }


    private Rect rect = new Rect();

    public void setSize(int screenWidth, int screenHeight, int videoWidth, int videoHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.width = videoWidth;
        this.height = videoHeight;
        rect();
    }/**
    public void setSEye(float[][] eyes) {
        eye=eyes;
        if(eyes!=null){
            System.out.println(eyes[0][0]+" "+eyes[0][1]+" "+eyes[1][0]+" "+eyes[1][1]);
        }
    }**/
    public void setI(float eyec){
        this.eyeEnlargeIntensity = eyec;
    }

    private void rect() {
        int left, top, viewWidth, viewHeight;
        float sh = screenWidth * 1.0f / screenHeight;
        float vh = width * 1.0f / height;
        if (sh < vh) {
            left = 0;
            viewWidth = screenWidth;
            viewHeight = (int) (height * 1.0f / width * viewWidth);
            top = (screenHeight - viewHeight) / 2;
        } else {
            top = 0;
            viewHeight = screenHeight;
            viewWidth = (int) (width * 1.0f / height * viewHeight);
            left = (screenWidth - viewWidth) / 2;
        }
        rect.left = left;
        rect.top = top;
        rect.right = viewWidth;
        rect.bottom = viewHeight;
    }

    public void drawFrame(int textureId, float[] STMatrix) {
        //updateFaceVertices();
        vertexBuffers = new int[2];
        GLES20.glGenBuffers(2, vertexBuffers, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4, mVertexBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureVertexData.length * 4, mTextureBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, rect.right, rect.bottom);
        GLES20.glUseProgram(programId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false,
                0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[1]);
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0);
/*
        //左眼
        float x = points[110]*0.5f+0.5f;
        float y = points[111]*0.5f+0.5f;
        left.clear();
        left.put(x);
        left.put(y);
        left.position(0);
        GLES20.glUniform2fv(mleHandle,1,left);
        //右眼
        x = points[104]*0.5f+0.5f;
        y = points[105]*0.5f+0.5f;
        right.clear();
        right.put(x);
        right.put(y);
        right.position(0);
        GLES20.glUniform2fv(mreHandle,1,right);
*/
//        GLES20.glUniform2fv(mCartesianPointsHandle,106,mCartesianBuffer);

     //   GLES20.glUniform1fv(mReshapeIntensityHandle, 14, FloatBuffer.wrap(mReshapeIntensity));

      //  GLES20.glUniform1f(aHandle,eyeEnlargeIntensity);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(uTextureSamplerHandle, 0);
        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, STMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void release() {
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteBuffers(2, vertexBuffers, 0);
    }


    public void setpoints(float[] points) {
        this.points=points;
        updateCartesianVertices();
        /**
        // 计算额外的人脸顶点坐标
        calculateExtraFacePoints(mVertices, points);
        // 计算图像边沿顶点坐标
        calculateImageEdgePoints(mVertices);
        // 计算纹理坐标
        for (int i = 0; i < mVertices.length; i++) {
            mTextureVertices[i] = mVertices[i] * 0.5f + 0.5f;

        }
        mVertexBuffer.rewind();
        mVertexBuffer.put(mVertices);//填充数据
        mVertexBuffer.position(0);
        mTextureBuffer.rewind();
        mTextureBuffer.put(mVertices);//填充数据
        mTextureBuffer.position(0);**/
    }

    /**
     * 更新笛卡尔坐标系
     */
    private void updateCartesianVertices() {
        for (int i = 0; i < FacePoints; i++) {
            mCartesianVertices[i * 2] = points[i * 2]*0.5f+0.5f ;
            mCartesianVertices[i * 2 + 1] = points[i * 2 + 1]*0.5f+0.5f ;
        }
        mCartesianBuffer.clear();
        mCartesianBuffer.put(mCartesianVertices);
        mCartesianBuffer.position(0);
    }

    public void onBeauty(BeautyParam beauty) {
        if (beauty == null) {
            return;
        }
        mReshapeIntensity[0]  = beauty.faceLift;                // 瘦脸
        mReshapeIntensity[1]  = beauty.faceShave;               // 削脸
        mReshapeIntensity[2]  = beauty.faceNarrow;              // 小脸
        mReshapeIntensity[3]  = beauty.chinIntensity;           // 下巴
        mReshapeIntensity[4]  = beauty.foreheadIntensity;       // 额头
        mReshapeIntensity[5]  = beauty.eyeEnlargeIntensity;     // 大眼
        mReshapeIntensity[6]  = beauty.eyeDistanceIntensity;    // 眼距
        mReshapeIntensity[7]  = beauty.eyeCornerIntensity;      // 眼角
        mReshapeIntensity[8]  = beauty.noseThinIntensity;       // 瘦鼻
        mReshapeIntensity[9]  = beauty.alaeIntensity;           // 鼻翼
        mReshapeIntensity[10] = beauty.proboscisIntensity;      // 长鼻
        mReshapeIntensity[11] = beauty.mouthEnlargeIntensity;   // 嘴型
        mReshapeIntensity[12] = beauty.beautyIntensity;         //磨皮
        mReshapeIntensity[13] = beauty.complexionIntensity;     //美肤
    }

    /**
     * 人脸图像索引(702个点)（122个关键点）
     * 具体的关键点可参考 landmarklibrary的assets目录下的三角剖.jpg
     */
    private static final short[] FaceImageIndices = {
            // 脸外索引(人脸顶部中心逆时针数)
            110, 114, 111,
            111, 114, 115,
            115, 111, 32,
            32, 115, 116,
            116, 32, 31,
            31, 116, 30,
            30, 116, 29,
            29, 116, 28,
            28, 116, 27,
            27, 116, 26,
            26, 116, 25,
            25, 116, 117,
            117, 25, 24,
            24, 117, 23,
            23, 117, 22,
            22, 117, 21,
            21, 117, 20,
            20, 117, 19,
            19, 117, 118,
            118, 19, 18,
            18, 118, 17,
            17, 118, 16,
            16, 118, 15,
            15, 118, 14,
            14, 118, 13,
            13, 118, 119,
            119, 13, 12,
            12, 119, 11,
            11, 119, 10,
            10, 119, 9,
            9, 119, 8,
            8, 119, 7,
            7, 119, 120,
            120, 7, 6,
            6, 120, 5,
            5, 120, 4,
            4, 120, 3,
            3, 120, 2,
            2, 120, 1,
            1, 120, 0,
            0, 120, 121,
            121, 0, 109,
            109, 121, 114,
            114, 109, 110,
            // 脸内部索引
            // 额头
            0, 33, 109,
            109, 33, 34,
            34, 109, 35,
            35, 109, 36,
            36, 109, 110,
            36, 110, 37,
            37, 110, 43,
            43, 110, 38,
            38, 110, 39,
            39, 110, 111,
            111, 39, 40,
            40, 111, 41,
            41, 111, 42,
            42, 111, 32,
            // 左眉毛
            33, 34, 64,
            64, 34, 65,
            65, 34, 107,
            107, 34, 35,
            35, 36, 107,
            107, 36, 66,
            66, 107, 65,
            66, 36, 67,
            67, 36, 37,
            37, 67, 43,
            // 右眉毛
            43, 38, 68,
            68, 38, 39,
            39, 68, 69,
            39, 40, 108,
            39, 108, 69,
            69, 108, 70,
            70, 108, 41,
            41, 108, 40,
            41, 70, 71,
            71, 41, 42,
            // 左眼
            0, 33, 52,
            33, 52, 64,
            52, 64, 53,
            64, 53, 65,
            65, 53, 72,
            65, 72, 66,
            66, 72, 54,
            66, 54, 67,
            54, 67, 55,
            67, 55, 78,
            67, 78, 43,
            52, 53, 57,
            53, 72, 74,
            53, 74, 57,
            74, 57, 73,
            72, 54, 104,
            72, 104, 74,
            74, 104, 73,
            73, 104, 56,
            104, 56, 54,
            54, 56, 55,
            // 右眼
            68, 43, 79,
            68, 79, 58,
            68, 58, 59,
            68, 59, 69,
            69, 59, 75,
            69, 75, 70,
            70, 75, 60,
            70, 60, 71,
            71, 60, 61,
            71, 61, 42,
            42, 61, 32,
            61, 60, 62,
            60, 75, 77,
            60, 77, 62,
            77, 62, 76,
            75, 77, 105,
            77, 105, 76,
            105, 76, 63,
            105, 63, 59,
            105, 59, 75,
            59, 63, 58,
            // 左脸颊
            0, 52, 1,
            1, 52, 2,
            2, 52, 57,
            2, 57, 3,
            3, 57, 4,
            4, 57, 112,
            57, 112, 74,
            74, 112, 56,
            56, 112, 80,
            80, 112, 82,
            82, 112, 7,
            7, 112, 6,
            6, 112, 5,
            5, 112, 4,
            56, 80, 55,
            55, 80, 78,
            // 右脸颊
            32, 61, 31,
            31, 61, 30,
            30, 61, 62,
            30, 62, 29,
            29, 62, 28,
            28, 62, 113,
            62, 113, 76,
            76, 113, 63,
            63, 113, 81,
            81, 113, 83,
            83, 113, 25,
            25, 113, 26,
            26, 113, 27,
            27, 113, 28,
            63, 81, 58,
            58, 81, 79,
            // 鼻子部分
            78, 43, 44,
            43, 44, 79,
            78, 44, 80,
            79, 81, 44,
            80, 44, 45,
            44, 81, 45,
            80, 45, 46,
            45, 81, 46,
            80, 46, 82,
            81, 46, 83,
            82, 46, 47,
            47, 46, 48,
            48, 46, 49,
            49, 46, 50,
            50, 46, 51,
            51, 46, 83,
            // 鼻子和嘴巴中间三角形
            7, 82, 84,
            82, 84, 47,
            84, 47, 85,
            85, 47, 48,
            48, 85, 86,
            86, 48, 49,
            49, 86, 87,
            49, 87, 88,
            88, 49, 50,
            88, 50, 89,
            89, 50, 51,
            89, 51, 90,
            51, 90, 83,
            83, 90, 25,
            // 上嘴唇部分
            84, 85, 96,
            96, 85, 97,
            97, 85, 86,
            86, 97, 98,
            86, 98, 87,
            87, 98, 88,
            88, 98, 99,
            88, 99, 89,
            89, 99, 100,
            89, 100, 90,
            // 下嘴唇部分
            90, 100, 91,
            100, 91, 101,
            101, 91, 92,
            101, 92, 102,
            102, 92, 93,
            102, 93, 94,
            102, 94, 103,
            103, 94, 95,
            103, 95, 96,
            96, 95, 84,
            // 唇间部分
            96, 97, 103,
            97, 103, 106,
            97, 106, 98,
            106, 103, 102,
            106, 102, 101,
            106, 101, 99,
            106, 98, 99,
            99, 101, 100,
            // 嘴巴与下巴之间的部分(关键点7 到25 与嘴巴鼻翼围起来的区域)
            7, 84, 8,
            8, 84, 9,
            9, 84, 10,
            10, 84, 95,
            10, 95, 11,
            11, 95, 12,
            12, 95, 94,
            12, 94, 13,
            13, 94, 14,
            14, 94, 93,
            14, 93, 15,
            15, 93, 16,
            16, 93, 17,
            17, 93, 18,
            18, 93, 92,
            18, 92, 19,
            19, 92, 20,
            20, 92, 91,
            20, 91, 21,
            21, 91, 22,
            22, 91, 90,
            22, 90, 23,
            23, 90, 24,
            24, 90, 25
    };

    /**
     * 计算额外人脸顶点，新增8个额外顶点坐标
     * @param vertexPoints
     * @param Points
     */
    public void calculateExtraFacePoints(float[] vertexPoints, float[] Points) {
        // 复制关键点的数据
        System.arraycopy(Points, 0, vertexPoints, 0, Points.length);
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

    // 手机当前的方向，0表示正屏幕，3表示倒过来，1表示左屏幕，2表示右屏幕
    private float mOrientation=0;
    private boolean mNeedFlip=true;
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
}

