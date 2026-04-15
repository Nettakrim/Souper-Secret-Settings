#version 330

uniform sampler2D InSampler;

layout(std140) uniform GradientMapConfig {
    uniform float Colors;
    uniform float Rounding;
    // this is inputted as 8 seperate vec3s, but the bytes can be directly interpretted as an array!
    uniform vec3[8] Palette;
    uniform vec3 Gray; // a float cant go after the array trick, since vec3 arrays do actually reserve the full vec4 worth of space
};

layout(std140) uniform MergeConfig {
    float Alpha;
};


in vec2 texCoord;

out vec4 fragColor;

vec3 getColor(float index) {
    return Palette[int(mod(index, 8))];
}

void main(){
    vec3 col = texture(InSampler, texCoord).rgb;
    float t = dot(col, Gray)*Colors;
    if (t < 0) {
        t += 7;
    }
    t = mix(t, Rounding > 0 ? ceil(t) : floor(t), abs(Rounding));
    fragColor = vec4(mix(col, mix(getColor(floor(t)), getColor(ceil(t)), fract(t)), Alpha), 1.0);
}
