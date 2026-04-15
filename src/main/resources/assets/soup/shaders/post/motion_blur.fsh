#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform MotionBlurConfig {
    float Steps;
    float BaseMix;
    vec2 RotationScale;
    float Wrapping;
    vec4 Offset;
    float Fov;
    float Pitch;
    float PitchDelta;
    float YawDelta;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

mat3 pitch(float rotation) {
    rotation /= 57.2957795131;
    float s = sin(rotation);
    float c = cos(rotation);
    return mat3(1, 0, 0, 0, c, -s, 0, s, c);
}

mat3 yaw(float rotation) {
    rotation /= 57.2957795131;
    float s = sin(rotation);
    float c = cos(rotation);
    return mat3(c, 0, s, 0, 1, 0, -s, 0, c);
}

vec4 wrapTexture(sampler2D tex, vec2 coord) {
    return texture(tex, mix(coord, fract(coord), Wrapping));
}

void main(){
    float near = 0.1;
    float far = 1000.0;
    float aspect = InSize.x/InSize.y;
    float yTan = tan(Fov/114.591559);
    float yCotan = 1.0/yTan;
    mat4 projection = mat4(yCotan/aspect, 0, 0, 0, 0, yCotan, 0, 0, 0, 0, (far+near)/(near-far), (2*far*near)/(near-far), 0, 0, -1, 0);

    vec3 pos = (vec3(yTan * (texCoord.x*2.0 - 1.0) * aspect, yTan * (texCoord.y*2.0 - 1.0), -Offset.w) + Offset.xyz);
    pos *= pitch(-Pitch);

    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 colAcc = base * BaseMix;

    mat3 pitchCorrection = pitch(Pitch);
    mat3 pitchStep = pitch(PitchDelta/Steps * RotationScale.x);
    float yawStep = YawDelta/Steps * RotationScale.y;
    float count = floor(abs(Steps));
    for (int i = 1; i < count; i++) {
        pos *= pitchStep;
        vec4 projected = vec4(pos * (yaw(yawStep * i) * pitchCorrection), 0.0) * projection;
        vec2 uv = ((projected.xy / projected.z) + vec2(1.0)) / 2;

        colAcc += wrapTexture(InSampler, uv).rgb;
    }

    colAcc /= count - (1-BaseMix);

    fragColor = vec4(mix(base, colAcc, Alpha), 1.0);
}
