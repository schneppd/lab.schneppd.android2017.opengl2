package com.schneppd.myopenglapp.OpenGl2v1

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

import android.opengl.GLES20

/**
 * Created by schneppd on 7/9/17.
 */
class Square {
	companion object Static {
		// This matrix member variable provides a hook to manipulate
		// the coordinates of the objects that use this vertex shader
		// The matrix must be included as a modifier of gl_Position.
		// Note that the uMVPMatrix factor *must be first* in order
		// for the matrix multiplication product to be correct.
		val vertexShaderCode:String = """
			uniform mat4 uMVPMatrix;
			attribute vec4 vPosition;
			void main() {
			gl_Position = uMVPMatrix * vPosition;
			}"""
		val fragmentShaderCode = """
			precision mediump float;
			uniform vec4 vColor;
			void main() {
				gl_FragColor = vColor;
			}
			"""

		// number of coordinates per vertex in this array
		val COORDS_PER_VERTEX = 3
		val squareCoords:FloatArray = floatArrayOf(
				-0.5f,  0.5f, 0.0f,   // top left
				-0.5f, -0.5f, 0.0f,   // bottom left
				0.5f, -0.5f, 0.0f,   // bottom right
				0.5f,  0.5f, 0.0f  // top right
		)
		val drawOrder:ShortArray = shortArrayOf(
				0, 1, 2
				, 0, 2, 3
		) // order to draw vertices
		val vertexStride:Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
		val color:FloatArray = floatArrayOf(
				0.2f, 0.709803922f, 0.898039216f, 1.0f
		)

	}

	private var vertexBuffer: FloatBuffer
	private var drawListBuffer: ShortBuffer? = null
	private var mProgram: Int = 0
	private var mPositionHandle: Int = 0
	private var mColorHandle: Int = 0
	private var mMVPMatrixHandle: Int = 0

	init{
		// initialize vertex byte buffer for shape coordinates
		var bb:ByteBuffer = ByteBuffer.allocateDirect(
				// (number of coordinate values * 4 bytes per float)
				squareCoords.size * 4)
		// use the device hardware's native byte order
		bb.order(ByteOrder.nativeOrder())
		// create a floating point buffer from the ByteBuffer
		vertexBuffer = bb.asFloatBuffer()
		// add the coordinates to the FloatBuffer
		vertexBuffer.put(squareCoords)
		// set the buffer to read the first coordinate
		vertexBuffer.position(0)

		// initialize byte buffer for the draw list
		var dlb = ByteBuffer.allocateDirect(drawOrder.size * 2)// (# of coordinate values * 2 bytes per short)
		dlb.order(ByteOrder.nativeOrder())
		drawListBuffer = dlb.asShortBuffer()
		drawListBuffer!!.put(drawOrder)
		drawListBuffer!!.position(0)


		// prepare shaders and OpenGL program
		val vertexShader = CustomGLRenderer.loadShader(
				GLES20.GL_VERTEX_SHADER, vertexShaderCode
		)
		val fragmentShader = CustomGLRenderer.loadShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode
		)
		mProgram = GLES20.glCreateProgram() // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader) // add the vertex shader to program
		GLES20.glAttachShader(mProgram, fragmentShader) // add the fragment shader to program
		GLES20.glLinkProgram(mProgram) // create OpenGL program executables
	}

	/**
	 * Encapsulates the OpenGL ES instructions for drawing this shape.
	 *
	 * @param mvpMatrix - The Model View Project matrix in which to draw
	 * this shape.
	 */
	fun draw(mvpMatrix:FloatArray){
		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram)
		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle)
		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(
				mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, vertexBuffer)
		// get handle to fragment shader's vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
		// Set color for drawing the triangle
		GLES20.glUniform4fv(mColorHandle, 1, color, 0)
		// get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
		CustomGLRenderer.checkGlError("glGetUniformLocation")
		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
		CustomGLRenderer.checkGlError("glUniformMatrix4fv")
		// Draw the square
		GLES20.glDrawElements(
				GLES20.GL_TRIANGLES, drawOrder.size,
				GLES20.GL_UNSIGNED_SHORT, drawListBuffer!!)
		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle)
	}
}