#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform vec3 Gray;
uniform vec3 Mask;
uniform vec3 Threshold;

float maxRGB(vec3 v) {
    return max(max(v.r, v.g), v.b);
}

void main(){
    vec3 col = texture(InSampler, texCoord).rgb;

    if (maxRGB(col*Mask) > maxRGB(col*Threshold)) {
        fragColor = vec4(col.rgb, 1.0);
    } else {
        fragColor = vec4(vec3(col.r*Gray.r + col.g*Gray.g + col.b*Gray.b), 1.0);
    }
}
