#version 330

uniform sampler2D InSampler;

layout(std140) uniform ZigZagConfig {
    vec3 Zig;
    vec3 Zag;
    vec3 Curve;
    float Wrapping;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

vec4 wrapTexture(sampler2D tex, vec2 coord) {
    return texture(tex, mix(coord, fract(coord), Wrapping));
}

float bounce(float t) {
    t = pow(fract(t), Curve.z)-Curve.x;
    return pow(t / (step(0, t)-Curve.x), Curve.y);
}

void main() {
    vec2 coord = texCoord + Zag.xy*(bounce(dot(texCoord-vec2(0.5), Zig.xy) + Zig.z)-Zag.z);
    fragColor = vec4(mix(texture(InSampler, texCoord).rgb, wrapTexture(InSampler, coord).rgb, Alpha), 1.0);
}
