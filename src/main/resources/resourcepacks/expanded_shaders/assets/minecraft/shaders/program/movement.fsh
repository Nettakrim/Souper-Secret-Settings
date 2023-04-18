#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevInSampler;
uniform sampler2D PrevOutSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

void main() {
    vec4 col = texture(DiffuseSampler, texCoord);
    vec4 prevInCol = texture(PrevInSampler, texCoord);

    vec4 difference = abs(col-prevInCol);
    float scale = max(max(difference.r, difference.g), difference.b);

    float c = texture(PrevOutSampler, texCoord).r;
    float u = texture(PrevOutSampler, texCoord + vec2(        0.0, -oneTexel.y)).r;
    float d = texture(PrevOutSampler, texCoord + vec2( oneTexel.x,         0.0)).r;
    float l = texture(PrevOutSampler, texCoord + vec2(-oneTexel.x,         0.0)).r;
    float r = texture(PrevOutSampler, texCoord + vec2(        0.0,  oneTexel.y)).r;

    float ul = texture(PrevOutSampler, texCoord + vec2(-oneTexel.x,  oneTexel.y)).r;
    float dl = texture(PrevOutSampler, texCoord + vec2(-oneTexel.x, -oneTexel.y)).r;
    float ur = texture(PrevOutSampler, texCoord + vec2( oneTexel.x,  oneTexel.y)).r;
    float dr = texture(PrevOutSampler, texCoord + vec2( oneTexel.x, -oneTexel.y)).r;

    float d1 = max(max(u, d), max(l, r));
    float d2 = max(max(ul, dl), max(ur, dr));
    float m = max(max(c, d1*0.95), (d1+d2)*0.485);
    
    m=min(m-(0.5/255), m*0.99);

    col = max(vec4(m), vec4(scale));

    fragColor = vec4(col.rgb, 1.0);
}
