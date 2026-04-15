#version 330

uniform sampler2D InSampler;

layout(std140) uniform XorConfig {
    ivec3 Value;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

float xor(float c, int v) {
    return (int(c*255.0) ^ v)/255.0;
}

void main(){
    vec4 col = texture(InSampler, texCoord);

    fragColor = vec4(mix(col.rgb, vec3(xor(col.r, Value.r), xor(col.g, Value.g), xor(col.b, Value.b)), Alpha), 1.0);
}
