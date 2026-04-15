#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ShadowsConfig {
    float Fov;
    float Pitch;
    float Yaw;
    vec3 Offset;
    vec3 UpVector;
    vec3 Steps;
    float Padding;
    float DepthScale;
    vec3 ShadowColor;
    float ViewDistance;
    vec2 ShadowFade;
    float SunAngle;
    float SunFade;
    vec2 Clipping;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

float LinearizeDepth(float depth) {
    return (Clipping.x*Clipping.y) / (depth * (Clipping.x - Clipping.y) + Clipping.y);
}

float aspect = InSize.x/InSize.y;
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

vec3 GetWorldOffset(vec2 coord) {
    float xSlope = yTan * (coord.x*2.0 - 1.0) * aspect;
    float ySlope = yTan * (coord.y*2.0 - 1.0);

    float d = LinearizeDepth(texture(InDepthSampler, coord).r);
    vec3 pos = vec3(xSlope * d, (ySlope * d), -d) + Offset;
    return pos * rotation;
}


vec3 OffsetRaycast(mat4 projection, vec3 direction, vec3 startPos, float steps, float zStep, float zGrowth) {
    vec3 pos = startPos;
    for (float i = 1; i < steps; i++) {
        //d = zStep * (zGrowth^i) * i
        float d = i*zStep;

        pos = startPos + (direction*d);

        vec4 projected = projection*vec4(rotation*pos, 0.0);
        vec2 screen = ((projected.xy/projected.z) + vec2(1.0))/2;
        float depth = length(GetWorldOffset(screen));

        if (depth+Padding < length(pos)) {
            return pos-startPos;
        }
        zStep *= zGrowth;
    }
    return pos-startPos;
}

void main(){
    //https://registry.khronos.org/OpenGL-Refpages/gl2.1/xhtml/gluPerspective.xml
    float yCotan = 1.0/yTan;
    mat4 projection = mat4(yCotan/aspect, 0, 0, 0, 0, yCotan, 0, 0, 0, 0, (Clipping.y+Clipping.x)/(Clipping.x-Clipping.y), (2*Clipping.y*Clipping.x)/(Clipping.x-Clipping.y), 0, 0, -1, 0);
    vec3 pos = GetWorldOffset(texCoord);

    vec3 upVector = UpVector;
    float angle = fract(SunAngle + 0.25);
    if (angle > 0.5) {
        angle -= 0.5;
    }
    float sinAngle = sin(angle*6.28318530718);
    float cosAngle = cos(angle*6.28318530718);
    upVector.xy = vec2(-(upVector.y*cosAngle+upVector.x*sinAngle), upVector.y*sinAngle-upVector.x*cosAngle);

    vec3 hitOffset = OffsetRaycast(projection, upVector, pos, Steps.x, Steps.y, Steps.z);

    float shadowScale = 1-clamp(((length(hitOffset+pos)-ViewDistance*16)+ShadowFade.x+ShadowFade.y) / ShadowFade.y, 0, 1);
    shadowScale *= min(SunFade/4 - abs(SunFade*(angle-0.25)), 1);

    float offset = length(hitOffset*upVector);
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 color = mix(ShadowColor, base, mix(1, (offset / (offset + DepthScale)), shadowScale));

    fragColor = vec4(mix(base, color, Alpha), 1.0);
}
