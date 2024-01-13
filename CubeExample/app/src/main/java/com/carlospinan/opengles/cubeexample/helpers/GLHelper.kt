package com.carlospinan.opengles.cubeexample.helpers

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

const val ATTRIBUTE_POSITION = "a_Position"
const val ATTRIBUTE_COLOR = "a_Color"
const val UNIFORM_MVP_MATRIX = "u_MVPMatrix"

/** How many bytes per float. */
const val BYTES_PER_FLOAT = 4

/**
 * @author Carlos Piñan
 */

private const val TAG = "GLHelper"

fun checkGLError(glOperation: String) {
    var error: Int
    if (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
        Log.e(TAG, "$glOperation\nglError $error")
    }
}

fun compileShader(context: Context, shaderType: Int, resourceId: Int): Int {
    return compileShader(shaderType, context.readTextFileFromRawResource(resourceId))
}

/**
 * Helper function to compile a shader.
 *
 * @param shaderType The shader type.
 * @param shaderCode The shader source code.
 * @return An OpenGL handle to the shader.
 */
fun compileShader(shaderType: Int, shaderCode: String?): Int {
    val shader = GLES20.glCreateShader(shaderType)
    GLES20.glShaderSource(shader, shaderCode)
    GLES20.glCompileShader(shader)
    checkGLError("Error loading shader: shaderType = $shaderType - shaderCode\n$shaderCode")

    // Get the compilation status.
    val compileStatus = IntArray(1)
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

    // If the compilation failed, delete the shader.
    if (compileStatus[0] == 0) {
        Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader))
        GLES20.glDeleteShader(shader)
    }
    if (shader == 0) {
        throw RuntimeException("Error creating shader.")
    }
    return shader
}


/**
 * Helper function to compile and link a program.
 *
 * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
 * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
 * @param attributes Attributes that need to be bound to the program.
 * @return An OpenGL handle to the program.
 */
fun createProgram(
    vertexShaderHandle: Int,
    fragmentShaderHandle: Int,
    attributes: Array<String?>? = null
): Int {
    var programHandle = GLES20.glCreateProgram()
    if (programHandle != 0) {
        // Bind the vertex shader to the program.
        GLES20.glAttachShader(programHandle, vertexShaderHandle)

        // Bind the fragment shader to the program.
        GLES20.glAttachShader(programHandle, fragmentShaderHandle)

        // Bind attributes
        if (attributes != null) {
            val size = attributes.size
            for (i in 0 until size) {
                GLES20.glBindAttribLocation(programHandle, i, attributes[i])
            }
        }

        // Link the two shaders together into a program.
        GLES20.glLinkProgram(programHandle)

        // Get the link status.
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

        // If the link failed, delete the program.
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle))
            GLES20.glDeleteProgram(programHandle)
            programHandle = 0
        }
    }
    if (programHandle == 0) {
        throw RuntimeException("Error creating program.")
    }
    return programHandle
}

fun Context.readTextFileFromRawResource(resourceId: Int): String? {
    val inputStream = resources.openRawResource(resourceId)
    val inputStreamReader = InputStreamReader(inputStream)
    val bufferedReader = BufferedReader(inputStreamReader)
    val body = StringBuilder()

    try {
        var nextLine: String?
        while (bufferedReader.readLine().also { nextLine = it } != null) {
            body.append(nextLine)
            body.append('\n')
        }
    } catch (e: IOException) {
        return null
    }
    return body.toString()
}