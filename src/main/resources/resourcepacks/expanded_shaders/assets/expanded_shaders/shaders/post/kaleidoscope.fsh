#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

vec2 mirrorAxis(vec2 point, float x, float y) {
    return vec2(point.x*x, point.y*y);
}

vec2 mirrorDiagonal(vec2 point) {
    return vec2(point.y, point.x);
}

void main(){
    vec2 coord = vec2(texCoord.x-0.5, texCoord.y-0.5);

    if (coord.y > 0) {
        if (coord.x < 0) {
            coord = mirrorDiagonal(mirrorAxis(coord, -1, 1));
        }
    } else {
        if (coord.x < 0) {
            coord = mirrorAxis(coord, -1, -1);
        } else {
            coord = mirrorDiagonal(mirrorAxis(coord, 1, -1));
        }
    }

    if (coord.y < coord.x) {
        coord = mirrorDiagonal(coord);
    }

    vec4 col = texture(InSampler, vec2(coord.x+0.5, coord.y+0.5));
    fragColor = col;
}
