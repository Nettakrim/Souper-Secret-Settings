#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Boost;
uniform float Clamp;
uniform vec3 Gray;

void main(){
    vec4 col = texture(DiffuseSampler, texCoord);
    
    float scale = min((col.r*Gray.r + col.g*Gray.g + col.b*Gray.b)*Boost, Clamp);

    fragColor = vec4(col.rgb * scale, 1.0);
}
