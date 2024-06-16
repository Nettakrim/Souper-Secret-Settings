#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 Offset;
uniform float Centering;

void main() {
    float amount = ((int(texCoord.x/oneTexel.x)+int(texCoord.y/oneTexel.y))%2)+Centering;
    vec4 col = texture(DiffuseSampler, mod(texCoord + Offset*amount, vec2(1,1)));
    fragColor = vec4(col.rgb, 1.0);
}
