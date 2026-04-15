#version 150

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform CheckerboardConfig {
    vec2 Offset;
    float Centering;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    float amount = ((int(texCoord.x*InSize.x)+int(texCoord.y*InSize.y))%2)+Centering;
    vec4 col = texture(InSampler, mod(texCoord + Offset*amount, vec2(1,1)));
    fragColor = vec4(mix(texture(InSampler, texCoord), col, Alpha).rgb, 1.0);
}
