// A constant representing the combined model/view/projection matrix.
uniform mat4 u_MVPMatrix;

// Per-vertex position information we will pass in.
attribute vec4 a_Position;

// Per-vertex color information we will pass in.
attribute vec4 a_Color;

// This will be passed into the fragment shader.
varying vec4 v_Color;

// The entry point for our vertex shader.
void main() {
    // Pass the color through to the fragment shader.
    // It will be interpolated across the triangle.
    v_Color = a_Color;

    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in
    // normalized screen coordinates.
    gl_Position = u_MVPMatrix * a_Position;
}