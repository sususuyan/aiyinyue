package com.zeusee.main.hyperlandmark;

/**
 * 美颜参数
 */
public class BeautyParam {
    // 瘦脸程度 0.0 ~ 1.0f
    public float faceLift;
    // 削脸程度 0.0 ~ 1.0f
    public float faceShave;
    // 小脸 0.0 ~ 1.0f
    public float faceNarrow;
    // 下巴-1.0f ~ 1.0f
    public float chinIntensity;
    // 额头 -1.0f ~ 1.0f
    public float foreheadIntensity;
    // 大眼 0.0f ~ 1.0f
    public float eyeEnlargeIntensity;
    // 眼距 -1.0f ~ 1.0f
    public float philtrumIntensity;
    // 眼角 -1.0f ~ 1.0f
    public float eyeCornerIntensity;
    // 瘦鼻 0.0 ~ 1.0f
    public float noseThinIntensity;
    // 鼻翼 0.0 ~ 1.0f
    public float alaeIntensity;
    // 长鼻子 0.0 ~ 1.0f
    public float proboscisIntensity;
    // 嘴型 0.0 ~ 1.0f;
    public float mouthEnlargeIntensity;

    public BeautyParam() {
        reset();
    }

    /**
     * 重置为默认参数
     */
    public void reset() {
        faceLift = 0.0f;
        faceShave = 0.0f;
        faceNarrow = 0.0f;
        chinIntensity = 0.0f;
        foreheadIntensity = 0.0f;
        eyeEnlargeIntensity = 0.0f;
        philtrumIntensity = 0.0f;
        eyeCornerIntensity = 0.0f;
        noseThinIntensity = 0.0f;
        alaeIntensity = 0.0f;
        proboscisIntensity = 0.0f;
        mouthEnlargeIntensity = 0.0f;
    }
}
