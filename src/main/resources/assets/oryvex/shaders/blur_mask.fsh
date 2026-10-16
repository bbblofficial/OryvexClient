#version 120
// Composites the blurred framebuffer inside a rounded rectangle and tints it (glass).
uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform vec2 u_size;
uniform float u_radius;
uniform float u_softness;
uniform vec4 u_tint;
uniform float u_alpha;

float roundedBox(vec2 p, vec2 halfSize, float r) {
    vec2 q = abs(p) - halfSize + vec2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 local = gl_TexCoord[0].st * u_size;
    vec2 halfSize = u_size * 0.5;
    float d = roundedBox(local - halfSize, halfSize, u_radius);
    float mask = 1.0 - smoothstep(-u_softness * 0.5, u_softness * 0.5, d);
    vec3 blurred = texture2D(u_texture, gl_FragCoord.xy / u_resolution).rgb;
    vec3 color = mix(blurred, u_tint.rgb, u_tint.a);
    gl_FragColor = vec4(color, mask * u_alpha);
}
