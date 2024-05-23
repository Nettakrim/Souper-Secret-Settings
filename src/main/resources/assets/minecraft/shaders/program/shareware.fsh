#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float ColorResolution;
uniform float Saturation;

out vec4 fragColor;

void main() {
    vec2 scaleFactors = InSize / 2;
    vec2 truncPos = floor(texCoord * scaleFactors) / scaleFactors;
    vec4 baseTexel = texture2D(DiffuseSampler, truncPos);
    vec3 truncTexel = floor(baseTexel.rgb * ColorResolution) / ColorResolution;

    float luma = dot(truncTexel, vec3(0.3, 0.59, 0.11));
    vec3 chroma = (truncTexel - luma) * Saturation;
    fragColor = vec4(luma + chroma, 1.0);
}