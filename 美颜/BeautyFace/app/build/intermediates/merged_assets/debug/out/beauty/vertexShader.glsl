attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 textureCoordinate;
void main() {
    textureCoordinate = aTextureCoord;
    gl_Position = aPosition;
}
