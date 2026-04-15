#version 330

uniform sampler2D InSampler;

layout(std140) uniform DramaticConfig {
    float Boost;
    float Clamp;
    vec3 Gray;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 col = texture(InSampler, texCoord);
    
    float scale = min((col.r*Gray.r + col.g*Gray.g + col.b*Gray.b)*Boost, Clamp);

    fragColor = vec4(mix(col.rgb, col.rgb * scale, Alpha), 1.0);
}
