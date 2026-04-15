#version 330

uniform sampler2D InSampler;

layout(std140) uniform HuePerceptualConfig {
    float Rotation;
    vec3 Gray;
    vec3 R;
    vec3 G;
    vec3 B;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

// https://gist.github.com/mairod/a75e7b44f68110e1576d77419d608786
vec3 hue_shift(vec3 color, float dhue) {
    float s = sin(dhue);
    float c = cos(dhue);
    return (color * c) + (color * s) * mat3(R, G, B) + dot(Gray, color) * (1.0 - c);
}

void main(){
    vec4 col = texture(InSampler, texCoord);
    fragColor = vec4(mix(col.rgb, clamp(hue_shift(col.rgb, Rotation * -6.28318530718), 0, 1), Alpha), 1.0);
}
