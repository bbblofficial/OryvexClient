#version 120
// Anti-aliased rounded rectangle with gradient fill, border and soft glow.
// All sizes are in GUI units; u_softness is one physical pixel in GUI units.
uniform vec2 u_size;
uniform float u_pad;
uniform float u_radius;
uniform float u_softness;
uniform vec4 u_color1;
uniform vec4 u_color2;
uniform float u_vertical;
uniform float u_border;
uniform vec4 u_borderColor;
uniform float u_glow;

float roundedBox(vec2 p, vec2 halfSize, float r) {
    vec2 q = abs(p) - halfSize + vec2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 full = u_size + vec2(u_pad * 2.0);
    vec2 local = gl_TexCoord[0].st * full - vec2(u_pad);
    vec2 halfSize = u_size * 0.5;
    float d = roundedBox(local - halfSize, halfSize, u_radius);

    if (u_glow > 0.0) {
        float g = 1.0 - clamp(d / u_glow, 0.0, 1.0);
        g = g * g * (3.0 - 2.0 * g);
        gl_FragColor = vec4(u_color1.rgb, u_color1.a * g * 0.85);
        return;
    }

    float edge = 1.0 - smoothstep(-u_softness * 0.5, u_softness * 0.5, d);
    float t = u_vertical > 0.5 ? clamp(local.y / u_size.y, 0.0, 1.0) : clamp(local.x / u_size.x, 0.0, 1.0);
    vec4 fill = mix(u_color1, u_color2, t);
    if (u_border > 0.0) {
        float inner = 1.0 - smoothstep(-u_softness * 0.5, u_softness * 0.5, d + u_border);
        fill = mix(u_borderColor, fill, inner);
    }
    gl_FragColor = vec4(fill.rgb, fill.a * edge);
}
