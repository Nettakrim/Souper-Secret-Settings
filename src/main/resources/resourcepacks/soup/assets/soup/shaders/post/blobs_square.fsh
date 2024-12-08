#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

uniform vec2 Direction;
uniform float Steps;
uniform float Mode;

void main() {
    vec3 colMax = texture(InSampler, texCoord).rgb;
    vec3 colMin = colMax;

    for (float i = 0; i < Steps; i++) {
        float offset = i-Steps/2.0;
        vec3 new = texture(InSampler, texCoord + (offset * Direction * oneTexel)).rgb;
        colMax = max(colMax, new);
        colMin = min(colMin, new);
    }

    fragColor = vec4(mix(colMax, colMin, Mode), 1.0);
}
