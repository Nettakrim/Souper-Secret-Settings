#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform vec2 Corner00;
uniform vec2 Corner01;
uniform vec2 Corner10;
uniform vec2 Corner11;

mat3 getMatrix(vec2 a, vec2 b, vec2 c, vec2 d) {
    vec3 A = vec3(a, 1);
    vec3 B = vec3(b, 1);
    vec3 D = vec3(d, 1);

    vec3 C = inverse(mat3(A,D,B))*vec3(c, 1);

    return mat3(A*C.x,D*C.y,B*C.z);
}

mat3 getMapping(mat3 M_input, mat3 M_output) {
    return M_output * inverse(M_input);
}

vec2 applyTransform(mat3 mapping, vec2 point) {
    vec3 prime = mapping * vec3(point.x, point.y, 1);
    return vec2(prime.x/prime.z, prime.y/prime.z);
}

void main(){
    mat3 M_input = getMatrix(vec2(0,0), vec2(0,1), vec2(1,1), vec2(1,0));
    mat3 M_output = getMatrix(Corner00, Corner01, Corner11, Corner10);

    mat3 M_final = getMapping(M_output, M_input);

    vec2 pos = applyTransform(M_final, texCoord);

    vec3 col;

    if (pos.x < 0 || pos.x > 1 || pos.y < 0 || pos.y > 1) {
        col = texture(BaseSampler, texCoord).rgb;
    } else {
        col = texture(DiffuseSampler, pos).rgb;
    }

    fragColor = vec4(col, 1.0);
}