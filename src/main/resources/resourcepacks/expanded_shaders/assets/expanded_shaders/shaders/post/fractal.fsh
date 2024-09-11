#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevOutSampler;

in vec2 texCoord;

out vec4 fragColor;

vec2 rotate(vec2 v, float angle) {
    float radians = angle / 57.2957795131;
    float s = sin(radians);
    float c = cos(radians);
    return vec2(v.x*c - v.y*s, v.x*s + v.y*c);
}

void main() {
    vec2 coord = (texCoord-vec2(0.5))*3;
    vec4 col;

    bool x = abs(coord.x) < 0.5;
    bool y = abs(coord.y) < 0.5;

    if (x && y) {
        col = texture(DiffuseSampler, coord+vec2(0.5));
        col = mix(col, texture(PrevOutSampler, coord+vec2(0.5)).brga, 0.5);
    } else if (x != y) {
        col = texture(PrevOutSampler, mod(rotate(coord, 180)+vec2(0.5), 1.0)).gbra;
        col = mix(col, texture(DiffuseSampler, texCoord), 0.25);
    } else {
        col = texture(PrevOutSampler, mod(coord+vec2(0.5), 1.0)).gbra;
        col = mix(col, texture(DiffuseSampler, texCoord).brga, 0.25);
    }

    fragColor = vec4(col.rgb, 1.0);
}
