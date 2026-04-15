#version 330

uniform sampler2D InSampler;

layout(std140) uniform PendulumConfig {
    float Iterations;
    float TimeStep;

    vec4 InputX;
    vec4 InputY;
    vec4 InputOffset;

    vec4 OutputX;
    vec4 OutputY;
    vec2 OutputOffset;
};

layout(std140) uniform MergeConfig {
    float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

// inspiration + explanation:
// https://www.youtube.com/watch?v=n7JK4Ht8k8M
// as far as i am aware, it is implemented correctly

// Shader Code for simulating a double pendulum from:
// https://observablehq.com/@rreusser/the-double-pendulum-fractal?collection=@rreusser/writeups
//
// Copyright 2022 Ricky Reusser
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

vec4 derivative(vec4 state) {
    vec2 theta = state.xy;
    vec2 pTheta = state.zw;
    float threeCosTheta12 = 3.0 * cos(theta.x - theta.y);
    vec2 thetaDot = 6.0 * (
    vec2(
        2.0 * pTheta.x - threeCosTheta12 * pTheta.y,
        8.0 * pTheta.y - threeCosTheta12 * pTheta.x
    ) / (16.0 - threeCosTheta12 * threeCosTheta12));
    float thetaDot12sinTheta12 = thetaDot.x * thetaDot.y * sin(theta.x - theta.y);
    vec2 pThetaDot = -0.5 * vec2(
        thetaDot12sinTheta12 + 3.0 * sin(theta.x),
        -thetaDot12sinTheta12 + sin(theta.y)
    );
    return vec4(thetaDot, pThetaDot);
}

vec4 updatePendulum(vec4 yn, float dt) {
    float PI = 3.14159265359;

    // Convert from [0, 1] to [-PI, PI]:
    yn = yn * (2.0 * PI) - PI;

    // RK4 integration
    vec4 k1 = dt * derivative(yn);
    vec4 k2 = dt * derivative(yn + 0.5 * k1);
    vec4 k3 = dt * derivative(yn + 0.5 * k2);
    vec4 k4 = dt * derivative(yn + k3);
    yn += (k1 + k4 + 2.0 * (k2 + k3)) / 6.0;

    // Convert back from [-PI, PI] to [0, 1]:
    yn = (yn + PI) / (2.0 * PI);

    // Loop angles if they exceed the range
    yn.xy = fract(yn.xy);

    return yn;
}

void main(){
    vec4 pendulum = InputX*texCoord.x + InputY*texCoord.y + InputOffset;

    float a = abs(Iterations);
    float s = sign(Iterations);
    for (int i = 0; i < a; i++) {
        vec4 target = updatePendulum(pendulum, TimeStep);
        pendulum = mix(pendulum, target, s*min(a-i,1));
    }

    vec4 col = texture(InSampler, fract(vec2(dot(OutputX, pendulum), dot(OutputY, pendulum)) + OutputOffset));

    fragColor = vec4(mix(texture(InSampler, texCoord), col, Alpha).rgb, 1.0);
}
