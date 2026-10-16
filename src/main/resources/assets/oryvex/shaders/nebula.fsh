#version 120
// Procedural deep-black / electric-blue nebula with a scrolling perspective grid.
uniform float u_time;
uniform vec2 u_resolution;
uniform vec2 u_mouse;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 m = mat2(1.6, 1.2, -1.2, 1.6);
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = m * p;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution;
    vec2 p = (gl_FragCoord.xy - 0.5 * u_resolution) / u_resolution.y;
    float t = u_time * 0.035;
    vec2 parallax = (u_mouse - 0.5) * 0.12;

    vec2 q = vec2(fbm(p * 1.8 + vec2(0.0, t) + parallax), fbm(p * 1.8 + vec2(5.2, -t) - parallax));
    float n = fbm(p * 2.1 + q * 1.9 + vec2(t * 0.6, 0.0));

    vec3 obsidian = vec3(0.027, 0.031, 0.039);   // #07080A
    vec3 slate    = vec3(0.055, 0.067, 0.090);   // #0E1117
    vec3 blue     = vec3(0.000, 0.482, 1.000);   // #007BFF
    vec3 cyan     = vec3(0.000, 0.706, 1.000);   // #00B4FF
    vec3 glow     = vec3(0.000, 0.898, 1.000);   // #00E5FF

    vec3 col = mix(obsidian, slate, uv.y * 0.6);
    col += blue * pow(n, 3.2) * 1.05;
    col += cyan * pow(clamp(q.x * q.y, 0.0, 1.0), 2.2) * 0.45;

    float horizon = 0.36;
    float depth = horizon - uv.y;
    if (depth > 0.0) {
        float z = 0.09 / depth;
        vec2 g = vec2((uv.x - 0.5) * z * 5.0, z + u_time * 0.30);
        vec2 grid = abs(fract(g) - 0.5) / max(fwidth(g), vec2(0.0001));
        float line = 1.0 - min(min(grid.x, grid.y), 1.0);
        float fade = smoothstep(0.0, 0.12, depth) * (1.0 - smoothstep(0.25, 0.42, depth) * 0.6);
        col += mix(blue, cyan, uv.x) * line * fade * 0.55;
    }
    col += glow * exp(-abs(uv.y - horizon) * 38.0) * 0.22;

    vec2 sp = floor(gl_FragCoord.xy / 2.0);
    float s = hash(sp);
    float twinkle = 0.5 + 0.5 * sin(u_time * 2.0 + s * 120.0);
    col += vec3(0.55, 0.80, 1.0) * step(0.9972, s) * twinkle * step(horizon, uv.y) * 0.9;

    float vignette = smoothstep(1.25, 0.25, length(uv - 0.5) * 1.35);
    col *= mix(0.55, 1.0, vignette);
    gl_FragColor = vec4(col, 1.0);
}
