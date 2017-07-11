package com.schneppd.myopenglapp.OpenGL2

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log

/**
 * Created by schneppd on 7/9/17.
 */
class CustomGLRenderer : GLSurfaceView.Renderer{
	companion object Static {
		val TAG = "CustomGLRenderer"

		/**
		 * Utility method for compiling a OpenGL shader.
		 *
		 * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
		 * method to debug shader coding errors.</p>
		 *
		 * @param type - Vertex or fragment shader type.
		 * @param shaderCode - String containing the shader code.
		 * @return - Returns an id for the shader.
		 */
		fun loadShader(type:Int, shaderCode:String) : Int{
			// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
			// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
			val shader = GLES20.glCreateShader(type)
			// add the source code to the shader and compile it
			GLES20.glShaderSource(shader, shaderCode)
			GLES20.glCompileShader(shader)
			return shader
		}
		/**
		 * Utility method for debugging OpenGL calls. Provide the name of the call
		 * just after making it:
		 *
		 * <pre>
		 * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
		 * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
		 *
		 * If the operation is not successful, the check throws an error.
		 *
		 * @param glOperation - Name of the OpenGL call to check.
		 */
		fun checkGlError(glOperation:String) {
			var error = GLES20.glGetError()
			while (error != GLES20.GL_NO_ERROR) {
				Log.e(TAG, glOperation + ": glError " + error)
				//throw RuntimeException(glOperation + ": glError " + error)
				error = GLES20.glGetError()
			}
		}
	}

	var _angle = 0f
	var angle:Float
		get() = _angle
		set(value) {
			_angle = value
		}

	private val mTriangle: Triangle by lazy {
        Triangle()
	}
	private val mSquare: Square by lazy {
        Square()
	}

	// mMVPMatrix is an abbreviation for "Model View Projection Matrix"
	private var mMVPMatrix:FloatArray = FloatArray(16)
	private var mProjectionMatrix:FloatArray = FloatArray(16)
	private var mViewMatrix:FloatArray = FloatArray(16)
	private var mRotationMatrix:FloatArray = FloatArray(16)

	override fun onSurfaceCreated(p0: GL10, p1: EGLConfig?) {
		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
	}

	override fun onDrawFrame(p0: GL10) {
		val scratch = FloatArray(16)
		// Draw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
		// Set the camera position (View matrix)
		Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
		// Calculate the projection and view transformation
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
		// Draw square
		mSquare.draw(mMVPMatrix)

		// Create a rotation for the triangle

		// Use the following code to generate constant rotation.
		// Leave this code out when using TouchEvents.
		// long time = SystemClock.uptimeMillis() % 4000L;
		// float angle = 0.090f * ((int) time);
		Matrix.setRotateM(mRotationMatrix, 0, angle, 0f, 0f, 1.0f)

		// Combine the rotation matrix with the projection and camera view
		// Note that the mMVPMatrix factor *must be first* in order
		// for the matrix multiplication product to be correct.
		Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0)

		// Draw triangle
		mTriangle.draw(scratch)
	}

	override fun onSurfaceChanged(p0: GL10, width: Int, height: Int) {
		// Adjust the viewport based on geometry changes,
		// such as screen rotation
		GLES20.glViewport(0, 0, width, height)

		val ratio:Float = width.toFloat() / height.toFloat()

		// this projection matrix is applied to object coordinates
		// in the onDrawFrame() method
		Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
	}

}