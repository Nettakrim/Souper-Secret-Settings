#version 330

uniform sampler2D InSampler;

layout(std140) uniform BrightnessConfig {
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
    
    vec3 value = mix(col, 1.0 - pow(1.0 - col, Power), Amount);

    fragColor = vec4(mix(col, clamp(value, 0.0, 1.0), Alpha), 1.0);
}
