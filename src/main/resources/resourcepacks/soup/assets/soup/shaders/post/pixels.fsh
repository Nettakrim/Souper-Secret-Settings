#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int PixelSize;

void main(){
    vec2 roundedPosition = floor(texCoord/oneTexel / PixelSize) * PixelSize;

    roundedPosition = roundedPosition *oneTexel;
    vec4 ditherTarget = texture(InSampler, roundedPosition);

    fragColor = vec4(ditherTarget.rgb, 1.0);
}
