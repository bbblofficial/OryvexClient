#version 120
// Dual-Kawase blur, pass 1 (downsample).
uniform sampler2D u_texture;
uniform vec2 u_halfpixel;
uniform float u_offset;

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec2 o = u_halfpixel * u_offset;
    vec4 sum = texture2D(u_texture, uv) * 4.0;
    sum += texture2D(u_texture, uv - o);
    sum += texture2D(u_texture, uv + o);
    sum += texture2D(u_texture, uv + vec2(o.x, -o.y));
    sum += texture2D(u_texture, uv - vec2(o.x, -o.y));
    gl_FragColor = vec4(sum.rgb / 8.0, 1.0);
}
