#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform AsciiConfig {
    vec3 Scale;
    ivec2 Grid;
    vec2 Levels;
    vec4 Thresholds;
    ivec3 BitShift;
    ivec4 Char1A;
    ivec4 Char1B;
    ivec4 Char2A;
    ivec4 Char2B;
    ivec4 Char3A;
    ivec4 Char3B;
    ivec4 Char4A;
    ivec4 Char4B;
    ivec4 Char5A;
    ivec4 Char5B;
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

bool getChar(ivec4 charA, ivec4 charB, ivec2 index) {
    return (((((index.y/4)%2 > 0 ? charB : charA)[index.y%4] * BitShift.y) ^ BitShift.z) & (BitShift.x << index.x)) > 0;
}

void main() {
    vec2 halfSize = InSize/Scale.xy;
    ivec2 pixelCoord = ivec2(texCoord*InSize/Scale.xy);

    vec3 col = texture(InSampler, mix(pixelCoord, (pixelCoord/Grid)*Grid + Grid/2.0 + 0.5/halfSize, Scale.z)/InSize*Scale.xy).rgb;

    float m = max(max(col.r, col.g), col.b);
    col /= m;
    m *= Levels.y;
    float l = fract(m*0.9999);

    ivec4 charA;
    ivec4 charB;
    if (l < Thresholds.x) {
        charA = Char1A;
        charB = Char1B;
    } else if (l < Thresholds.y) {
        charA = Char2A;
        charB = Char2B;
    } else if (l <= Thresholds.z) {
        charA = Char3A;
        charB = Char3B;
    } else if (l <= Thresholds.w) {
        charA = Char4A;
        charB = Char4B;
    } else {
        charA = Char5A;
        charB = Char5B;
    }

    ivec2 coord = (Grid-ivec2(1)) - (pixelCoord)%Grid;
    ivec2 offset = (ivec2(8) - Grid) / 2;
    if (Grid.x < 8) {
        coord.x += offset.x;
    }
    if (Grid.y < 8) {
        coord.y += offset.y;
    }

    col = round(col*Levels.x)/Levels.x * (getChar(charA, charB, coord) ? ceil(m) : min(floor(m),Levels.y-1))/Levels.y;
    fragColor = vec4(mix(texture(InSampler, texCoord).rgb, col, Alpha), 1.0);
}
