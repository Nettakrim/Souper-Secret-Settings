#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Scale;

void main() {
    float d = distance(vec2(0.5), texCoord)*inversesqrt(0.5);

    vec4 col = texture(DiffuseSampler, texCoord);
    vec4 baseCol = texture(BaseSampler, texCoord);

    fragColor = vec4(mix(col.rgb, baseCol.rgb, (d/(d+(1/Scale)))/(Scale/(Scale+1))), 1.0);
}
