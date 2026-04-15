#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform WallsConfig {
    float Scale;
    vec2 DepthMix;
    vec4 TextureScale;

    vec4 FloorUV;
    vec4 WallUV;
    vec4 CeilUV;

    vec2 Clipping;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

float LinearizeDepth(float depth) {
    return (Clipping.x*Clipping.y) / (depth * (Clipping.x - Clipping.y) + Clipping.y);
}

void main(){
    vec2 samplePos;
    vec4 uv;
    vec2 scale;

    float horizonPosition = texCoord.y*2.0 - 1.0;
    float depth = LinearizeDepth(texture(InDepthSampler, vec2(texCoord.x, mix(DepthMix.y, texCoord.y, DepthMix.x))).r);
    float height = Scale/depth;
    if (abs(horizonPosition) < height) {
        samplePos = vec2((texCoord.x-0.5)*depth, ((horizonPosition/height)+1)/2);
        scale = TextureScale.xy;
        uv = WallUV;
    } else {
        samplePos = vec2(Scale*(texCoord.x-0.5)/abs(horizonPosition), 1/horizonPosition);
        scale = TextureScale.zw;
        uv = horizonPosition < 0 ? FloorUV : CeilUV;
    }

    samplePos = fract(samplePos*scale+vec2(0.5, 0));
    vec4 col = texture(InSampler, vec2(mix(uv.x, uv.z, samplePos.x), mix(uv.y, uv.w, samplePos.y)));

    fragColor = vec4(mix(texture(InSampler, texCoord), col, Alpha).rgb, 1.0);
}
