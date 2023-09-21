#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int LayerEffectValue;

void main(){
    vec4 color = texture(PrevSampler, texCoord);
    int channel = LayerEffectValue%3;
    if (channel == 0) {
        color.r = 0;
    } else if (channel == 1) {
        color.g = 0;
    } else {
        color.b = 0;
    }
    color += texture(DiffuseSampler, texCoord);
    fragColor = vec4(color.rgb, 1);
}