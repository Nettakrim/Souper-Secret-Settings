#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 InDepthSize;
};

layout(std140) uniform ReflectConfig {
    float Fov;
    float Pitch;
    float Yaw;
    vec3 Offset;
    vec3 Steps;
    float Padding;
    int Iterations;
    vec4 UVDistances;
    float MaxDistance;
    vec2 OutOfBounds;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

vec2 oneTexel = 1.0 / InSize;
float aspect = oneTexel.y/oneTexel.x;
float yTan = tan(Fov/114.591559);

mat3 GetRotationMatrix(vec2 rotation) {
    rotation /= 57.2957795131;
    float sx = sin(rotation.x);
    float cx = cos(rotation.x);
    float sy = sin(rotation.y);
    float cy = cos(rotation.y);
    return transpose(mat3(cy, 0, sy, 0, 1, 0, -sy, 0, cy) * mat3(1, 0, 0, 0, cx, -sx, 0, sx, cx));
}

mat3 rotation = GetRotationMatrix(vec2(Pitch, Yaw));

vec2 GetSlope(vec2 coord) {
    return vec2(yTan * (coord.x*2.0 - 1.0) * aspect, yTan * (coord.y*2.0 - 1.0));
}

vec3 GetWorldOffset(vec2 coord, vec2 slope) {
    float d = LinearizeDepth(texture(InDepthSampler, coord).r);
    vec3 pos = vec3(slope.x * d, (slope.y * d), -d) + Offset;
    return pos * rotation;
}

vec3 GetWorldOffset(vec2 coord) {
    return GetWorldOffset(coord, GetSlope(coord));
}

vec2 GetScreenPos(mat4 projection, vec3 pos) {
    vec4 projected = projection*vec4(rotation*pos, 0.0);
    return ((projected.xy/projected.z) + vec2(1.0))/2;
}

vec3 Raycast(mat4 projection, vec3 direction, vec3 startPos, float steps, float zStep, float zGrowth) {
    vec3 pos = startPos;
    for (float i = 1; i < steps; i++) {
        //d = zStep * (zGrowth^i) * i
        float d = i*zStep;

        pos = startPos + (direction*d);
        vec2 screen = GetScreenPos(projection, pos);
        float depth = length(GetWorldOffset(screen));

        if (depth+Padding < length(pos)) {
            return pos;
        }
        zStep *= zGrowth;
    }
    return pos;
}

void main(){
    //https://registry.khronos.org/OpenGL-Refpages/gl2.1/xhtml/gluPerspective.xml
    float yCotan = 1.0/yTan;
    mat4 projection = mat4(yCotan/aspect, 0, 0, 0, 0, yCotan, 0, 0, 0, 0, (far+near)/(near-far), (2*far*near)/(near-far), 0, 0, -1, 0);

    vec2 slope = GetSlope(texCoord);
    vec3 direction = inverse(rotation)*normalize(vec3(slope.x, slope.y, -1));

    vec3 pos;
    vec2 coord = texCoord;

    for (int i = 0; i < Iterations; i++) {
        pos = GetWorldOffset(coord);
        if (length(pos) > MaxDistance) {
            break;
        }
        vec3 offsetLeft =  pos - GetWorldOffset(coord + vec2(-oneTexel.x * UVDistances.z, 0));
        vec3 offsetRight = pos - GetWorldOffset(coord + vec2( oneTexel.x * UVDistances.x, 0));
        vec3 offsetUp =    pos - GetWorldOffset(coord + vec2(0,  oneTexel.y * UVDistances.y));
        vec3 offsetDown =  pos - GetWorldOffset(coord + vec2(0, -oneTexel.y * UVDistances.w));

        vec3 totalOffset = abs(offsetLeft) + abs(offsetRight) + abs(offsetUp) + abs(offsetDown);
        vec3 normal;
        if (totalOffset.x < totalOffset.y && totalOffset.x < totalOffset.z) {
            normal = vec3(1,0,0);
        } else if (totalOffset.y < totalOffset.x && totalOffset.y < totalOffset.z) {
            normal = vec3(0,1,0);
        } else {
            normal = vec3(0,0,1);
        }

        direction -= 2.0*dot(direction, normal)*normal;

        pos = Raycast(projection, direction, pos, Steps.x, Steps.y, Steps.z);
        coord = GetScreenPos(projection, pos);
    }

    vec4 color = texture(InSampler, coord);
    if (coord.x < 0 || coord.y < 0 || coord.x > 1 || coord.y > 1) {
        color *= OutOfBounds.x;
    }
    if ((rotation*pos).z > 0) {
        color *= OutOfBounds.y;
    }
    fragColor = vec4(mix(texture(InSampler, texCoord), color, Alpha).rgb, 1.0);
}
