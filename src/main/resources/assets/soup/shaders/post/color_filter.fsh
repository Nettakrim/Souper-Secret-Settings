#version 330

uniform sampler2D InSampler;

layout(std140) uniform ColorFilterConfig {
    vec3 Gray;
    vec3 Mask;
    vec3 Threshold;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

float maxRGB(vec3 v) {
    return max(max(v.r, v.g), v.b);
}

void main(){
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 col = maxRGB(base*Mask) > maxRGB(base*Threshold) ? base.rgb : vec3(dot(base, Gray));
    fragColor = vec4(mix(base, col, Alpha), 1.0);
}
