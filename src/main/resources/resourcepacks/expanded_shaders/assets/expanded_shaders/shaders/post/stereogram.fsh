#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Stripes;
uniform float Time;

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

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

//https://github.com/piellardj/stereogram-webgl
//https://www.shadertoy.com/view/XtlGRn

float sampleHeightmap(vec2 position) {
    float depth = LinearizeDepth(texture(InDepthSampler, position).r);

    depth = max(min(1.0-(depth/10.0), 1.0), 0.0);

    return depth*depth;
}

vec2 originalPosition(vec2 position) {
    float width = 1/Stripes;
    float maxStep = width/2;
    for(int count = 0; count < Stripes*2; count++) {
        if(position.x < width) {
            break;
        }

        float d = sampleHeightmap(position);

        position.x -= width - (d * maxStep);
    }
    return position;
}

float sampleNoise(vec2 coords) {
    float x = fract(coords.x*Stripes)/Stripes;
    float y = coords.y*(oneTexel.x/oneTexel.y);

    vec2 random = vec2(Time*123.456, Time*456.789);

    return noise((vec2(x,y)+random)*400);
}

void main(){
    vec2 dotCalc = (texCoord - vec2(0.5, 0.95))*vec2(1,oneTexel.x/oneTexel.y);
    float dotRadius = 0.005;
    if (min(distance(dotCalc, vec2(0.5/Stripes, 0)), distance(dotCalc, vec2(-(0.5/Stripes), 0))) < dotRadius) {
        fragColor = vec4(vec3(1.0), 1.0);
    } else {
        vec2 position = originalPosition(texCoord);

        float noise = sampleNoise(position);

        noise = (noise+1.0)/2.0;
        noise = noise*noise*(3.0-2.0*noise);
        noise = 2*noise - 1.0;

        vec2 offset = vec2(1.0/Stripes, 0.0);
        vec4 color = mix(texture(InSampler, texCoord + offset), texture(InSampler, texCoord), 0.65);

        if (noise < 0) color *= noise+1.0;
        else color = vec4(1.0)-((vec4(1.0)-color)*(1.0-noise));
        color = mix(color, vec4((noise+1.0)/2.0), 0.85);

        fragColor = vec4(color.rgb, 1.0);
    }
}
