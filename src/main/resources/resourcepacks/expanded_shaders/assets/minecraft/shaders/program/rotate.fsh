#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

vec2 rotate(vec2 v, float angle) {
    float s = sin(angle / 57.2957795131);
    float c = cos(angle / 57.2957795131);
    return vec2(v.x*c - v.y*s, v.x*s + v.y*c);
}

void main(){
    vec2 centered = texCoord-vec2(0.5);

    vec4 col = texture(DiffuseSampler, rotate(centered, 90)+vec2(0.5));

    fragColor = vec4(col.rgb, 1.0);
}
