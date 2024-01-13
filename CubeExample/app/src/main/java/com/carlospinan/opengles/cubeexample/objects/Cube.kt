package com.carlospinan.opengles.cubeexample.objects

import android.opengl.GLES20
import com.carlospinan.opengles.cubeexample.helpers.ATTRIBUTE_COLOR
import com.carlospinan.opengles.cubeexample.helpers.ATTRIBUTE_POSITION
import com.carlospinan.opengles.cubeexample.helpers.BYTES_PER_FLOAT
import com.carlospinan.opengles.cubeexample.helpers.UNIFORM_MVP_MATRIX
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Cube(private val program: Int) {
    companion object {

        /** Size of the position data in elements. */
        private const val POSITION_DATA_SIZE = 3

        /** Size of the color data in elements. */
        private const val COLOR_DATA_SIZE = 4

        // Define points for a cube.
        // X, Y, Z
        private val cubePositionData =
            floatArrayOf(
                // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
                // if the points are counter-clockwise we are looking at the "front". If not we are looking at
                // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
                // usually represent the backside of an object and aren't visible anyways.
                // Front face
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                // Right face
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                // Back face
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                // Left face
                -1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                // Top face
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                // Bottom face
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f
            )

        // R, G, B, A
        private val cubeColorData = floatArrayOf(
            // Front face (red)
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            // Right face (green)
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            // Back face (blue)
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            // Left face (yellow)
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            // Top face (cyan)
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            // Bottom face (magenta)
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f
        )

    }

    /** Store our model data in a float buffer. */
    private var cubePositions: FloatBuffer =
        ByteBuffer.allocateDirect(cubePositionData.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(cubePositionData)
                position(0)
            }
    private var cubeColors: FloatBuffer =
        ByteBuffer.allocateDirect(cubeColorData.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(cubeColorData)
                position(0)
            }

    private val mvpMatrixHandle = GLES20.glGetUniformLocation(program, UNIFORM_MVP_MATRIX)
    private val positionHandle = GLES20.glGetAttribLocation(program, ATTRIBUTE_POSITION)
    private val colorHandle = GLES20.glGetAttribLocation(program, ATTRIBUTE_COLOR)

    fun draw(modelViewProjectionMatrix: FloatArray) {
        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(program)

        // Pass in the position information
        cubePositions.position(0)
        GLES20.glVertexAttribPointer(
            /* indx = */ positionHandle,
            /* size = */ POSITION_DATA_SIZE,
            /* type = */ GLES20.GL_FLOAT,
            /* normalized = */ false,
            /* stride = */ 0,
            /* ptr = */ cubePositions
        )
        GLES20.glEnableVertexAttribArray(positionHandle)

        // Pass in the color information
        cubeColors.position(0)
        GLES20.glVertexAttribPointer(
            /* indx = */ colorHandle,
            /* size = */ COLOR_DATA_SIZE,
            /* type = */ GLES20.GL_FLOAT,
            /* normalized = */ false,
            /* stride = */ 0,
            /* ptr = */ cubeColors
        )
        GLES20.glEnableVertexAttribArray(colorHandle)

        GLES20.glUniformMatrix4fv(
            /* location = */ mvpMatrixHandle,
            /* count = */ 1,
            /* transpose = */ false,
            /* value = */ modelViewProjectionMatrix,
            /* offset = */ 0
        )
        GLES20.glDrawArrays(
            /* mode = */ GLES20.GL_TRIANGLES,
            /* first = */ 0,
            /* count = */ 36
        )

        GLES20.glUseProgram(0)
    }

}