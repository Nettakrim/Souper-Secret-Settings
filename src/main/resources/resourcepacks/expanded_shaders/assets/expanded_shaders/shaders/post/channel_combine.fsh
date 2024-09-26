#version 150

uniform sampler2D InSampler;
uniform sampler2D SecondSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Color1;
uniform vec3 Color2;

void main(){
    vec3 c1 = texture(InSampler, texCoord).rgb*Color1;
    vec3 c2 = texture(SecondSampler, texCoord).rgb*Color2;
    fragColor = vec4(c1+c2, 1.0);
}

