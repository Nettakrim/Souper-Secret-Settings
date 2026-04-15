#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 DepthSize;
};

layout(std140) uniform DepthOutlineConfig {
    vec3 ColorScale;
    vec2 Clipping;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

float LinearizeDepth(float depth) {
    return (Clipping.x*Clipping.y) / (depth * (Clipping.x - Clipping.y) + Clipping.y);
}

void main(){
    vec4 col = texture(InSampler, texCoord);

    vec2 oneTexel = 1.0 / InSize;
    float depth = LinearizeDepth(texture(InDepthSampler, texCoord).r);
    float depthUp = LinearizeDepth(texture(InDepthSampler, texCoord+vec2(0.0, oneTexel.y)).r);

    float diff = abs(depth-depthUp);
    col = mix(col, clamp(col * vec4(ColorScale*diff, 1.0), 0, 1), Alpha);

    fragColor = vec4(col.rgb, 1.0);
}
