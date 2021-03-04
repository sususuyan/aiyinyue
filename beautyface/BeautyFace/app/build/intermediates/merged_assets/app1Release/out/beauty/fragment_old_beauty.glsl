#extension GL_OES_EGL_image_external : require
precision lowp float;
uniform samplerExternalOES inputTexture;
//uniform sampler2D inputTexture;
varying lowp vec2 textureCoordinate;

uniform int width;
uniform int height;
uniform mat4 uSTMatrix;
// 磨皮程度(由低到高: 0.5 ~ 0.99)
uniform float opacity;
uniform float intensity1;
uniform lowp float alpha; // 肤色参数
const float intensity = 30.0;
const int SHIFT_SIZE = 10;

uniform sampler2D grayTexture;  // 灰度查找表
uniform sampler2D lookupTexture; // LUT

uniform highp float levelRangeInv; // 范围
uniform lowp float levelBlack; // 灰度level

//USM锐化


void main() {

vec2 coordinate = textureCoordinate.xy;
        coordinate = (uSTMatrix * vec4(coordinate, 0, 1.0)).xy;
//vec4 test1 = texture2D(lookupTexture, textureCoordinate);
vec4 test1 = texture2D(inputTexture, coordinate);
vec4 cutColor = texture2D(inputTexture, coordinate);
 float y= (cutColor.r * 0.256789 + cutColor.g * 0.504129 + cutColor.b * 0.097906)+ 16.0/255.0;

     float cb= (cutColor.r *-0.148223 + cutColor.g * -0.290992 + cutColor.b * 0.439215)+ 128.0/255.0;

     float cr= (cutColor.r * 0.439215 + cutColor.g * -0.367789 + cutColor.b *-0.071426)+ 128.0/255.0;

  if(!((cutColor.r>95.0/255.0) &&(cutColor.g>40.0/255.0)&&(cutColor.b>20.0/255.0)&&(cutColor.r>cutColor.g)&&
  (cutColor.r>cutColor.b)&&(max(max(cutColor.r,cutColor.g),cutColor.b)-min(min(cutColor.r,cutColor.g),cutColor.b)>15.0/255.0)&&
  (abs(cutColor.r-cutColor.g)>15.0/255.0))){
            cutColor.r = 0.0;
            cutColor.g = 0.0;
            cutColor.b = 0.0;
            cutColor.a = 0.0;
  }
lowp vec3 textureColor = texture2D(inputTexture, coordinate).rgb;

//vec4 complexion = texture2D(grayTexture, vec2(textureColor.r, 0.5)*coordinate);
//vec4 complexion = texture2D(grayTexture, textureCoordinate);
    textureColor = clamp((textureColor - vec3(levelBlack, levelBlack, levelBlack)) * levelRangeInv, 0.0, 1.0);
    textureColor.r = texture2D(grayTexture, vec2(textureColor.r, 0.5)).r;
    textureColor.g = texture2D(grayTexture, vec2(textureColor.g, 0.5)).g;
    textureColor.b = texture2D(grayTexture, vec2(textureColor.b, 0.5)).b;
//complexion1 = vec4(textureColor.rgb,1.0);
    mediump float blueColor = textureColor.b * 15.0;

    mediump vec2 quad1;
    quad1.y = floor(blueColor / 4.0);
    quad1.x = floor(blueColor) - (quad1.y * 4.0);

    mediump vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 4.0);
    quad2.x = ceil(blueColor) - (quad2.y * 4.0);

    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);

    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.25) + 0.5 / 64.0 + ((0.25 - 1.0 / 64.0) * textureColor.g);

    lowp vec4 newColor1 = texture2D(lookupTexture, texPos1);
    lowp vec4 newColor2 = texture2D(lookupTexture, texPos2);

    lowp vec3 newColor = mix(newColor1.rgb, newColor2.rgb, fract(blueColor));

    textureColor = mix(textureColor, newColor, alpha/2.0);

    vec4 complexion = vec4(textureColor.rgb,1.0);

        highp vec4 blurShiftCoordinates[SHIFT_SIZE];
    // 偏移步距
        vec2 singleStepOffset = vec2(width, height);
        // 记录偏移坐标
        for (int i = 0; i < SHIFT_SIZE; i++) {
            blurShiftCoordinates[i] = vec4(coordinate - float(i + 1) * (1.0/2000.0),
                                           coordinate + float(i + 1) * (1.0/2000.0));
        }

        // 计算当前坐标的颜色值
           //vec4 currentColor = texture2D(inputTexture, coordinate);
          //vec4 currentColor = vec4(textureColor, 1.0);
          vec4 currentColor = cutColor;
        mediump vec3 sum = currentColor.rgb;
        // 计算偏移坐标的颜色值总和
        for (int i = 0; i < SHIFT_SIZE; i++) {
           sum += texture2D(inputTexture, blurShiftCoordinates[i].xy).rgb;
           sum += texture2D(inputTexture, blurShiftCoordinates[i].zw).rgb;
        }
               // 求出平均值
           vec4 coordinate1 = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);
     //vec4 sourceColor = texture2D(inputTexture, coordinate);
         vec4 sourceColor = vec4(textureColor, 1.0);
         vec4 blurColor = coordinate1;
         // 高通滤波之后的颜色值
         vec4 highPassColor = sourceColor - blurColor;
        //vec4 highPassColor = vec4(1.0,1.0,1.0,1.0) - blurColor;
         // 对应混合模式中的强光模式(color = 2.0 * color1 * color2)，对于高反差的颜色来说，color1 和color2 是同一个
         highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r * intensity1, 0.0, 1.0);
         highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g * intensity1, 0.0, 1.0);
         highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b * intensity1, 0.0, 1.0);
//磨皮
          //currentColor = vec4(highPassColor.rgb, 1.0);
          sourceColor = vec4(textureColor, 1.0);
          //sourceColor = currentColor;
          blurColor = coordinate1;
          lowp vec4 highPassBlurColor = vec4(highPassColor.rgb, 1.0);
          // 调节蓝色通道值
          mediump float value = clamp((min(sourceColor.b, blurColor.b) - 0.2) * 5.0, 0.0, 1.0);
// 找到模糊之后RGB通道的最大值
    mediump float maxChannelColor = max(max(highPassBlurColor.r, highPassBlurColor.g), highPassBlurColor.b);
    // 计算当前的强度
        mediump float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value * opacity * 5.0;
        // 混合输出结果
            lowp vec3 resultColor = mix(sourceColor.rgb, blurColor.rgb, currentIntensity);


       // 输出的是把痘印等过滤掉
       //gl_FragColor = vec4(resultColor, 1.0);
      // gl_FragColor = newColor1;
      // gl_FragColor = cutColor;
       //gl_FragColor = deltaColor;
       //  gl_FragColor = vec4(highPassColor.rgb, 1.0);
          //gl_FragColor = highPassColor;
          //gl_FragColor = sourceColor;
           //gl_FragColor = coordinate1;
         // gl_FragColor = test1;
          gl_FragColor = complexion;
          //gl_FragColor = vec4(textureColor, 1.0);
    }
