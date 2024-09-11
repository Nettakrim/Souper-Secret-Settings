#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int LayerEffectValue;

void main(){
    vec4 color = texture(LayerEffectValue%3 == 0 ? DiffuseSampler : PrevSampler, texCoord);
    fragColor = vec4(color.rgb, 1);
}