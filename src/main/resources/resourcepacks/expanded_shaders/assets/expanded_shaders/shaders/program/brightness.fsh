#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Boost;
uniform vec3 Gray;

void main(){
    vec4 col = texture(DiffuseSampler, texCoord);
    
    col = mix(col, 1.0 - (1.0-col)*(1.0-col), Boost);

    fragColor = vec4(col.rgb, 1.0);
}
