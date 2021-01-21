#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES s_Texture;
void main() {
    vec4 nColor = texture2D(s_Texture, textureCoordinate);

    nColor.r = max(min(nColor.r,1.0),0.0);
    nColor.g = max(min(nColor.g,1.0),0.0);
    nColor.b = max(min(nColor.b,1.0),0.0);
    nColor.a = max(min(nColor.a,1.0),0.0);

    gl_FragColor = nColor;

}