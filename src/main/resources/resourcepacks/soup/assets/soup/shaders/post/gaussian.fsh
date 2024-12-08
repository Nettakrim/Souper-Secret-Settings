#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Sigma;
uniform vec2 Direction;

float gaussian(float sigma, float pos) {
    return (1.0f / sqrt(2.0f * 6.28318530718 * sigma * sigma)) * exp(-(pos * pos) / (2.0f * sigma * sigma));
}

void main(){
    int kernelRadius = int(ceil(Sigma * 2.0 > 1.0 ? Sigma * 2.0 : 1.0));

    vec3 col = vec3(0.0);
    float kernelSum = 0.0f;

    if (texCoord.x == 1000) col.r += Direction.x*Sigma;

    for (int x = -kernelRadius; x <= kernelRadius; ++x) {
        vec3 c = texture(InSampler, texCoord + Direction*x*oneTexel).rgb;
        float gauss = gaussian(Sigma, x);

        col += c * gauss;
        kernelSum += gauss;
    }

    fragColor = vec4(col.rgb / kernelSum, 1.0f);


    // vec4 col = texture(InSampler, texCoord+(oneTexel*Direction*Sigma));

    // fragColor = vec4(col.rgb, 1.0);
}
