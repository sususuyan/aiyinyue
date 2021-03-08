precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D iTexture;
void main() {
    gl_FragColor = texture2D(iTexture, vTexCoord);
}