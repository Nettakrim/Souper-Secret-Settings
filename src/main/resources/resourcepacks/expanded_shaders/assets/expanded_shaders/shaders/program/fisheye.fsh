#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Curvature;
uniform float Scale;

//https://www.shadertoy.com/view/wtt3z2

void main(){
    vec2 coord = texCoord-vec2(0.5);
    float d=length(coord);
    float z = sqrt(1.0 + d * d * Curvature);
    float r = atan(d, z) / 3.14159;
    r *= Scale;
    float phi = atan(coord.y, coord.x);

    coord = vec2(r*cos(phi),r*sin(phi)) + vec2(0.5);

    vec4 color = texture(DiffuseSampler, coord);

    fragColor = vec4(color.rgb, 1.0);
}
