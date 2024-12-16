#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Zoom;
uniform float Iterations;
uniform float Radius;
uniform vec3 ZR;
uniform vec3 ZI;
uniform vec3 XR;
uniform vec3 XI;
uniform vec3 CR;
uniform vec3 CI;

//https://www.shadertoy.com/view/ml2fDh
vec2 complexPow(vec2 z, vec2 n) {
    float r = length(z);
    float theta = atan(z.y, z.x);

    float realPart = pow(r, n.x) * cos(n.x * theta) * exp(-n.y * theta);
    float imagPart = pow(r, n.x) * sin(n.x * theta) * exp(n.y * theta);

    return vec2(realPart, imagPart);
}

vec2 mandelbrot(vec2 z, vec2 x, vec2 c) {
    return complexPow(z, x) + c;
}

vec2 parameter(vec2 c, vec3 r, vec3 i) {
    return vec2(r.r*c.x + r.g*c.y + r.b, i.r*c.x + i.g*c.y + i.b);
}

void main() {
    float duration = 0;
    vec2 lastPos = vec2(0);

    vec2 coord = ((texCoord-vec2(0.5))*vec2(1.0, oneTexel.x/oneTexel.y)/Zoom.r + Zoom.gb);

    vec2 z = parameter(coord, ZR, ZI);
    vec2 x = parameter(coord, XR, XI);
    vec2 c = parameter(coord, CR, CI);

    for (int i = 0; i < Iterations; i++) {
        if (z.x*z.x + z.y*z.y <= Radius) {
            duration += 1;
            lastPos = z;
            z = mandelbrot(z, x, c);
        }
    }

    vec4 col = texture(InSampler, mod(lastPos + vec2(0.5), 1.0))*(duration/ceil(Iterations));
    fragColor = vec4(col.rgb, 1.0);
}
