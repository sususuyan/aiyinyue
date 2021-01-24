#extension GL_OES_EGL_image_external : require
precision lowp float;
uniform samplerExternalOES inputTexture;
//uniform sampler2D inputTexture;
uniform sampler2D cutTexture;
varying lowp vec2 textureCoordinate;

uniform int width;
uniform int height;
uniform mat4 uSTMatrix;
// 磨皮程度(由低到高: 0.5 ~ 0.99)
uniform float opacity;
uniform float intensity1;

const float intensity = 24.0;
const int SHIFT_SIZE = 10;

uniform sampler2D grayTexture;  // 灰度查找表
uniform sampler2D lookupTexture; // LUT

uniform highp float levelRangeInv; // 范围
uniform lowp float levelBlack; // 灰度level



void main() {
vec2 coordinate = textureCoordinate.xy;
        coordinate = (uSTMatrix * vec4(coordinate, 0, 1.0)).xy;
        highp vec4 blurShiftCoordinates[SHIFT_SIZE];
    // 偏移步距
        vec2 singleStepOffset = vec2(width, height);
        // 记录偏移坐标
        for (int i = 0; i < SHIFT_SIZE; i++) {
            blurShiftCoordinates[i] = vec4(textureCoordinate - float(i + 1) * (1.0/2000.0),
                                           textureCoordinate + float(i + 1) * (1.0/2000.0));
        }

        // 计算当前坐标的颜色值
           vec4 currentColor = texture2D(inputTexture, coordinate);
          //  vec4 currentColor1 = texture2D(cutTexture, textureCoordinate);
          //  vec4 currentColor = texture2D(cutTexture, textureCoordinate);
          //vec4 currentColor = vec4(textureColor, 1.0);
          //vec4 currentColor = cutColor;
        mediump vec3 sum = currentColor.rgb;
        // 计算偏移坐标的颜色值总和
        lowp float distanceFromCentralColor;
        lowp float gaussianWeight;
        lowp float gaussianWeightTotal;
        gaussianWeightTotal = 0.22;
            sum = currentColor.rgb * 0.22;
        for (int i = 0; i < SHIFT_SIZE; i++) {
          // sum += texture2D(cutTexture, blurShiftCoordinates[i].xy).rgb;
          // sum += texture2D(cutTexture, blurShiftCoordinates[i].zw).rgb;
           distanceFromCentralColor = min(distance(currentColor.rgb, texture2D(cutTexture, blurShiftCoordinates[i].xy).rgb) * 1.0, 1.0);
           gaussianWeight = 0.17 * (1.0 - distanceFromCentralColor);
           gaussianWeightTotal += gaussianWeight;
           sum += texture2D(cutTexture, blurShiftCoordinates[i].xy).rgb * gaussianWeight;
           distanceFromCentralColor = min(distance(currentColor.rgb, texture2D(cutTexture, blurShiftCoordinates[i].zw).rgb) * 2.0, 1.0);
                      gaussianWeight = 0.17 * (1.0 - distanceFromCentralColor);
                      gaussianWeightTotal += gaussianWeight;
                      sum += texture2D(cutTexture, blurShiftCoordinates[i].zw).rgb * gaussianWeight;
        }
        vec4 coordinate1 = currentColor;
               // 求出平均值
               if((currentColor.r>95.0/255.0) &&(currentColor.g>40.0/255.0)&&(currentColor.b>20.0/255.0)&&(currentColor.r>currentColor.g)&&
                    (currentColor.r>currentColor.b)&&(max(max(currentColor.r,currentColor.g),currentColor.b)-min(min(currentColor.r,currentColor.g),currentColor.b)>15.0/255.0)&&
                    (abs(currentColor.r-currentColor.g)>15.0/255.0)){
         // coordinate1 = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);
         coordinate1 = vec4(sum,1.0) / gaussianWeightTotal;
           }

         //vec4 sourceColor = texture2D(inputTexture, coordinate);
         vec4 sourceColor = texture2D(cutTexture, textureCoordinate);
         vec4 blurColor = coordinate1;

         // 高通滤波之后的颜色值
         vec4 highPassColor = sourceColor - blurColor;
         vec4 test1 = sourceColor - blurColor;
        //vec4 highPassColor = vec4(1.0,1.0,1.0,1.0) - blurColor;
         // 对应混合模式中的强光模式(color = 2.0 * color1 * color2)，对于高反差的颜色来说，color1 和color2 是同一个
         highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r * (intensity1*20.0+20.0)*30.0, 0.0, 1.0);
         highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g * (intensity1*20.0+20.0)*30.0, 0.0, 1.0);
         highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b * (intensity1*20.0+20.0)*30.0, 0.0, 1.0);
//磨皮
          //currentColor = vec4(highPassColor.rgb, 1.0);
          //sourceColor = vec4(textureColor, 1.0);
          //sourceColor = currentColor;
          lowp vec4 highPassBlurColor = vec4(highPassColor.rgb, 1.0);
          // 调节蓝色通道值
          mediump float value = clamp((min(sourceColor.b, blurColor.b) - 0.2) * 5.0, 0.0, 1.0);
// 找到模糊之后RGB通道的最大值
    mediump float maxChannelColor = max(max(highPassBlurColor.r, highPassBlurColor.g), highPassBlurColor.b);
    // 计算当前的强度
        mediump float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value * opacity * 5.0 ;
        // 混合输出结果
            lowp vec3 resultColor = mix(sourceColor.rgb, blurColor.rgb, currentIntensity);


       // 输出的是把痘印等过滤掉
      gl_FragColor = vec4(resultColor, 1.0);
      //gl_FragColor = vec4(sum,1.0) / gaussianWeightTotal;
      // gl_FragColor = blurColor;
      // gl_FragColor = newColor1;
       //gl_FragColor = cutColor;
       //gl_FragColor = deltaColor;
        //gl_FragColor = vec4(highPassColor.rgb, 1.0);
          //gl_FragColor = highPassColor;
         // gl_FragColor = sourceColor;
           //gl_FragColor = coordinate1;
          //gl_FragColor = test1;
         // gl_FragColor = complexion;
         // gl_FragColor = vec4(textureColor, 1.0);
    }
