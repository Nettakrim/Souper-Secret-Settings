#version 330

uniform sampler2D InSampler;

layout(std140) uniform HalfInvertConfig {
    uniform float Iterations;
    uniform vec3 Subtract;
    uniform vec3 Multiply;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

const float roundingFactor = 0.5/255.0;

//mimics the way floats are converted into 8 bit fixed point
float limit(float f) {
    f = clamp(f,0,1);
    float i = f >= 0.5 ? -roundingFactor : roundingFactor;
    return round(255.0*(f - (mod(i - f, 1.0/16.0) - i) / 256.0))/255.0;
}

vec3 limit(vec3 v) {
    return vec3(limit(v.x), limit(v.y), limit(v.z));
}

void main(){
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 col = base;

    float a = abs(Iterations);
    float s = sign(Iterations);
    for (int i = 0; i < a; i++) {
        vec3 target = limit(abs(col - Subtract) * Multiply);
        col = mix(col, target, s*min(a-i,1));
    }

    fragColor = vec4(mix(base, col, Alpha), 1.0);
}
