#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;
uniform vec3 Gray;
uniform float NoiseScale;
uniform float NoiseAmount;

//https://www.shadertoy.com/view/XdXGW8
vec2 grad( ivec2 z )
{
    int n = z.x+z.y*11111;

    n = (n<<13)^n;
    n = (n*(n*n*15731+789221)+1376312589)>>16;

    // Perlin style vectors
    n &= 7;
    vec2 gr = vec2(n&1,n>>1)*2.0-1.0;
    return ( n>=6 ) ? vec2(0.0,gr.x) :
    ( n>=4 ) ? vec2(gr.x,0.0) :
    gr;
}

float noise( in vec2 p )
{
    ivec2 i = ivec2(floor( p ));
    vec2 f =       fract( p );

    vec2 u = f*f*(3.0-2.0*f); // feel free to replace by a quintic smoothstep instead

    return mix( mix( dot( grad( i+ivec2(0,0) ), f-vec2(0.0,0.0) ),
                     dot( grad( i+ivec2(1,0) ), f-vec2(1.0,0.0) ), u.x),
                mix( dot( grad( i+ivec2(0,1) ), f-vec2(0.0,1.0) ),
                     dot( grad( i+ivec2(1,1) ), f-vec2(1.0,1.0) ), u.x), u.y);
}

vec2 rotate(vec2 v, float angle) {
    float radians = angle / 57.2957795131;
    float s = sin(radians);
    float c = cos(radians);
    return vec2(v.x*c - v.y*s, v.x*s + v.y*c);
}

float pattern(vec2 coord) {
    return length(coord - round(coord));
}

float halftone(float col, vec2 coord, float size, float angle, float edgeScale) {
    return step(col, pattern(rotate(coord, angle)*size)*edgeScale);
}

float black(vec3 col, vec2 coord, float size, float angle, float edgeScale) {
    return halftone(1.0 - (col.r*Gray.r + col.g*Gray.g + col.b*Gray.b), coord, size, angle, edgeScale);
}

void main(){
    vec4 col = texture(InSampler, texCoord);

    vec2 coord = vec2(texCoord.x, texCoord.y * (oneTexel.x/oneTexel.y));

    vec3 sub = vec3((noise(coord * NoiseScale)/NoiseAmount) + (1.0 - (1.0/NoiseAmount)));

    sub.r -= halftone(col.r, coord, 175.0, 0.0, 1.0)*0.9;
    sub.g -= halftone(col.g, coord, 175.0, 15.0, 1.0)*0.9;
    sub.b -= halftone(col.b, coord, 175.0, 75.0, 1.0)*0.9;
    sub *= black(col.rgb, coord, 175.0, 45.0, 3.0);

    fragColor = vec4(sub, 1.0);
}
