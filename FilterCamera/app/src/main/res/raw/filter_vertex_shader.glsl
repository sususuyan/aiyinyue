attribute vec4 vPosition;//顶点位置
attribute vec2 inputTextureCoordinate;//纹理位置
varying vec2 textureCoordinate;
void main() {
    textureCoordinate = inputTextureCoordinate;
    gl_Position = vPosition;
}