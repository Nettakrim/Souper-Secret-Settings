#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform CrystallizeConfig {
    float Scale;
    vec2 Offset;
    vec3 Angle;
    float Seed;
    float UVMix;
    int NthClosest;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

//https://www.shadertoy.com/view/3sGSWV

float hash(vec3 p3){
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

//https://www.ronja-tutorials.com/post/028-voronoi-noise/

vec2 offset(vec2 cell) {
    return cell + vec2(hash(vec3(cell.x, Seed, -cell.y))-0.5, hash(vec3(cell.y, -cell.x, Seed))-0.5)*Offset;
}

vec2 voronoiNoise(vec2 value){
    vec2 center = floor(value);

    vec3 values[9];

    for(int x = -1; x <= 1; x++) {
        int i = (x+1)*3 + 1;
        for (int y = -1; y <= 1; y++) {
            vec2 cellPosition = center + vec2(x,y);
            values[y+i] = vec3(cellPosition, length(offset(cellPosition) - value));
        }
    }

    float maxFloat = 3.402823466e+38;
    float closestDistance = maxFloat;
    int closestIndex = 4;

    for (int i = 0; i < min(NthClosest, 9); i++) {
        for (int j = 0; j < 9; j++) {
            if (values[j].z < closestDistance) {
                closestIndex = j;
                closestDistance = values[j].z;
            }
        }
        values[closestIndex].z = maxFloat;
        closestDistance = maxFloat;
    }

    return values[closestIndex].xy;
}

void main(){
    vec2 scale = InSize/Scale;

    vec2 pos = texCoord*scale;
    vec2 cell = voronoiNoise(pos);
    vec3 color = texture(InSampler, mix(texCoord, cell/scale, UVMix)).rgb;

    vec2 slope = (vec2(hash(vec3(cell.x, cell.y, Seed))-0.5, hash(vec3(Seed, cell.x, cell.y))-0.5)*Angle.xy)*pos;
    float angle = fract(slope.x + slope.y);
    color = mix(color, step(angle, color*color), Angle.z);

    fragColor = vec4(mix(texture(InSampler, texCoord).rgb, color, Alpha), 1);
}