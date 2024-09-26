#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int LayerEffectValue;

void main(){
    vec4 targetColor = texture(InSampler, texCoord);
    vec4 color = texture(PrevSampler, texCoord);
    int channel = LayerEffectValue%3;
    if (channel == 0) {
        color.r = targetColor.r;
    } else if (channel == 1) {
        color.g = targetColor.g;
    } else {
        color.b = targetColor.b;
    }
    fragColor = vec4(color.rgb, 1);
}