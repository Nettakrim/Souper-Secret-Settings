#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D SubtractSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

void main() {
    vec4 col = texture(DiffuseSampler, texCoord) - texture(SubtractSampler, texCoord);

    fragColor = vec4(col.rgb, 1.0);
}
