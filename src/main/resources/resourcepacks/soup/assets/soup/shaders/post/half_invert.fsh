#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 col = texture(InSampler, texCoord);

    col = abs(col-vec4(0.499))*2;

    fragColor = vec4(col.rgb, 1.0);
}
