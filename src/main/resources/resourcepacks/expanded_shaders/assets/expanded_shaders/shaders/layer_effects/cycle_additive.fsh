#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int LayerEffectValue;

void main(){
    vec4 color = texture(DiffuseSampler, texCoord);
    int channel = LayerEffectValue%3;
    if (channel == 0) {
        color.g = 0;
        color.b = 0;
    } else if (channel == 1) {
        color.r = 0;
        color.b = 0;
    } else {
        color.r = 0;
        color.g = 0;
    }
    fragColor = vec4(color.rgb, 1);
}