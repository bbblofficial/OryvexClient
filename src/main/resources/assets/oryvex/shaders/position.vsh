#version 120
// Shared fixed-function-compatible vertex stage for every Oryvex shader.
void main() {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
