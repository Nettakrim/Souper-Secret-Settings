#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform vec3 BackgroundColor;
uniform float NearOpacity;
uniform float FarOpacity;

void main() {
    vec4 col;
    if (texCoord.y < 0.25 || texCoord.y > 0.75) {
        float d = 4*abs(0.5-texCoord.y)-1.0;
        d = d*d*(3.0 - 2.0*d);
        col = mix(mix(texture(DiffuseSampler, texCoord), texture(BaseSampler, texCoord), 0.5), vec4(BackgroundColor, 0), d*(FarOpacity-NearOpacity)+NearOpacity);
    } else if (texCoord.x < 0.5) {
        col = texture(DiffuseSampler, (texCoord*2)-vec2(0,0.5));
    } else {
        col = texture(BaseSampler, (texCoord*2)-vec2(1.0,0.5));
    }

    fragColor = vec4(col.rgb, 1.0);
}
