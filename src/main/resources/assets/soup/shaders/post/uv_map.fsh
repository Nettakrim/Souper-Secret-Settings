#version 330

uniform sampler2D InSampler;
uniform sampler2D BaseSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform UVMapConfig {
    uniform vec4 UVDistances;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;

    vec3 pos = texture(InSampler, texCoord).rgb;

    vec3 offsetLeft =  pos - texture(InSampler, texCoord + vec2(-oneTexel.x * UVDistances.z, 0)).rgb;
    vec3 offsetRight = pos - texture(InSampler, texCoord + vec2( oneTexel.x * UVDistances.x, 0)).rgb;
    vec3 offsetUp =    pos - texture(InSampler, texCoord + vec2(0,  oneTexel.y * UVDistances.y)).rgb;
    vec3 offsetDown =  pos - texture(InSampler, texCoord + vec2(0, -oneTexel.y * UVDistances.w)).rgb;

    vec3 totalOffset = abs(offsetLeft) + abs(offsetRight) + abs(offsetUp) + abs(offsetDown);

    vec2 uv;
    if (totalOffset.x < totalOffset.y && totalOffset.x < totalOffset.z) {
        uv = pos.zy;
    } else if (totalOffset.y < totalOffset.x && totalOffset.y < totalOffset.z) {
        uv = pos.xz;
    } else {
        uv = pos.xy;
    }

    fragColor = vec4(mix(pos, texture(BaseSampler, uv).rgb, Alpha), 1.0);
}
