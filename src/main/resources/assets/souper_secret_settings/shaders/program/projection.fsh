#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform vec2 Corner00;
uniform vec2 Corner01;
uniform vec2 Corner10;
uniform vec2 Corner11;

void main(){
    vec2 posX0 = mix(Corner00, Corner10, texCoord.x);
    vec2 posX1 = mix(Corner01, Corner11, texCoord.x);
    vec2 pos = mix(posX0, posX1, texCoord.y);

    fragColor = vec4(texture(DiffuseSampler, pos).rgb, 1.0);
}