#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Scale;

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main(){
    vec4 col = texture(DiffuseSampler, texCoord);
    float depth = LinearizeDepth(texture(DiffuseDepthSampler, texCoord).r);
    float brightness = min((Scale/depth), 1.0);
    float s = (brightness*brightness);
    fragColor = vec4(col.rgb*(s+brightness-s*brightness), 1.0);
}
