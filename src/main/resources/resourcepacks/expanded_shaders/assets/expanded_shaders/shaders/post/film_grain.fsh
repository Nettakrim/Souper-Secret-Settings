#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Time;
uniform float Rate;
uniform float Pitch;
uniform float Strength;
uniform float ColorStrength;
uniform float MixMode;

//https://www.shadertoy.com/view/3sGSWV

float hash(vec3 p3){
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

// From iq: https://www.shadertoy.com/view/4sfGzS
float noise(vec3 x){
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f*f*(3.0-2.0*f);
    return mix(mix(mix(hash(i+vec3(0, 0, 0)),
                       hash(i+vec3(1, 0, 0)),f.x),
                   mix(hash(i+vec3(0, 1, 0)),
                       hash(i+vec3(1, 1, 0)),f.x),f.y),
               mix(mix(hash(i+vec3(0, 0, 1)),
                       hash(i+vec3(1, 0, 1)),f.x),
                   mix(hash(i+vec3(0, 1, 1)),
                       hash(i+vec3(1, 1, 1)),f.x),f.y),f.z);
}

float grain_source(vec3 x, float strength, float pitch){
    float center = noise(x);
    float v1 = center - noise(vec3( 1, 0, 0)/pitch + x) + 0.5;
    float v2 = center - noise(vec3( 0, 1, 0)/pitch + x) + 0.5;
    float v3 = center - noise(vec3(-1, 0, 0)/pitch + x) + 0.5;
    float v4 = center - noise(vec3( 0,-1, 0)/pitch + x) + 0.5;

    float total = (v1 + v2 + v3 + v4) / 4.0;
    return mix(1.0, 0.5 + total, strength);
}

void main(){
    vec2 pixel = texCoord/oneTexel;

    const float grain_strength = 1.0;
    const float grain_rate = 60.0;
    const float grain_pitch = 1.0;

    float rg = grain_source(vec3(pixel, floor(Rate*(Time))),     Strength, Pitch);
    float gg = grain_source(vec3(pixel, floor(Rate*(Time+9.0))), Strength, Pitch);
    float bg = grain_source(vec3(pixel, floor(Rate*(Time-9.0))), Strength, Pitch);

    vec3 grain = vec3(rg, gg, bg);
    grain = mix(vec3(dot(grain, vec3(0.2126, 0.7152, 0.0722))), grain, ColorStrength);

    vec3 color = texture(DiffuseSampler, texCoord).rgb;
    color = max(mix(color*grain, color+(grain-1.0), MixMode), 0.0);

    fragColor = vec4(color.rgb, 1);
}