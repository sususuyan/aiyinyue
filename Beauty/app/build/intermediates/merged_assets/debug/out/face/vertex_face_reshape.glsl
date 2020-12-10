//属性变量，只能用于顶点着色器中
//含四个浮点型数据的向量 顶点坐标 物体形状组成的点、
attribute vec4 aPosition;
//纹理坐标 从图像上采集像素的位置 根据vposition形状对应的采集图片像素的点
attribute vec2 aTextureCoord;

//易变变量 通过varying传给fragment varying修饰的变量才能够传值
varying vec2 textureCoordinate;

void main() {
    //gl_position是着色器内部变量，只要将坐标传给opengl就会自动处理
    gl_Position = aPosition;
    textureCoordinate = aTextureCoord.xy;
}
