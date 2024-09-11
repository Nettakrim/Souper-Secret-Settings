#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D SubtractSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

uniform vec3 Color;
uniform float Scale;

void main() {
    vec3 col = Color + (texture(DiffuseSampler, texCoord).rgb - texture(SubtractSampler, texCoord).rgb) * Scale;
    fragColor = vec4(col, 1.0);
}
