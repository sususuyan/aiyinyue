package com.zeusee.main.hyperlandmark;

/**
 * 关键点索引（106个关键点 + 扩展8个关键点）
 * Created by cain on 2017/11/10.
 */

public final class FaceLandmark {

    private FaceLandmark() {}

    // 左眉毛
    public static int leftEyebrowRightCorner = 29;      // 左眉毛右边角
    public static int leftEyebrowLeftCorner = 20;       // 左眉毛左边角
    public static int leftEyebrowLeftTopCorner = 85;    // 左眉毛左顶角
    public static int leftEyebrowRightTopCorner = 80;   // 左眉毛右顶角
    public static int leftEyebrowUpperMiddle = 30;      // 左眉毛上中心
    public static int leftEyebrowLowerMiddle = 84;      // 左眉毛下中心

    // 右眉毛
    public static int rightEyebrowRightCorner = 25;     // 右眉毛右边角
    public static int rightEyebrowLeftCorner = 75;      // 右眉毛左上角
    public static int rightEyebrowLeftTopCorner = 74;   // 右眉毛左顶角
    public static int rightEyebrowRightTopCorner = 76;  // 右眉毛右顶角
    public static int rightEyebrowUpperMiddle = 71;     // 右眉毛上中心
    public static int rightEyebrowLowerMiddle = 72;     // 右眉毛下中心

    // 左眼
    public static int leftEyeTop = 35;         //  左眼球上边
    public static int leftEyeCenter = 53;      // 左眼球中心
    public static int leftEyeBottom = 4;      // 左眼球下边
    public static int leftEyeLeftCorner = 95;  // 左眼左边角
    public static int leftEyeRightCorner = 60; // 左眼右边角

    // 右眼
    public static int rightEyeTop = 42;            // 右眼球上边
    public static int rightEyeCenter = 56;         // 右眼球中心
    public static int rightEyeBottom = 44;         // 右眼球下边
    public static int rightEyeLeftCorner = 28;     // 右眼左边角
    public static int rightEyeRightCorner = 21;    // 右眼右边角

    public static int eyeCenter = 22;   // 两眼中心

    // 鼻子
    public static int noseTop = 70;         // 鼻尖
    public static int noseLeft = 32;        // 鼻子左边
    public static int noseRight = 94;       // 鼻子右边
    public static int noseLowerMiddle = 47; // 两鼻孔中心

    // 脸边沿
    public static int leftCheekEdgeCenter = 8;        // 左脸颊边沿中心
    public static int rightCheekEdgeCenter = 17;      // 右脸颊边沿中心

    // 嘴巴
    public static int mouthLeftCorner = 46;        // 嘴唇左边
    public static int mouthRightCorner = 51;       // 嘴唇右边
    public static int mouthUpperLipTop = 39;       // 上嘴唇上中心
    public static int mouthUpperLipBottom = 37;    // 上嘴唇下中心
    public static int mouthLowerLipTop = 104;      // 下嘴唇上中心
    public static int mouthLowerLipBottom = 33;    // 下嘴唇下中心

    // 下巴
    public static int chinLeft = 78;        // 下巴左边
    public static int chinRight = 96;       // 下巴右边
    public static int chinCenter = 1;      // 下巴中心

    // 扩展的关键点(8个)
    public static int mouthCenter = 106;        // 嘴巴中心
    public static int leftEyebrowCenter = 107;  // 左眉心
    public static int rightEyebrowCenter = 108; // 右眉心
    public static int leftHead = 109;           // 额头左侧
    public static int headCenter = 110;         // 额头中心
    public static int rightHead = 111;          // 额头右侧
    public static int leftCheekCenter = 112;    // 左脸颊中心
    public static int rightCheekCenter = 113;   // 右脸颊中心
}
