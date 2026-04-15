#version 330

uniform sampler2D InSampler;

layout(std140) uniform PaletteDitherConfig {
    int Amount;
    // this could be received as an array, but we filter then through a Pow() that shouldnt be repeated
    vec3 Color1;
    vec3 Color2;
    vec3 Color3;
    vec3 Color4;
    vec3 Color5;
    vec3 Color6;
    vec3 Color7;
    vec3 Color8;
    float Fallback;
    float Power;
    float Seed;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

float scalarTripleProduct(vec3 a, vec3 b, vec3 c) {
    return dot(a,cross(b,c));
}

float hash(vec3 p3){
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

float squareDist(vec3 a, vec3 b) {
    vec3 v = a - b;
    return dot(v, v);
}

vec4 getBarycentric(vec3 a, vec3 b, vec3 c, vec3 d, vec3 p) {
    vec3 vap = p - a;
    vec3 vbp = p - b;
    vec3 vab = b - a;
    vec3 vac = c - a;
    vec3 vad = d - a;
    vec3 vbc = c - b;
    vec3 vbd = d - b;

    return vec4(
        scalarTripleProduct(vbp, vbd, vbc),
        scalarTripleProduct(vap, vac, vad),
        scalarTripleProduct(vap, vad, vab),
        scalarTripleProduct(vap, vab, vac)
    );
}

int orders[70] = int[](
    1672, 2256, 2257, 2248, 2184, 2840, 2768, 2833, 2824, 2832,
    2760, 2842, 2696, 2841, 2769, 3425, 3418, 3281, 3344, 3280,
    3352, 3354, 3353, 3427, 3416, 3408, 3424, 3426, 3345, 3417,
    3400, 3272, 3409, 3208, 3336, 3984, 3936, 3848, 3857, 3993,
    3793, 3985, 3865, 3864, 4009, 3929, 3921, 3937, 3976, 4002,
    3784, 3856, 4008, 3928, 4012, 3938, 4011, 3912, 3992, 3930,
    4003, 4010, 3866, 3939, 3920, 3720, 3994, 4001, 4000, 3792
);

vec3 p = vec3(Power);

vec3 colors[8] = vec3[](
    pow(Color1, p), pow(Color2, p), pow(Color3, p), pow(Color4, p),
    pow(Color5, p), pow(Color6, p), pow(Color7, p), pow(Color8, p)
);

// interpretation of a palette dither method described by https://bsky.app/profile/krisp.bsky.social
// its less cool though as the medium of soup doesnt really allow for precomputing lookup tables
// and tetrahedralisation is hard on the gpu, so instead i just brute force all of them, which is about 2x as bad as soup:mouse
void main() {
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 col = pow(base, p);

    float fallbackDistance = 1000;
    int fallbackOrder = 0;

    float minDistance = 1000;
    vec4 weights = vec4(-1);

    vec3 A;
    vec3 B;
    vec3 C;
    vec3 D;

    for (int i = 0; i < min(Amount, 70); i++) {
        int order = orders[i];
        vec3 a = vec3(colors[order & 7]);
        vec3 b = vec3(colors[(order >> 3) & 7]);
        vec3 c = vec3(colors[(order >> 6) & 7]);
        vec3 d = vec3(colors[order >> 9]);

        float dist = squareDist(col, a) + squareDist(col, b) + squareDist(col, c) + squareDist(col, d);

        if (dist > minDistance) {
            continue;
        }

        if (dist < fallbackDistance) {
            fallbackDistance = dist;
            fallbackOrder = i;
        }

        vec4 barycentric = getBarycentric(a, b, c, d, col);

        if ((barycentric.x < 0 || barycentric.y < 0 || barycentric.z < 0 || barycentric.w < 0) && (barycentric.x > 0 || barycentric.y > 0 || barycentric.z > 0 || barycentric.w > 0)) {
            // i think if the handedness is incorrect and the point is on the tetrahedron, then all the values are negative
            // if this is let through though, itll just fix itself, since itll be divided by its sum
            continue;
        }

        float sum = barycentric.x + barycentric.y + barycentric.z + barycentric.w;
        if (sum == 0) {
            continue;
        }

        minDistance = dist;
        weights = barycentric / sum;
        A = a;
        B = b;
        C = c;
        D = d;
    }

    // fallback to unit cube
    // https://cs.stackexchange.com/questions/89910/how-to-decompose-a-unit-cube-into-tetrahedra
    if (weights.x < 0) {
        if (Fallback > 0.5) {
            A = vec3(0);
            D = vec3(1);

            if (col.x <= col.z && col.z <= col.y) {
                B = vec3(0, 1, 0);
                C = vec3(0, 1, 1);
            }
            else if (col.x <= col.y && col.y <= col.z) {
                B = vec3(0, 1, 1);
                C = vec3(0, 0, 1);
            }
            else if (col.y <= col.x && col.x <= col.z) {
                B = vec3(0, 0, 1);
                C = vec3(1, 0, 1);
            }
            else if (col.y <= col.z && col.z <= col.x) {
                B = vec3(1, 0, 1);
                C = vec3(1, 0, 0);
            }
            else if (col.z <= col.y && col.y <= col.x) {
                B = vec3(1, 0, 0);
                C = vec3(1, 1, 0);
            }
            else {
                B = vec3(1, 1, 0);
                C = vec3(0, 1, 0);
            }
        } else {
            // or nearest pallete value
            int order = orders[fallbackOrder];
            A = vec3(colors[order & 7]);
            B = vec3(colors[(order >> 3) & 7]);
            C = vec3(colors[(order >> 6) & 7]);
            D = vec3(colors[order >> 9]);
        }

        vec4 barycentric = getBarycentric(A, B, C, D, col);
        if (barycentric.x >= 0 || barycentric.y >= 0 || barycentric.z >= 0 || barycentric.w >= 0) {
            barycentric = max(barycentric, 0);
        }
        weights = barycentric / (barycentric.x + barycentric.y + barycentric.z + barycentric.w);
    }

    vec2 t = texCoord + vec2(1.234 * Seed, 5.678);
    float r = hash(vec3(t * t, Seed * t.y));
    float random = hash(vec3(texCoord + vec2(r), fract(t.x - r)));

    vec3 result;
    if (random < weights.x) {
        result = A;
    }
    else if (random < weights.x + weights.y) {
        result = B;
    }
    else if (random < 1 - weights.w) {
        result = C;
    }
    else {
        result = D;
    }

    fragColor = vec4(mix(base, pow(result, 1.0/p), Alpha), 1.0);
}
