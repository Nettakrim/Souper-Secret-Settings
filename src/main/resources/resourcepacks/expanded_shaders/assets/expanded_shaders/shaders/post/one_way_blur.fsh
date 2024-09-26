#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 BlurDir;
uniform float Radius;
uniform float Curve;

void main(){
    vec3 col = vec3(0);
    float total = 0;
    for (float i = 0; i <= Radius; i++) {
        float weight = 1.0-(i/Radius);
        weight = mix(weight, weight * weight, Curve);
        total += weight;
        col += texture(InSampler, texCoord-(BlurDir*oneTexel*i)).rgb * weight;
    }
    col /= total;

    fragColor = vec4(col, 1.0);
}
