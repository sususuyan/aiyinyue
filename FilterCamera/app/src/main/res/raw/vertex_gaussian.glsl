// 优化后的高斯模糊
attribute vec4 aPosition;
attribute vec4 aTextureCoord;

// 高斯算子左右偏移值，当偏移值为2时，高斯算子为5 x 5
const int SHIFT_SIZE = 2;

uniform highp float texelWidthOffset;
uniform highp float texelHeightOffset;

varying vec2 textureCoordinate;
varying vec4 blurShiftCoordinates[SHIFT_SIZE];

void main() {
    gl_Position = aPosition;
    textureCoordinate = aTextureCoord.xy;
    // 偏移步距
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    // 记录偏移坐标
    for (int i = 0; i < SHIFT_SIZE; i++) {
        blurShiftCoordinates[i] = vec4(textureCoordinate.xy - float(i + 1) * singleStepOffset,
                                       textureCoordinate.xy + float(i + 1) * singleStepOffset);
    }
}