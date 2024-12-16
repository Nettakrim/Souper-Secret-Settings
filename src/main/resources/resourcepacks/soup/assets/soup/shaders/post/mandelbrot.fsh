#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Zoom;
uniform float Iterations;
uniform vec3 Coloring;
uniform vec3 ZR;
uniform vec3 ZI;
uniform vec3 XR;
uniform vec3 XI;
uniform vec3 CR;
uniform vec3 CI;
uniform float Radius;

//https://www.shadertoy.com/view/ml2fDh
vec2 complexPow(vec2 z, vec2 n) {
    float r = length(z);
    float theta = atan(z.y, z.x);

    float realPart = pow(r, n.x) * cos(n.x * theta) * exp(-n.y * theta);
    float imagPart = pow(r, n.x) * sin(n.x * theta) * exp(n.y * theta);

    return vec2(realPart, imagPart);
}

vec3 hueShift(vec3 color, float hue) {
    const vec3 k = vec3(0.57735, 0.57735, 0.57735);
    float cosAngle = cos(hue);
    return vec3(color * cosAngle + cross(k, color) * sin(hue) + k * dot(k, color) * (1.0 - cosAngle));
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
            z = complexPow(z, x) + c;
        }
    }

    vec3 col = texture(InSampler, fract(lastPos + vec2(0.5))).rgb;
    if (duration > Iterations-1) {
        col *= Coloring.b;
    } else {
        if (Coloring.g != 0) {
            col = hueShift(col, duration/Coloring.g);
        }
        col *= 1 - (1 - duration/ceil(Iterations)) * Coloring.r;
    }
    fragColor = vec4(col, 1.0);
}
