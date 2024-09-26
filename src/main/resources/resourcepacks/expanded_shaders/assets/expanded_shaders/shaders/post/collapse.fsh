#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float GameTime;
uniform float MaxDistance;

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

void main(){
    vec2 aspect = vec2(1, oneTexel.x/oneTexel.y);
    vec2 randomPos = ((texCoord*vec2(100,0.1))+vec2(43.123))*aspect;
    float t = fract(noise(randomPos*300.123)+mod(GameTime*1200, 1));

    vec3 center = texture(InSampler, texCoord).rgb;
    vec3 down = texture(InSampler, texCoord - vec2(0, oneTexel.y*t*MaxDistance)).rgb;

    vec3 col = mix(down, center, t);

    fragColor = vec4(col, 1.0);
}
