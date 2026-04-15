#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform S2xEConfig {
    vec3 Pos;
    vec3 Rotation;
    vec2 Curvature;
    float Wrapping;
    float Fov;
    float Pitch;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

vec4 wrapTexture(sampler2D tex, vec2 coord) {
    return texture(tex, mix(coord, fract(coord), Wrapping));
}

mat3 rotationAroundAxis(vec3 axis, float rotation)
{
    float s = sin(rotation);
    float c = cos(rotation);
    float one_minus_c = 1.0 - c;
    axis = normalize(axis);

    return mat3(
        one_minus_c * axis.x * axis.x + c, one_minus_c * axis.x * axis.y - axis.z * s, one_minus_c * axis.z * axis.x + axis.y * s,
        one_minus_c * axis.x * axis.y + axis.z * s, one_minus_c * axis.y * axis.y + c, one_minus_c * axis.y * axis.z - axis.x * s,
        one_minus_c * axis.z * axis.x - axis.y * s, one_minus_c * axis.y * axis.z + axis.x * s, one_minus_c * axis.z * axis.z + c
    );
}

mat2 rotation2D(float rotation) {
    rotation *= 6.28318530718;
    float s = sin(rotation);
    float c = cos(rotation);
    return mat2(c, s, -s, c);
}

void main(){
    vec4 base = texture(InSampler, texCoord);

    float aspect = InSize.x/InSize.y;
    float yTan = tan(Fov/114.591559);
    vec3 view = normalize(vec3(yTan * (texCoord.x*2.0 - 1.0) * aspect, yTan * (texCoord.y*2.0 - 1.0), -1));
    view.yz *= rotation2D(Pitch);

    vec3 pos = Pos;
    mat2 xRotation = rotation2D(Rotation.x);
    mat2 yRotation = rotation2D(Rotation.y);
    pos.xy *= xRotation;
    pos.zy *= yRotation;
    view.xy *= xRotation;
    view.zy *= yRotation;

    vec3 axis = cross(pos, view);
    float height = length(pos);
    vec3 nPos = pos/height;

    float eSlope = dot(nPos, view);
    float sSlope = length(view - nPos*eSlope);

    float arc = (height-1) / -eSlope;

    vec3 hit = normalize((pos + (nPos*eSlope*arc)) * rotationAroundAxis(axis, arc * sSlope));

    vec2 uv = (hit.xz*pow(1+hit.y, Curvature.x)*Curvature.y/2) * rotation2D(Rotation.z);
    uv.y *= -aspect;

    vec4 projected = wrapTexture(InSampler, uv + vec2(0.5));

    fragColor = vec4(mix(base.rgb, projected.rgb, Alpha), 1.0);
}
