varying highp vec2 vTexCoord;
uniform highp sampler2D sTexture;
void main() {
    gl_FragColor = texture2D(sTexture,vec2(vTexCoord.x,1.0 - vTexCoord.y));
}