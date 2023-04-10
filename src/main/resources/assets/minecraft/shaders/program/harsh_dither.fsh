#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

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
    vec4 target = texture(DiffuseSampler, texCoord);

    vec2 positionInPattern = floor(fract((texCoord/oneTexel)/6)*6);
    
    positionInPattern.y = 5-positionInPattern.y;
    float positionThreshold = getPositionThreshold(positionInPattern);

    vec4 color = vec4(getChannelOutput(target.r, positionThreshold), getChannelOutput(target.g, positionThreshold), getChannelOutput(target.b, positionThreshold), 1.0);

    color = (color+(floor(target*4)/4))/2;

    fragColor = color;
}
