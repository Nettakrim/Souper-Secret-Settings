#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 InDepthSize;
};

layout(std140) uniform Noise3DConfig {
    vec4 Mode;
    float Colored;
    vec2 NoiseCurve;
    float Loop;
    vec3 Scale;
    vec3 Offset;
    vec3 Scroll;
    float Fov;
    float Pitch;
    float Yaw;
    vec3 CamFract;
    vec3 Cam;
    vec2 Clipping;
    vec3 ScreenScale;
    vec3 RenderLocation;
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

float GetDepth(vec2 coord) {
    return LinearizeDepth(texture(InDepthSampler, coord).r);
}

vec3 GetWorldOffset(vec2 coord, float d) {
    float xSlope = yTan * (coord.x*2.0 - 1.0) * aspect;
    float ySlope = yTan * (coord.y*2.0 - 1.0);

    vec3 pos = vec3(xSlope * d, (ySlope * d), -d);
    return (pos * rotation);
}

vec3 GetWorldOffset(vec2 coord) {
    return GetWorldOffset(coord, GetDepth(coord));
}

vec3 edge = Loop*Scale;

//https://www.shadertoy.com/view/3sGSWV

float hash(vec3 p3){
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 rand(vec3 p3){
    vec3 p = p3 - (step(edge, p3) * edge + floor(Scroll));
    vec3 n = vec3(hash(p));
    if (Colored == 0) {
        return n;
    }
    vec3 c = n;
    c.y = hash(n+p);
    c.z = hash(n+c);
    c.x = hash(c);
    return mix(n, c, Colored);
}

// From iq: https://www.shadertoy.com/view/4sfGzS
vec3 noise(vec3 x){
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = mix(f, f*f*(3.0-2.0*f), NoiseCurve.x);
    f = mix(f, clamp(f, 0, 1),  NoiseCurve.y+1);
    return mix(mix(mix(rand(i),
                       rand(i+vec3(1, 0, 0)),f.x),
                   mix(rand(i+vec3(0, 1, 0)),
                       rand(i+vec3(1, 1, 0)),f.x),f.y),
               mix(mix(rand(i+vec3(0, 0, 1)),
                       rand(i+vec3(1, 0, 1)),f.x),
                   mix(rand(i+vec3(0, 1, 1)),
                       rand(i+vec3(1, 1, 1)),f.x),f.y),f.z);
}

void main(){
    float dist = GetDepth(texCoord);
    vec3 pos = GetWorldOffset(texCoord, dist) + Offset - fract(Scroll)/Scale;

    vec3 offset = CamFract/Loop + fract(floor(Cam)/Loop);
    offset.y = -offset.y;
    pos = (fract(pos/Loop - offset)*Loop)*Scale;

    vec3 color = texture(InSampler, texCoord).rgb;
    vec3 base = color;

    vec3 screen = vec3(texCoord.xy*ScreenScale.xy, ScreenScale.z)-fract(Scroll);
    screen.x *= aspect;
    // RenderLocation.y = isOverUi()
    vec3 n = noise(mix(pos, screen, RenderLocation.y));

    color = mix(color, step(n, pow(color, vec3(Mode.y))), Mode.x);
    color = mix(color, color*mix(n, 1-n, Mode.w), Mode.z);

    fragColor = vec4(mix(base, color, Alpha), 1.0);
}
