#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform HorrorConfig {
    float Scale;
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
    vec4 col = texture(InSampler, texCoord);
    float depth = LinearizeDepth(texture(InDepthSampler, texCoord).r);
    float brightness = min((Scale/depth), 1.0);
    float s = (brightness*brightness);
    fragColor = vec4(col.rgb*(1-(1-(s+brightness-s*brightness))*Alpha), 1.0);
}
