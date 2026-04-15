#version 330

uniform sampler2D InSampler;

layout(std140) uniform KaleidoscopeConfig {
    vec2 Mirrors;
    vec2 Position;
    float Wrapping;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

vec4 wrapTexture(sampler2D tex, vec2 coord) {
    return texture(tex, mix(coord, fract(coord), Wrapping));
}

vec2 mirrorAlongLines(vec2 point, vec3 lineA, vec3 lineB, float iterations) {
    for (int i = 0; i < iterations; i++) {
        vec3 a;
        vec3 b;
        if (i%2 == 0) {
            a = lineA;
            b = lineB;
        } else {
            a = lineB;
            b = lineA;
        }

        float distanceA = a.x*point.x + a.y*point.y + a.z;
        float distanceB = b.x*point.x + b.y*point.y + b.z;
        if (distanceA >= 0 && distanceB >= 0) {
            return point;
        }

        point -= a.xy*distanceA*2;
    }

    return point;
}

vec3 getLine(float angle, float offset) {
    return vec3(cos(angle), -sin(angle), offset);
}

void main(){
    vec2 coord = mirrorAlongLines(vec2(texCoord.x-0.5, texCoord.y-0.5), getLine(Mirrors.y*6.28318530718, Position.x), getLine((Mirrors.y+(1.0/Mirrors.x)+0.5)*6.28318530718, Position.y), abs(Mirrors.x-1));
    vec4 col = wrapTexture(InSampler, vec2(coord.x+0.5, coord.y+0.5));
    fragColor = mix(texture(InSampler, texCoord), col, Alpha);
}
