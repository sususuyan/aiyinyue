# Beauty 介绍
该部分实现了美型功能。截止目前为止，已完成美型处理包括：
* 大眼
* 瘦脸
* 伸缩下巴
* 削脸
* 瘦鼻
* 显示人脸识别关键点
### 备注：
目前只能实现单人美型，多人美型尚在更改中

# 关于人脸SDK
采用HyperLandmark人脸标定算法，更多相关内容可以去开源项目中了解：(https://github.com/zeusees/HyperLandmark)。
# Beauty 介绍
该部分实现了美型功能。截止目前为止，已完成美型处理包括：
* 大眼
* 瘦脸
* 伸缩下巴
* 削脸
* 瘦鼻
* 显示人脸识别关键点
### 备注：
目前只能实现单人美型，多人美型尚在更改中

# 关于人脸SDK
采用HyperLandmark人脸标定算法，更多相关内容可以去开源项目中了解：(https://github.com/zeusees/HyperLandmark)。

# 部署
* Android 4.3或更高版本（OpenGL ES 2.0或更高版本）

# 代码说明
    main
    |   AndroidManifest.xml
    |   
    +---assets
    |   +---face
    |   |       fragmentShader.glsl
    |   |       fragment_face_reshape.glsl
    |   |       vertexShader.glsl
    |   |       vertex_face_reshape.glsl
    |   |       
    |   \---ZeuseesFaceTracking
    |       \---models
    |               det1.bin
    |               det1.param
    |               det2.bin
    |               det2.param
    |               det3.bin
    |               det3.param
    |               tracking.bin
    |               tracking.proto.bin
    |       
    +---java
    |   \---com
    |       \---zeusee
    |           \---main
    |               \---hyperlandmark
    |                   |   BeautyParam.java
    |                   |   CameraOverlap.java
    |                   |   EGLUtils.java
    |                   |   FaceLandmark.java
    |                   |   FacePointsUtils.java
    |                   |   FileUtil.java
    |                   |   FixedAspectRatioRelativeLayout.java
    |                   |   GLBitmap.java
    |                   |   GLFrame.java
    |                   |   GLFramebuffer.java
    |                   |   GLPoints.java
    |                   |   MainActivity.java
    |                   |   ShaderUtils.java
    |                   |   
    |                   \---jni
    |                           Face.java
    |                           FaceTracking.java
    |                           
    +---jniLibs
    |   +---arm64-v8a
    |   |       libzeuseesTracking-lib.so
    |   |       
    |   \---armeabi-v7a
    |           libzeuseesTracking-lib.so
    |           
    \---res   
        +---layout
        |       activity_main.xml

