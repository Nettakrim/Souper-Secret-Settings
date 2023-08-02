#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform vec2 Corner00;
uniform vec2 Corner01;
uniform vec2 Corner10;
uniform vec2 Corner11;

vec2 invert(vec2 v) {
    return ((0.5f/(v-0.5f))+1)/2;
}

void main(){
    vec2 posX0 = mix(invert(Corner00), invert(Corner10), texCoord.x);
    vec2 posX1 = mix(invert(Corner01), invert(Corner11), texCoord.x);
    vec2 pos = mix(posX0, posX1, texCoord.y);

    vec3 col;

    if (pos.x < 0 || pos.x > 1 || pos.y < 0 || pos.y > 1) {
        col = texture(BaseSampler, texCoord).rgb;
    } else {
        col = texture(DiffuseSampler, pos).rgb;
    }

    fragColor = vec4(col, 1.0);
}