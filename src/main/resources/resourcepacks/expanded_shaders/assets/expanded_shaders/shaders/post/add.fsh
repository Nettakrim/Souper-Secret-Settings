#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D AddSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

uniform float Mix;

void main() {
    vec3 CurrTexel = texture(DiffuseSampler, texCoord).rgb;
    vec3 AddTexel = texture(AddSampler, texCoord).rgb;

    fragColor = vec4(mix(CurrTexel, AddTexel + CurrTexel, Mix), 1.0);
}
