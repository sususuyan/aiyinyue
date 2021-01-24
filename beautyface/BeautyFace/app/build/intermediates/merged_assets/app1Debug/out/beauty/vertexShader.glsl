attribute vec4 aPosition;
attribute vec4 aTextureCoord;
varying vec2 textureCoordinate;
void main() {
    textureCoordinate = aTextureCoord.xy;
    gl_Position = aPosition;
}
