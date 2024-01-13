package com.carlospinan.opengles.cubeexample

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.carlospinan.opengles.cubeexample.helpers.compileShader
import com.carlospinan.opengles.cubeexample.helpers.createProgram
import com.carlospinan.opengles.cubeexample.objects.Cube
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    companion object {
        // Position the eye behind the origin.
        private const val EYE_X = 0.0F
        private const val EYE_Y = 0.0F
        private const val EYE_Z = -0.5F

        // We are looking toward the distance
        private const val LOOK_X = 0.0F
        private const val LOOK_Y = 0.0F
        private const val LOOK_Z = -5.0F

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        private const val UP_X = 0.0F
        private const val UP_Y = 1.0F
        private const val UP_Z = 0.0F
    }

    private var cube: Cube? = null

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private val modelMatrix = FloatArray(16)

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private val viewMatrix = FloatArray(16)

    /**
     * Store the model view matrix. This is used to store the results of
     * modelMatrix * viewMatrix
     */
    private val modelViewMatrix = FloatArray(16)

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private val projectionMatrix = FloatArray(16)

    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private val modelViewProjectionMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Called when an new surface has been created
        // Create OpenGL resources here

        // https://stackoverflow.com/questions/5717654/glulookat-explanation
        // Set the camera position (View matrix)
        // This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(
            /* rm = */ viewMatrix,
            /* rmOffset = */ 0,
            /* eyeX = */ EYE_X,
            /* eyeY = */ EYE_Y,
            /* eyeZ = */ EYE_Z,
            /* centerX = */ LOOK_X,
            /* centerY = */ LOOK_Y,
            /* centerZ = */ LOOK_Z,
            /* upX = */ UP_X,
            /* upY = */ UP_Y,
            /* upZ = */ UP_Z,
        )

        val vertexShaderHandle =
            compileShader(
                context = context,
                shaderType = GLES20.GL_VERTEX_SHADER,
                resourceId = R.raw.common_vertex_shader
            )

        val fragmentShaderHandle =
            compileShader(
                context = context,
                shaderType = GLES20.GL_FRAGMENT_SHADER,
                resourceId = R.raw.common_fragment_shader
            )

        val program = createProgram(
            vertexShaderHandle = vertexShaderHandle,
            fragmentShaderHandle = fragmentShaderHandle
        )

        cube = Cube(program = program)

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Called when new GL Surface has been created or changes size

        // Set the OpenglES camera viewport here
        GLES20.glViewport(
            /* x = */ 0,
            /* y = */ 0,
            /* width = */ width,
            /* height = */ height
        )

        val ratio = width.toFloat() / height.toFloat()
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(
            /* m = */ projectionMatrix,
            /* offset = */ 0,
            /* left = */ -ratio,
            /* right = */ ratio,
            /* bottom = */ -1f,
            /* top = */ 1.0F,
            /* near = */ 1.0F,
            /* far = */ 10f
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        // Put code to draw 3d objects to screen here
        // Do a complete rotation every 10 seconds.
        val time = SystemClock.uptimeMillis() % 10000L
        val angleInDegrees = 360.0f / 10000.0f * time.toInt()

        // Set the background frame color
        GLES20.glClearColor(
            /* red = */ 0.0f,
            /* green = */ 0.0f,
            /* blue = */ 0.0f,
            /* alpha = */ 0.0f
        )

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0.0f, -8.0f)
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 1.0f, 0.1f, 0.1f)

        // This multiplies the view matrix by the model matrix, and stores the result in the modelView matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        // This multiplies the model view matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

        cube?.draw(modelViewProjectionMatrix = modelViewProjectionMatrix)
    }
}