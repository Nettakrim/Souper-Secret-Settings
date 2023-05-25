#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 Ratio;
uniform vec2 Scale;

void main(){
    vec4 color = texture(DiffuseSampler, texCoord);

    vec2 coord = abs(texCoord-vec2(0.5));

    float ratioMultiplier = (Ratio.x/Ratio.y)/(oneTexel.y/oneTexel.x);
    if (ratioMultiplier > 1) {
        coord.y *= ratioMultiplier;
    } else {
        coord.x /= ratioMultiplier;
    }
    coord /= Scale;

    if (coord.x > 0.5 || coord.y > 0.5) {
        color = vec4(0);
    }

    fragColor = vec4(color.rgb, 1);
}