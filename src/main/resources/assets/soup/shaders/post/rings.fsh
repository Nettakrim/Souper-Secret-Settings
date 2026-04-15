#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform RingsConfig {
    float Radius;
    float InnerPercent;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec2 oneTexel = 1.0 / InSize;

    vec4 base = texture(InSampler, texCoord);

    vec4 maxVal = InnerPercent > 0 ? vec4(0) : base;
    for(float u = 0.0; u <= Radius; u += 1.0) {
        for(float v = u == 0 ? 1.0 : 0.0; v <= Radius; v += 1.0) {
            float d = sqrt(u * u + v * v) / (Radius);
            if (d > 1.0 || d < InnerPercent) continue;

            vec4 s0 = texture(InSampler, texCoord + vec2(-u * oneTexel.x, -v * oneTexel.y));
            vec4 s1 = texture(InSampler, texCoord + vec2( u * oneTexel.x,  v * oneTexel.y));
            vec4 s2 = texture(InSampler, texCoord + vec2(-u * oneTexel.x,  v * oneTexel.y));
            vec4 s3 = texture(InSampler, texCoord + vec2( u * oneTexel.x, -v * oneTexel.y));

            maxVal = max(maxVal, max(max(s0, s1), max(s2, s3)));
        }
    }

    fragColor = vec4(mix(base, maxVal, Alpha).rgb, 1.0);
}
