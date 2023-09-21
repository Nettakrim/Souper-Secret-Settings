#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Boost;

void main(){
    vec4 col = texture(DiffuseSampler, texCoord);

    vec4 contrast = col*col*(3.0 - 2.0*col);

    fragColor = vec4(mix(col, contrast, Boost).rgb, 1.0);
}
