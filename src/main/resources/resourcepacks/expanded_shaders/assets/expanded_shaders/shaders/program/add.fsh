#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D AddSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

void main() {
    vec4 CurrTexel = texture(DiffuseSampler, texCoord);
    vec4 AddTexel = texture(AddSampler, texCoord);

    fragColor = vec4(AddTexel.rgb + CurrTexel.rgb, 1.0);
}
