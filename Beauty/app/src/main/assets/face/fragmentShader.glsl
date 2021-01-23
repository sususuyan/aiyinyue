#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTexCoord;
uniform samplerExternalOES sTexture;
uniform mat4 uSTMatrix;


#define INDEX_FACE_LIFT     0// 瘦脸
#define INDEX_FACE_SHAVE    1// 削脸
#define INDEX_FACE_NARROW   2// 小脸
#define INDEX_CHIN          3// 下巴
#define INDEX_FOREHEAD      4// 额头
#define INDEX_EYE_ENLARGE   5// 大眼
#define INDEX_PHILTRUM      6// 人中
#define INDEX_EYE_CORNER    7// 眼角
#define INDEX_NOSE_THIN     8// 瘦鼻
#define INDEX_ALAE          9// 鼻翼
#define INDEX_PROBOSCIS    10// 长鼻
#define INDEX_MOUTH        11// 嘴型

#define INDEX_SIZE 12// 索引大小

// 图像的关键点坐标
uniform vec2 cartesianPoints[109];

// 美型程度参数列表
uniform float ReshapeIntensity[INDEX_SIZE];

// 曲线形变处理
vec2 curveWarp(vec2 textureCoord, vec2 originPosition, vec2 targetPosition, float radius){
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

// 圆内放大
vec2 enlargeFun(vec2 currentCoordinate, vec2 circleCenter, float radius, float intensity){
    float currentDistance = distance(currentCoordinate, circleCenter);
    float weight = currentDistance / radius;
    weight = 1.0 - intensity * (1.0 - weight * weight);
    weight = clamp(weight, 0.0, 1.0);
    currentCoordinate = circleCenter + (currentCoordinate - circleCenter) * weight;

    return currentCoordinate;
}

//圆内缩小
vec2 narrowFun(vec2 currentCoordinate, vec2 circleCenter, float radius, float intensity){
    float currentDistance = distance(currentCoordinate, circleCenter);
    float weight = currentDistance/radius;
    weight = 1.0 + intensity*(1.0-weight * weight);
    weight = clamp(weight, 0.0001, 1.0);
    currentCoordinate = circleCenter+(currentCoordinate-circleCenter)/weight;
    return currentCoordinate;
}

// 瘦脸
vec2 faceLift(vec2 currentCoordinate, float faceLength){
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
vec2 chinChange(vec2 currentCoordinate, float faceLength){
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float chinScale = ReshapeIntensity[INDEX_CHIN] *0.1;
    float radius = faceLength * 0.3;
    currentPoint = cartesianPoints[77];
    destPoint = currentPoint + (cartesianPoints[69] - currentPoint) * chinScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);
    currentPoint = cartesianPoints[0];
    destPoint = currentPoint + (cartesianPoints[69] - currentPoint) * chinScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);
    currentPoint = cartesianPoints[95];
    destPoint = currentPoint + (cartesianPoints[69] - currentPoint) * chinScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}

// 削脸
vec2 faceShave(vec2 currentCoordinate, float faceLength){
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float faceShaveScale = ReshapeIntensity[INDEX_FACE_SHAVE]*0.1;
    float radius = faceLength * 0.55;

    // 下嘴唇外沿中心
    vec2 chinCenter =  cartesianPoints[32];
    currentPoint = cartesianPoints[78];
    destPoint = currentPoint + (chinCenter - currentPoint) * faceShaveScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[95];
    destPoint = currentPoint + (chinCenter - currentPoint) * faceShaveScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[81];
    destPoint = currentPoint + (chinCenter - currentPoint) * faceShaveScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[98];
    destPoint = currentPoint + (chinCenter - currentPoint) * faceShaveScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}

// 瘦鼻
vec2 noseThin(vec2 currentCoordinate, float faceLength){
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float noseThinScale = ReshapeIntensity[INDEX_NOSE_THIN]*0.1;
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
    destPoint = currentPoint + (noseCenter - currentPoint) * noseThinScale*3.0;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[92];
    destPoint = currentPoint + (noseCenter - currentPoint) * noseThinScale*3.0;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}

