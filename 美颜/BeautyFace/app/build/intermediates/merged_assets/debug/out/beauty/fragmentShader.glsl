#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES inputTexture;
uniform mat4 uSTMatrix;
void main() {
    vec2 coordinate = textureCoordinate.xy;
    coordinate = (uSTMatrix * vec4(coordinate, 0, 1.0)).xy;
    gl_FragColor = texture2D(inputTexture, coordinate);
}