#version 150

uniform sampler2D InSampler;
uniform sampler2D LookupSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float GridSize;

out vec4 fragColor;

void main() {
    vec3 col = texture(InSampler, texCoord).rgb*255.0/256.0;

    vec2 coord = vec2((col.r+floor(fract(col.b*GridSize)))/GridSize, (col.g+floor(col.b*GridSize))/GridSize);

    fragColor = vec4(texture(LookupSampler, coord).rgb, 1.0);
}
