#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

uniform float Time;
uniform float Threshold;

void main() {
    vec3 col = texture(DiffuseSampler, texCoord).rgb;
    vec3 prev = texture(PrevSampler, texCoord).rgb;

    float d = texCoord.y;

    fragColor = vec4(mix(prev, col, mod(Time+d, 1.0) < Threshold ? 1.0 : 0.0), 1.0);
}
