#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ThermalConfig {
    float LumaRamp;
    float LumaLevel;
    float OutlineBlur;

    vec4 MainHSV;
    vec4 OutlineHSV;

    vec3 Gray;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

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

vec3 GetHSV(vec4 value, float hue) {
    return HSVtoRGB(vec3(fract(hue*value.w + value.x), value.y, value.z));
}

void main() {
    vec2 oneTexel = 1.0 / InSize;
    vec4 center = texture(InSampler, texCoord);
    vec4 up     = texture(InSampler, texCoord + vec2(        0.0, -oneTexel.y*OutlineBlur));
    vec4 up2    = texture(InSampler, texCoord + vec2(        0.0, -oneTexel.y*OutlineBlur) * 2.0);
    vec4 down   = texture(InSampler, texCoord + vec2( oneTexel.x*OutlineBlur,         0.0));
    vec4 down2  = texture(InSampler, texCoord + vec2( oneTexel.x*OutlineBlur,         0.0) * 2.0);
    vec4 left   = texture(InSampler, texCoord + vec2(-oneTexel.x*OutlineBlur,         0.0));
    vec4 left2  = texture(InSampler, texCoord + vec2(-oneTexel.x*OutlineBlur,         0.0) * 2.0);
    vec4 right  = texture(InSampler, texCoord + vec2(        0.0,  oneTexel.y*OutlineBlur));
    vec4 right2 = texture(InSampler, texCoord + vec2(        0.0,  oneTexel.y*OutlineBlur) * 2.0);
    vec4 uDiff = abs(center - up);
    vec4 dDiff = abs(center - down);
    vec4 lDiff = abs(center - left);
    vec4 rDiff = abs(center - right);
    vec4 u2Diff = abs(center - up2);
    vec4 d2Diff = abs(center - down2);
    vec4 l2Diff = abs(center - left2);
    vec4 r2Diff = abs(center - right2);
    vec4 sum = uDiff + dDiff + lDiff + rDiff + u2Diff + d2Diff + l2Diff + r2Diff;
    float sumLuma = 1.0 - dot(clamp(sum, 0.0, 1.0).rgb, Gray);

    // Get luminance of center pixel and adjust
    float centerLuma = dot((center + (center - pow(center, vec4(LumaRamp)))).rgb, Gray);

    // Quantize the luma value
    centerLuma = min(centerLuma - fract(centerLuma * LumaLevel) / LumaLevel, 1.0);

    float h = centerLuma * ((LumaLevel-1)/LumaLevel);
    vec3 color = GetHSV(MainHSV, h);
    vec3 outlineColor = GetHSV(OutlineHSV, h);

    // Blend with outline
    color = color * sumLuma;
    outlineColor = outlineColor * (1.0 - sumLuma);

    fragColor = vec4(mix(center.rgb, color + outlineColor, Alpha), 1.0);
}
