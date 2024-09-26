#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main(){
    vec4 col = texture(InSampler, texCoord);

    col/=max(max(col.r,col.g),col.b);

    fragColor = vec4(col.rgb, 1.0);
}
