#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float MinPopulation;
uniform float MaxPopulation;
uniform float Creation;
uniform vec3 AliveThreshold;

out vec4 fragColor;

float life(float current, float neighbours) {
    if (neighbours+0.5 < MinPopulation) {
        return 0;
    } else if (neighbours-0.5 > MaxPopulation) {
        return 0;
    } else if (neighbours-0.5 < Creation && neighbours+0.5 > Creation) {
        return 1;
    } else {
        return current;
    }
}

void main() {
    vec3 new = step(AliveThreshold, texture(InSampler, texCoord).rgb);

    vec3 count = texture(PrevSampler, texCoord+vec2( oneTexel.x,  oneTexel.y)).rgb
               + texture(PrevSampler, texCoord+vec2( oneTexel.x,  0         )).rgb
               + texture(PrevSampler, texCoord+vec2( oneTexel.x, -oneTexel.y)).rgb
               + texture(PrevSampler, texCoord+vec2(-oneTexel.x,  oneTexel.y)).rgb
               + texture(PrevSampler, texCoord+vec2(-oneTexel.x,  0         )).rgb
               + texture(PrevSampler, texCoord+vec2(-oneTexel.x, -oneTexel.y)).rgb
               + texture(PrevSampler, texCoord+vec2( 0         ,  oneTexel.y)).rgb
               + texture(PrevSampler, texCoord+vec2( 0         , -oneTexel.y)).rgb;

    vec3 current = texture(PrevSampler, texCoord).rgb;

    vec3 iteration = vec3(life(current.r, count.r), life(current.g, count.g), life(current.b, count.b));
    fragColor = vec4(max(new, iteration), 1.0);
}
