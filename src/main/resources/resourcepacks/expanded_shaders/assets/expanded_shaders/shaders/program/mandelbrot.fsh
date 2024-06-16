#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Iterations;

vec2 mandelbrot(vec2 p, vec2 c) {
    return vec2(p.x*p.x - p.y*p.y + c.x, 2*p.x*p.y + c.y);
}

void main() {
    vec2 offsetCoord = (((texCoord - vec2(0.5))*3)-vec2(0.5, 0))*vec2(1.0, oneTexel.x/oneTexel.y);

    float duration = 0;
    vec2 displayPos = vec2(0);
    vec2 pos = vec2(0);
    for (int i = 0; i < Iterations; i++) {
        pos = mandelbrot(pos, offsetCoord);
        if (pos.x*pos.x + pos.y*pos.y <= 4) {
            duration += 1;
            displayPos = pos;
        }
    }
    displayPos += vec2(0.5);

    vec4 col = texture(DiffuseSampler, mod(displayPos, 1.0))*(duration/Iterations);

    fragColor = vec4(col.rgb, 1.0);
}
