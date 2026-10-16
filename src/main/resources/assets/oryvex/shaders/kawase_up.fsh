#version 120
// Dual-Kawase blur, pass 2 (upsample).
uniform sampler2D u_texture;
uniform vec2 u_halfpixel;
uniform float u_offset;

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec2 o = u_halfpixel * u_offset;
    vec4 sum = texture2D(u_texture, uv + vec2(-o.x * 2.0, 0.0));
    sum += texture2D(u_texture, uv + vec2(-o.x, o.y)) * 2.0;
    sum += texture2D(u_texture, uv + vec2(0.0, o.y * 2.0));
    sum += texture2D(u_texture, uv + vec2(o.x, o.y)) * 2.0;
    sum += texture2D(u_texture, uv + vec2(o.x * 2.0, 0.0));
    sum += texture2D(u_texture, uv + vec2(o.x, -o.y)) * 2.0;
    sum += texture2D(u_texture, uv + vec2(0.0, -o.y * 2.0));
    sum += texture2D(u_texture, uv + vec2(-o.x, -o.y)) * 2.0;
    gl_FragColor = vec4(sum.rgb / 12.0, 1.0);
}