// 人中
vec2 philtrumChange(vec2 currentCoordinate, float faceLength){
    vec2 coordinate = currentCoordinate;
    vec2 centerPoint=(cartesianPoints[36]+cartesianPoints[103]) * 0.5;//嘴唇中心
    vec2 destPoint = vec2(0.0);
    float philtrumChangeScale = ReshapeIntensity[INDEX_PHILTRUM]* 0.2;
    float a = max( distance(centerPoint,cartesianPoints[46]) , distance(centerPoint,cartesianPoints[50]) );
    float b = max( distance(centerPoint,cartesianPoints[32]) , distance(centerPoint , (cartesianPoints[39]+cartesianPoints[26]) * 0.5) );

    float weight= (coordinate.x-centerPoint.x) * (coordinate.x-centerPoint.x) / (a*a) + (coordinate.y-centerPoint.y) * (coordinate.y-centerPoint.y) / (b*b);//椭圆方程
    weight=3.0-weight;
    weight = clamp(weight, 0.0, 1.0);

    destPoint = centerPoint + (cartesianPoints[46] - centerPoint) * philtrumChangeScale;
    vec2 direction = destPoint - centerPoint;
        coordinate=coordinate-direction*weight;





    return coordinate;
}

// 额头
vec2 foreheadChange(vec2 currentCoordinate, float faceLength){
    vec2 coordinate = currentCoordinate;
    vec2 currentPoint = vec2(0.0);
    vec2 destPoint = vec2(0.0);
    float faceLiftScale = ReshapeIntensity[INDEX_FOREHEAD]* 0.15;
    float radius = faceLength*0.7;

    // 额头中心
    vec2 headCenter = (cartesianPoints[21] + cartesianPoints[107]) * 0.5;

    currentPoint = cartesianPoints[106];
    destPoint = currentPoint + (headCenter - currentPoint) * faceLiftScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    currentPoint = cartesianPoints[108];
    destPoint = currentPoint + (headCenter - currentPoint) * faceLiftScale;
    coordinate = curveWarp(coordinate, currentPoint, destPoint, radius);

    return coordinate;
}

void main() {
    vec2 coordinate = vTexCoord.xy;
    float eyeDistance = distance(cartesianPoints[55], cartesianPoints[52]);// 两个瞳孔的距离

    //大眼
    float eyeEnlarge = ReshapeIntensity[INDEX_EYE_ENLARGE]*0.15;// 放大倍数
    if (eyeEnlarge > 0.0) {
        float eyeRadius = eyeDistance * 0.3;// 眼睛放大半径
        coordinate = enlargeFun(coordinate, cartesianPoints[55] + (cartesianPoints[52] - cartesianPoints[55]) * 0.05, eyeRadius, eyeEnlarge);
        coordinate = enlargeFun(coordinate, cartesianPoints[52] + (cartesianPoints[55] - cartesianPoints[52]) * 0.05, eyeRadius, eyeEnlarge);
    }

    // 瘦脸
    coordinate = faceLift(coordinate, eyeDistance);

    // 下巴
    coordinate = chinChange(coordinate, eyeDistance);

    // 削脸
    coordinate = faceShave(coordinate, eyeDistance);

    //瘦鼻
    coordinate = noseThin(coordinate, eyeDistance);

    //嘴唇
    float mouseEnlarge = ReshapeIntensity[INDEX_MOUTH]*0.4;// 放大倍数
    float mouseRadius = eyeDistance * 0.3;// 嘴巴变化半径
    if (mouseEnlarge > 0.0) { //放大
        coordinate = enlargeFun(coordinate, cartesianPoints[45]+(cartesianPoints[50] - cartesianPoints[45]) * 0.25, mouseRadius, mouseEnlarge);
        coordinate = enlargeFun(coordinate, cartesianPoints[50]+(cartesianPoints[45] - cartesianPoints[50]) * 0.25, mouseRadius, mouseEnlarge);
    }
    else if (mouseEnlarge<0.0){ //縮小
        coordinate = narrowFun(coordinate, cartesianPoints[45]+(cartesianPoints[50] - cartesianPoints[45]) * 0.25, mouseRadius, mouseEnlarge);
        coordinate = narrowFun(coordinate, cartesianPoints[50]+(cartesianPoints[45] - cartesianPoints[50]) * 0.25, mouseRadius, mouseEnlarge);
    }

    //人中
    coordinate = philtrumChange(coordinate, eyeDistance);

    //额头
    coordinate = foreheadChange(coordinate, eyeDistance);

    coordinate = (uSTMatrix * vec4(coordinate, 0, 1.0)).xy;
    gl_FragColor = texture2D(sTexture, coordinate);


}