#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main(){
    vec2 ditherPosition = floor(texCoord/oneTexel / 6) * 6;

    ditherPosition = ditherPosition*oneTexel;
    vec4 ditherTarget = texture(DiffuseSampler, ditherPosition);

    fragColor = vec4(ditherTarget.rgb, 1.0);
}
