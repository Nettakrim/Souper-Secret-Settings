#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D PrevOutSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Threshold;

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main(){
    vec4 col = texture(InSampler, texCoord);
    float depth = LinearizeDepth(texture(InDepthSampler, texCoord).r);
    if (depth > Threshold) {
        col = texture(PrevOutSampler, texCoord);
    }
    fragColor = vec4(col.rgb, 1.0);
}
