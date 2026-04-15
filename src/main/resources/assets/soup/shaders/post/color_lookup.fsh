#version 330

uniform sampler2D InSampler;
uniform sampler2D LookupSampler;

layout(std140) uniform ColorLookupConfig {
    float GridSize;
    float Iterations;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

const float scale = 255.0 / 256.0;

vec3 lookup(vec3 col) {
    col *= scale;
    vec2 coord = vec2((col.r + floor(fract(col.b * GridSize))) / GridSize, (col.g + floor(col.b * GridSize)) / GridSize);
    return texture(LookupSampler, coord).rgb;
}

void main() {
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 col = base;

    float a = abs(Iterations);
    float s = sign(Iterations);
    for (int i = 0; i < a; i++) {
        vec3 target = lookup(col);
        col = mix(col, target, s*min(a-i,1));
    }

    fragColor = vec4(mix(base, col, Alpha), 1.0);
}
