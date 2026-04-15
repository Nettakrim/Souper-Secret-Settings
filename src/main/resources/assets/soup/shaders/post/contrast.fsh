#version 330

uniform sampler2D InSampler;

layout(std140) uniform ContrastConfig {
    float Amount;
    vec3 Power;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec3 col = texture(InSampler, texCoord).rgb;

    vec3 value = mix(col, pow(col, Power) * (3.0 - 2.0*col), Amount);

    fragColor = vec4(mix(col, clamp(value, 0.0, 1.0), Alpha), 1.0);
}
