#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform SuspiciousConfig {
    uniform float Scale;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

const int XOffset [22] = int[](1, 0, -1, -2, 1, 1, -1, -1, 2, 1, 0, -1, 2, 1, 1, 0, -1, 1, 0, -1, -1, -2);
const int YOffset [22] = int[](-1, -1, -1, -1, -2, 2, -2, 2, 1, 1, 1, 1, 0, 0, 1, 1, 1, -1, -1, -1, 0, 0);

void main(){
    ivec2 pos = ivec2(texCoord*InSize/Scale);

    int i = int((pos.x + pos.y*4) % 22);

    vec2 oneTexel = 1.0 / InSize;
    vec4 col = texture(InSampler, ((pos + vec2(XOffset[i], YOffset[i]) + vec2(0.5))*oneTexel)*Scale);

    fragColor = vec4(mix(texture(InSampler, texCoord), col, Alpha).rgb, 1.0);
}
