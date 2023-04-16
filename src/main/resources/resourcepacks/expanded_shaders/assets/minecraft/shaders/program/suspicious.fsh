#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int XOffset [24] = int[24](1, 0, -1, -2, 1, 1, -1, -1, 2, 1, 0, -1, 2, 1, 1, 0, -1, 1, 0, -1, -1, -2, 1, 0);
uniform int YOffset [24] = int[24](-1, -1, -1, -1, -2, 2, -2, 2, 1, 1, 1, 1, 0, 0, 1, 1, 1, -1, -1, -1, 0, 0, 1, 1);

void main(){
    vec2 pos = floor(texCoord/oneTexel);

    int i = int(mod(pos.x + pos.y*4, 22));

    vec4 col = texture(DiffuseSampler, texCoord + oneTexel*vec2(XOffset[i], YOffset[i]));

    fragColor = vec4(col.rgb, 1.0);
}
