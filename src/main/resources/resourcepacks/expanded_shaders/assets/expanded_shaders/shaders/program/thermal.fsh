#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float LumaRamp;
uniform float LumaLevel;
uniform float OutlineBlur;

out vec4 fragColor;

vec3 hue(float h) {
    float r = abs(h * 6.0 - 3.0) - 1.0;
    float g = 2.0 - abs(h * 6.0 - 2.0);
    float b = 2.0 - abs(h * 6.0 - 4.0);
    return clamp(vec3(r,g,b), 0.0, 1.0);
}

vec3 HSVtoRGB(vec3 hsv) {
    return ((hue(hsv.x) - 1.0) * hsv.y + 1.0) * hsv.z;
}

void main(){
    vec4 center = texture(DiffuseSampler, texCoord);
    vec4 up     = texture(DiffuseSampler, texCoord + vec2(        0.0, -oneTexel.y*OutlineBlur));
    vec4 up2    = texture(DiffuseSampler, texCoord + vec2(        0.0, -oneTexel.y*OutlineBlur) * 2.0);
    vec4 down   = texture(DiffuseSampler, texCoord + vec2( oneTexel.x*OutlineBlur,         0.0));
    vec4 down2  = texture(DiffuseSampler, texCoord + vec2( oneTexel.x*OutlineBlur,         0.0) * 2.0);
    vec4 left   = texture(DiffuseSampler, texCoord + vec2(-oneTexel.x*OutlineBlur,         0.0));
    vec4 left2  = texture(DiffuseSampler, texCoord + vec2(-oneTexel.x*OutlineBlur,         0.0) * 2.0);
    vec4 right  = texture(DiffuseSampler, texCoord + vec2(        0.0,  oneTexel.y*OutlineBlur));
    vec4 right2 = texture(DiffuseSampler, texCoord + vec2(        0.0,  oneTexel.y*OutlineBlur) * 2.0);
    vec4 uDiff = abs(center - up);
    vec4 dDiff = abs(center - down);
    vec4 lDiff = abs(center - left);
    vec4 rDiff = abs(center - right);
    vec4 u2Diff = abs(center - up2);
    vec4 d2Diff = abs(center - down2);
    vec4 l2Diff = abs(center - left2);
    vec4 r2Diff = abs(center - right2);
    vec4 sum = uDiff + dDiff + lDiff + rDiff + u2Diff + d2Diff + l2Diff + r2Diff;
    vec4 gray = vec4(0.3, 0.59, 0.11, 0.0);
    float sumLuma = 1.0 - dot(clamp(sum, 0.0, 1.0), gray);

    // Get luminance of center pixel and adjust
    float centerLuma = dot(center + (center - pow(center, vec4(LumaRamp))), gray);

    // Quantize the luma value
    centerLuma = min(centerLuma - fract(centerLuma * LumaLevel) / LumaLevel, 1.0);

    float h = centerLuma * ((LumaLevel-1)/LumaLevel);
    vec3 color = HSVtoRGB(vec3(h, 1 , 1));

    vec3 outlineColor = HSVtoRGB(vec3(h, 1 , 0.5));

    // Blend with outline
    color = color * sumLuma;
    outlineColor = outlineColor * (1.0 - sumLuma);

    fragColor = vec4(color + outlineColor, 1.0);
}
