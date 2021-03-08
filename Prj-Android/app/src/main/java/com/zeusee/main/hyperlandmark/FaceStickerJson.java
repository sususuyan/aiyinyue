package com.zeusee.main.hyperlandmark;

import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * @ClassName FaceStickerJson
 * @Description TODO
 * @Author : kevin
 * @Date 2021/02/03 16:17
 * @Version 1.0
 */
public class FaceStickerJson {
    public int[] centerIndexList;   // 中心坐标索引列表， 有可能是多个关键的计算中心
    public float offsetX;           // 相对于贴纸中心坐标的x轴偏移像素
    public float offsetY;           // 相对于贴纸中心坐标的y轴偏移像素
    public float baseScale;         // 贴纸基准缩放倍数
    public int startIndex;          // 人脸起始索引
    public int endIndex;            // 人脸结束索引， 用于计算人脸宽度
    public double width;               // 贴纸宽度
    public double height;              // 贴纸高度
    public double deep;             // 3D模型的深度
    public int frames;              // 贴纸帧数
    public int action;              // 动作， 0表示默认显示，这里用来处理贴纸动作等
    public String stickerName;      // 贴纸名称， 用于标记贴纸所在文件夹以及png文件的
    public String readType;
    public int duration;            // 贴纸帧显示间隔
    public boolean stickerLooping;  //贴纸是否循环渲染
    public int maxCount;            // 最大贴纸渲染次数

    @NonNull
    @Override
    public String toString() {
        return "FaceStickerJson{"+
                "centerIndexList = " + Arrays.toString(centerIndexList) +
                "offsetX = " + offsetX +
                "offsetY = " + offsetY +
                "baseScale = " + baseScale +
                "startIndex = " + startIndex +
                "endIndex = " + endIndex +
                "width = " + width +
                "height = " + height +
                "deep = " + deep +
                ", frames=" + frames +
                ", action=" + action +
                ", stickerName='" + stickerName + '\'' +
                ", duration=" + duration +
                ", stickerLooping=" + stickerLooping +
                ", maxCount=" + maxCount +
                '}';
    }
}
