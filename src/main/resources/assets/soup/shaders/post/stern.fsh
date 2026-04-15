#version 330

uniform sampler2D InSampler;

layout(std140) uniform SternConfig {
    uniform vec4 Left;
    uniform vec4 Right;
    uniform ivec4 Bitshifts;
    uniform float Angle;
    uniform vec2 Range;
    uniform float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec3 base = texture(InSampler, texCoord).rgb;
    
    ivec3 bits = ivec3(base*255.0);
    vec2 r = vec2(cos(Angle*6.2831853071), sin(Angle*6.2831853071));
    vec2 g = r;
    vec2 b = r;

    mat2x2 L = mat2x2(Left);
    mat2x2 R = mat2x2(Right);

    for (int i = Bitshifts.x; i < Bitshifts.y; i++) {
        ivec3 layer = bits & (Bitshifts.z << i);

        r = layer.r > Bitshifts.w ? r*R : r*L;
        g = layer.g > Bitshifts.w ? g*R : g*L;
        b = layer.b > Bitshifts.w ? b*R : b*L;
    }

    vec3 col = (vec3(r.x/r.y, g.x/g.y, b.x/b.y) + Range.x) * Range.y;

    fragColor = vec4(mix(base, clamp(col,0,1), Alpha), 1.0);
}
