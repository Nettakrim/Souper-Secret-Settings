#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform QuiltedConfig {
    ivec2 Pixels;
    vec2 Blur;
    vec3 Highlight;
    vec3 Exponent;
    vec2 Center;
    vec4 Base;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec3 base = texture(InSampler, texCoord).rgb;

    vec2 oneTexel = 1.0 / InSize;

    vec2 scale = oneTexel*Pixels;
    vec2 start = floor(texCoord/scale + vec2(0.5))*scale;

    vec3 exponent = 1.0/Exponent;

    vec3 lastCol = vec3(0.0);
    vec3 average = vec3(0.0);
    vec3 difference = vec3(0.0);

    float mag = max(abs(Blur.x), abs(Blur.y));
    int s = -int(floor(mag/2.0));
    int e = int(ceil(mag/2.0));
    for (int i = s; i < e; i++) {
        vec3 col = texture(InSampler, start+(oneTexel*Blur*(i/mag))).rgb;
        average += col;
        vec3 diff = pow(max(col-lastCol, 0), exponent);
        if (i > s) {
            difference += diff;
        } else {
            difference += diff*Highlight.y;
        }
        lastCol = col;
    }

    mag = e-s;
    average = mix(average/mag, Base.rgb, Base.a);
    difference *= Highlight.x/max(mag-1, 1);

    vec3 col = average + (difference+Highlight.z)*length((texCoord-start - Center*scale)*InSize);

    fragColor = vec4(mix(base, col, Alpha), 1.0);
}
