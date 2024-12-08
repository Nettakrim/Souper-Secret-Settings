#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 Ratio;
uniform vec2 Scale;
uniform float Squish;

void main(){
    vec2 coord = texCoord-vec2(0.5);

    float ratioMultiplier = (Ratio.x/Ratio.y)/(oneTexel.y/oneTexel.x);
    if (ratioMultiplier > 1) {
        coord.y *= ratioMultiplier;
    } else {
        coord.x /= ratioMultiplier;
    }
    coord /= Scale;

    vec4 color = texture(InSampler, mix(texCoord, coord + vec2(0.5), Squish));

    if (abs(coord.x) > 0.5 || abs(coord.y) > 0.5) {
        color = vec4(0);
    }

    fragColor = vec4(color.rgb, 1);
}