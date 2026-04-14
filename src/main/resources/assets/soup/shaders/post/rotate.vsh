#version 330

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform RotateConfig {
    float Angle;
    vec2 X;
    vec2 Y;
    vec2 Offset;
    float Squish;
};

in vec4 Position;

out vec2 texCoord;

vec2 scale = vec2(mix(1.0, OutSize.x/OutSize.y, Squish), 1.0);

vec2 rotate(vec2 v, float angle) {
    v *= scale;
    float radians = angle / 57.2957795131;
    float s = sin(radians);
    float c = cos(radians);
    return vec2(v.x*c - v.y*s, v.x*s + v.y*c);
}

void main(){
    vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
    vec4 pos = vec4(uv * vec2(2, 2) + vec2(-1, -1), 0, 1);

    gl_Position = pos;

    texCoord = uv;

    vec2 centered = texCoord-vec2(0.5);
    vec2 v = mat3x2(X, Y, Offset) * vec3(rotate(centered, Angle), 1.0);
    v /= scale;
    texCoord = v + vec2(0.5);
}
