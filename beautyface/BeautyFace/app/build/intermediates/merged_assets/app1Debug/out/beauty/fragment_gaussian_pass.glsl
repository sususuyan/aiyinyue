// 优化后的高斯模糊
#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES inputTexture;
uniform mat4 uSTMatrix;
uniform highp float texelWidthOffset;
uniform highp float texelHeightOffset;
uniform sampler2D grayTexture;  // 灰度查找表
uniform sampler2D lookupTexture; // LUT
uniform highp float levelRangeInv; // 范围
uniform lowp float levelBlack; // 灰度level
uniform lowp float alpha; // 肤色成都
const float intensity = 24.0;   // 强光程度
// 高斯算子左右偏移值，当偏移值为2时，高斯算子为5 x 5
const int SHIFT_SIZE = 20;
void main() {
vec2 coordinate = textureCoordinate.xy;
        coordinate = (uSTMatrix * vec4(coordinate, 0, 1.0)).xy;
    highp vec4 blurShiftCoordinates[SHIFT_SIZE];
// 偏移步距
    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
    // 记录偏移坐标
    for (int i = 0; i < SHIFT_SIZE; i++) {
        blurShiftCoordinates[i] = vec4(textureCoordinate.xy - float(i + 1) * singleStepOffset,
                                       textureCoordinate.xy + float(i + 1) * singleStepOffset);
    }

    // 计算当前坐标的颜色值
       vec4 currentColor = texture2D(inputTexture, textureCoordinate);
     // vec4 currentColor = vec4(textureColor, 1.0);
    mediump vec3 sum = currentColor.rgb;
    // 计算偏移坐标的颜色值总和
    for (int i = 0; i < SHIFT_SIZE; i++) {
        sum += texture2D(inputTexture, (uSTMatrix*vec4(blurShiftCoordinates[i].xy,0,1.0)).xy).rgb;
       sum += texture2D(inputTexture, (uSTMatrix*vec4(blurShiftCoordinates[i].zw,0,1.0)).xy).rgb;
    }
           // 求出平均值
       vec4 coordinate1 = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);


    vec4 sourceColor = texture2D(inputTexture, coordinate);
    //vec4 sourceColor = vec4(textureColor, 1.0);
    vec4 blurColor = coordinate1;
    // 高通滤波之后的颜色值
    vec4 highPassColor = sourceColor - blurColor;
    // 对应混合模式中的强光模式(color = 2.0 * color1 * color2)，对于高反差的颜色来说，color1 和color2 是同一个
    highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r * intensity, 0.0, 1.0);
    highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g * intensity, 0.0, 1.0);
    highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b * intensity, 0.0, 1.0);
    // 输出的是把痘印等过滤掉
    gl_FragColor = vec4(highPassColor.rgb, 1.0);
   //gl_FragColor = highPassColor;
   //gl_FragColor = sourceColor;
    //gl_FragColor = coordinate1;
  // gl_FragColor = complexion;
   //gl_FragColor = vec4(textureColor, 1.0);
}