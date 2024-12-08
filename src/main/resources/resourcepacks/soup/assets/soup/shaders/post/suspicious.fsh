#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int XOffset [22] = int[22](1, 0, -1, -2, 1, 1, -1, -1, 2, 1, 0, -1, 2, 1, 1, 0, -1, 1, 0, -1, -1, -2);
uniform int YOffset [22] = int[22](-1, -1, -1, -1, -2, 2, -2, 2, 1, 1, 1, 1, 0, 0, 1, 1, 1, -1, -1, -1, 0, 0);

void main(){
    vec2 pos = floor(texCoord/oneTexel);

    int i = int(mod(pos.x + pos.y*4, 22.0));

    vec4 col = texture(InSampler, texCoord + oneTexel*vec2(XOffset[i], YOffset[i]));

    fragColor = vec4(col.rgb, 1.0);
}
