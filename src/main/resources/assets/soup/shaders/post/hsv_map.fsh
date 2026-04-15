#version 330

uniform sampler2D InSampler;

layout(std140) uniform HSVMapConfig {
    vec3 HueMatrix;
    vec3 SatMatrix;
    vec3 ValMatrix;
    float Direction;
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
    return ((hue(fract(hsv.x)) - 1.0) * hsv.y + 1.0) * hsv.z;
}

vec3 RGBtoHSV(vec3 rgb) {
    vec3 hsv = vec3(0.0);
    hsv.z = max(rgb.r, max(rgb.g, rgb.b));
    float min = min(rgb.r, min(rgb.g, rgb.b));
    float c = hsv.z - min;

    if (c != 0.0)
    {
        hsv.y = c / hsv.z;
        vec3 delta = (hsv.z - rgb) / c;
        delta.rgb -= delta.brg;
        delta.rg += vec2(2.0, 4.0);
        if (rgb.r >= hsv.z) {
            hsv.x = delta.b;
        } else if (rgb.g >= hsv.z) {
            hsv.x = delta.r;
        } else {
            hsv.x = delta.g;
        }
        hsv.x = fract(hsv.x / 6.0);
    }
    return hsv;
}

void main(){
    vec3 rgb = texture(InSampler, texCoord).rgb;
    vec3 RGBAsHSV = HSVtoRGB(vec3(dot(rgb, HueMatrix), dot(rgb, SatMatrix), dot(rgb, ValMatrix)));

    vec3 hsv = RGBtoHSV(rgb);
    vec3 HSVAsRGB = HueMatrix*hsv.x + SatMatrix*hsv.y + ValMatrix*hsv.z;

    fragColor = vec4(mix(rgb, mix(HSVAsRGB, RGBAsHSV, Direction), Alpha), 1.0);
}
