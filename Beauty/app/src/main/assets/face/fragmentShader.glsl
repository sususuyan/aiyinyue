#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTexCoord;
uniform samplerExternalOES sTexture;
uniform mat4 uSTMatrix;

uniform vec2 le;
uniform vec2 re;
uniform float a;

#define INDEX_FACE_LIFT     0   // 瘦脸
#define INDEX_FACE_SHAVE    1   // 削脸
#define INDEX_FACE_NARROW   2   // 小脸
#define INDEX_CHIN          3   // 下巴
#define INDEX_FOREHEAD      4   // 额头
#define INDEX_EYE_ENLARGE   5   // 大眼
#define INDEX_EYE_DISTANCE  6   // 眼距
#define INDEX_EYE_CORNER    7   // 眼角
#define INDEX_NOSE_THIN     8   // 瘦鼻
#define INDEX_ALAE          9   // 鼻翼
#define INDEX_PROBOSCIS    10   // 长鼻
#define INDEX_MOUTH        11   // 嘴型

#define INDEX_SIZE 12           // 索引大小

// 图像的关键点，也就是纹理坐标
uniform vec2 cartesianPoints[106];

// 美型程度参数列表
uniform float reshapeIntensity;

#define INDEX_SIZE 12           // 索引大小

// 美型程度参数列表
uniform float ReshapeIntensity[INDEX_SIZE];

//园内放大
vec2 newCoord(vec2 coord,vec2 eye,float rmax){
    vec2 p=coord;
    float r=distance(coord,eye);
    if(r<rmax){
        float fsr=(1.-pow(r/rmax-1.,2.)*a);
        p=fsr*(coord-eye)+eye;
    }
    return p;
}
// 曲线形变处理
vec2 curveWarp(vec2 textureCoord, vec2 originPosition, vec2 targetPosition, float radius)
{
    vec2 offset = vec2(0.0);
    vec2 result = vec2(0.0);

    vec2 direction = targetPosition - originPosition;

    float infect = distance(textureCoord, originPosition)/radius;

    infect = 1.0 - infect;
    infect = clamp(infect, 0.0, 1.0);
    offset = direction * infect;

    result = textureCoord - offset;

    return result;
}

// 大眼处理
vec2 enlargeEye(vec2 currentCoordinate, vec2 circleCenter, float radius, float intensity)
{
    float currentDistance = distance(currentCoordinate, circleCenter);
    float weight = currentDistance / radius;
    weight = 1.0 - intensity * (1.0 - weight * weight);
    weight = clamp(weight, 0.0, 1.0);
    currentCoordinate = circleCenter + (currentCoordinate - circleCenter) * weight;

    return currentCoordinate;
}

// 瘦脸
vec2 faceLift(vec2 currentCoordinate, float faceLength)
{
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float faceLiftScale = ReshapeIntensity[INDEX_FACE_LIFT]* 0.05;
    float radius = faceLength;

    currentPoint = cartesianPoints[8];
    destPoint = currentPoint + (cartesianPoints[69] - currentPoint) * faceLiftScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[17];
    destPoint = currentPoint + (cartesianPoints[69] - currentPoint) * faceLiftScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    radius = faceLength * 0.8;
    currentPoint = cartesianPoints[81];
    destPoint = currentPoint + (cartesianPoints[69] - currentPoint) * (faceLiftScale * 0.6);
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[98];
    destPoint = currentPoint + (cartesianPoints[69] - currentPoint) * (faceLiftScale * 0.6);
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}

// 处理下巴
vec2 chinChange(vec2 currentCoordinate, float faceLength)
{
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float chinScale = ReshapeIntensity[INDEX_CHIN] *0.08;
    float radius = faceLength * 1.25;
    currentPoint = cartesianPoints[0];
    destPoint = currentPoint + (cartesianPoints[69] - currentPoint) * chinScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}
// 削脸
vec2 faceShave(vec2 currentCoordinate, float faceLength)
{
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float faceShaveScale = ReshapeIntensity[INDEX_FACE_SHAVE]*0.12;
    float radius = faceLength * 1.0;

    // 下巴中心
    vec2 chinCenter = (cartesianPoints[0] + cartesianPoints[32]) * 0.5;
    currentPoint = cartesianPoints[78];
    destPoint = currentPoint + (chinCenter - currentPoint) * faceShaveScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[96];
    destPoint = currentPoint + (chinCenter - currentPoint) * faceShaveScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}
// 瘦鼻
vec2 noseThin(vec2 currentCoordinate, float faceLength)
{
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float noseThinScale = ReshapeIntensity[INDEX_NOSE_THIN]*0.12;
    float radius = faceLength * 0.2;

    // 鼻尖及两侧
    vec2 noseCenter = cartesianPoints[69];

    currentPoint = cartesianPoints[31];
    destPoint = currentPoint + (noseCenter - currentPoint) * noseThinScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[93];
    destPoint = currentPoint + (noseCenter - currentPoint) * noseThinScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    //鼻梁及两侧
    noseCenter = cartesianPoints[22];

    currentPoint = cartesianPoints[90];
    destPoint = currentPoint + (noseCenter - currentPoint) * noseThinScale*0.6;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[92];
    destPoint = currentPoint + (noseCenter - currentPoint) * noseThinScale*0.6;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}
void main() {
    vec2 coordinate = vTexCoord.xy;
    float eyeDistance = distance(cartesianPoints[55], cartesianPoints[52]); // 两个瞳孔的距离

    //大眼
    float eyeEnlarge = ReshapeIntensity[INDEX_EYE_ENLARGE]*0.12; // 放大倍数
    if (eyeEnlarge > 0.0) {
        float radius = eyeDistance * 0.33; // 眼睛放大半径
        coordinate = enlargeEye(coordinate, cartesianPoints[55] + (cartesianPoints[52] - cartesianPoints[55]) * 0.05, radius, eyeEnlarge);
        coordinate = enlargeEye(coordinate, cartesianPoints[52] + (cartesianPoints[55] - cartesianPoints[52]) * 0.05, radius, eyeEnlarge);
        //coordinate = enlargeEye(coordinate, le + (re - le) * 0.05, radius, eyeEnlarge);
        //coordinate = enlargeEye(coordinate, re + (le - re) * 0.05, radius, eyeEnlarge);
    }

    // 瘦脸
    coordinate = faceLift(coordinate, eyeDistance);

    // 下巴
    coordinate = chinChange(coordinate, eyeDistance);

    // 削脸
    coordinate = faceShave(coordinate, eyeDistance);

    //瘦鼻
    coordinate = noseThin(coordinate, eyeDistance);

    coordinate = (uSTMatrix * vec4(coordinate, 0, 1.0)).xy;
    gl_FragColor = texture2D(sTexture, coordinate);




    /**
    //vec2 tx_transformed = (uSTMatrix * vec4(vTexCoord, 0, 1.0)).xy;
    //vec2 left_eye=(uSTMatrix * vec4(le, 0, 1.0)).xy;
    //vec2 right_eye=(uSTMatrix * vec4(re, 0, 1.0)).xy;
    float rmax=distance(le,re)/3.;
    vec2 p =newCoord(vTexCoord,le,rmax);
    p=newCoord(p,re,rmax);
    vec2 tx_transformed = (uSTMatrix * vec4(p, 0, 1.0)).xy;
    gl_FragColor = texture2D(sTexture, tx_transformed);
    **/
}