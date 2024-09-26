#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float ThresholdBrightness;
uniform float ThresholdSlope;
uniform vec3 Luminance;

void main(){
    vec4 color = texture(InSampler, texCoord);
    float luminance = dot(Luminance, color.rgb);
    if (luminance > ThresholdBrightness) {
        fragColor = (color/max(max(color.r, color.g), color.b))*min(((luminance-ThresholdBrightness)/(1.0-ThresholdBrightness)*ThresholdSlope), 1.0);
    } else {
        fragColor = vec4(0.0);
    }
}
