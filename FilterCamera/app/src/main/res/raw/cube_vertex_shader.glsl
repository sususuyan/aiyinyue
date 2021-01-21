attribute vec3 vPosition;
uniform mat4 vMatrix;
varying vec4 vColor;
attribute vec4 aColor;
void main() {
    gl_Position = vMatrix * vec4(vPosition, 1.0);
    vColor = aColor;
}