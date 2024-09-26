#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

uniform float GameTime;
uniform float Threshold;

void main() {
    vec3 col = texture(InSampler, texCoord).rgb;
    vec3 prev = texture(PrevSampler, texCoord).rgb;

    float d = texCoord.y;

    fragColor = vec4(mix(prev, col, mod((GameTime*1200)+d, 1.0) < Threshold ? 1.0 : 0.0), 1.0);
}
