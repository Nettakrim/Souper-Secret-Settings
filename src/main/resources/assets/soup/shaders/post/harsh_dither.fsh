#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform HarshDitherConfig {
    int Steps;
    float Mix;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

float getPositionThreshold(vec2 positionInPattern) {
    if (positionInPattern.x == 3) {
        positionInPattern.x = 0;
    }
    if (positionInPattern.y == 3) {
        positionInPattern.y = 0;
    }

    if (positionInPattern.x == 0 || positionInPattern.y == 0) {
        if (positionInPattern.x == positionInPattern.y) {
            return 0.95;
        } else {
            return 0.65;
        }
    }

    if (positionInPattern.x < 3) {
        positionInPattern.x += 1;
    }
    if (positionInPattern.y < 3) {
        positionInPattern.y += 1;
    }

    float threshold = 1-((positionInPattern.x/5)*(positionInPattern.y/5));
    return threshold/1.5;
}

float getChannelOutput(float target, float positionThreshold) {
    if (target > positionThreshold) {
        return 1;
    }
    return 0;
}

void main(){
    vec4 target = texture(InSampler, texCoord);

    vec2 positionInPattern = floor(fract((texCoord*InSize)/6)*6);

    positionInPattern.y = 5-positionInPattern.y;
    float positionThreshold = getPositionThreshold(positionInPattern);

    vec4 color = vec4(getChannelOutput(target.r, positionThreshold), getChannelOutput(target.g, positionThreshold), getChannelOutput(target.b, positionThreshold), 1.0);

    color = mix(color, (floor(target*Steps)/Steps), Mix);

    fragColor = vec4(mix(target, color, Alpha).rgb, 1);
}