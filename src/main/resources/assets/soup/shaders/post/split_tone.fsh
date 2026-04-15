#version 330

uniform sampler2D InSampler;

layout(std140) uniform SplitToneConfig {
    vec3 ScaleA;
    vec3 OffsetA;
    vec3 ScaleB;
    vec3 OffsetB;
    float Normalise;
    vec3 ColorModulate;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec3 col = texture(InSampler, texCoord).rgb;

    vec3 normal = col*ScaleA - OffsetA;
    normal = (mix(normal, normalize(normal), Normalise)*ScaleB + OffsetB);

    fragColor = vec4(mix(col, normal*ColorModulate, Alpha), 1.0);
}
