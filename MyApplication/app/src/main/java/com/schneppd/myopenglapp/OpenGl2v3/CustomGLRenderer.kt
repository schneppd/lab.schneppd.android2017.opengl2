package com.schneppd.myopenglapp.OpenGl2v3

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by schneppd on 7/11/17.
 */

class CustomGLRenderer : GLSurfaceView.Renderer{
	companion object Static {
		val platformBytesPerFloat = 4
		val positionDataSize = 3
		val colorDataSize = 4
		val normalDataSize = 3

		val vertexShaderCode:String = """
            uniform mat4 u_MVPMatrix;
            uniform mat4 u_MVMatrix;
            uniform vec3 u_LightPos;
            attribute vec4 a_Position;
			attribute vec4 a_Color;
			attribute vec3 a_Normal;
			varying vec4 v_Color;
            void main() {
				vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);
				vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
				float distance = length(u_LightPos - modelViewVertex);
				vec3 lightVector = normalize(u_LightPos - modelViewVertex);
				float diffuse = max(dot(modelViewNormal, lightVector), 0.1);
				diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));
				v_Color = a_Color * diffuse;
				gl_Position = u_MVPMatrix * a_Position;
            }
        """

		val fragmentShaderCode:String = """
            precision mediump float;
            varying vec4 v_Color;
            void main() {
                gl_FragColor = v_Color;
            }
        """

		val pointVertexShaderCode:String = """
			uniform mat4 u_MVPMatrix;
			attribute vec4 a_Position;
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
				gl_PointSize = 5.0;
            }
        """

		val pointFragmentShaderCode:String = """
			precision mediump float;
            void main() {
                gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
            }
        """
	}

	//matricies
	var modelMatrix = FloatArray(16)
	var viewMatrix = FloatArray(16)
	var projectionMatrix = FloatArray(16)
	var shaderMatrix = FloatArray(16)
	var lightMatrix = FloatArray(16)

	//element to draw
	var cubePositions: FloatBuffer
	var cubeColors: FloatBuffer
	var cubeNormals: FloatBuffer

	//GL handles
	var mVPMatrixHandle = 0
	var mVMatrixHandle = 0
	var lightHandle = 0
	var positionHandle = 0
	var colorHandle = 0
	var normalHandle = 0

	//light data
	var lightPosInModelSpace = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)
	var lightPosInWorldSpace = FloatArray(4)
	var lightPosInEyeSpace = FloatArray(4)

	//program handle
	var perVertexProgramHandle = 0
	var pointProgramHandle = 0

	init{
		val cubePositionData = floatArrayOf(
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
		val cubeColorData = floatArrayOf(
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
		val cubeNormalData = floatArrayOf(
				// Front face
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,

				// Right face
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,

				// Back face
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,

				// Left face
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,

				// Top face
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,

				// Bottom face
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f
		)
		cubePositions = initBuffer(cubePositionData)
		cubeColors = initBuffer(cubeColorData)
		cubeNormals = initBuffer(cubeNormalData)

	}

	private fun initBuffer(verticesData:FloatArray): FloatBuffer {
		var buffer = ByteBuffer.allocateDirect(verticesData.size * platformBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
		buffer.put(verticesData).position(0)
		return buffer
	}



	override fun onSurfaceCreated(p0: GL10, p1: EGLConfig?) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

		GLES20.glEnable(GLES20.GL_CULL_FACE)
		GLES20.glEnable(GLES20.GL_DEPTH_TEST)

		val eyeX = 0.0f
		val eyeY = 0.0f
		val eyeZ = -0.5f

		val lookX = 0.0f
		val lookY = 0.0f
		val lookZ = -5.0f

		val upX = 0.0f
		val upY = 1.0f
		val upZ = 0.0f

		Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)
		val vertexShaderHandle = createShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
		val fragmentShaderHandle = createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
		perVertexProgramHandle = createGlProgram(vertexShaderHandle, fragmentShaderHandle, arrayOf("a_Position",  "a_Color", "a_Normal"))

		val pointVertexShaderHandle = createShader(GLES20.GL_VERTEX_SHADER, pointVertexShaderCode)
		val pointFragmentShaderHandle = createShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShaderCode)

		pointProgramHandle = createGlProgram(pointVertexShaderHandle, pointFragmentShaderHandle, arrayOf("a_Position"))

	}

	fun createShader(typeShade:Int, code:String) : Int{
		var shaderHandle = GLES20.glCreateShader(typeShade)

		if(shaderHandle != 0){
			GLES20.glShaderSource(shaderHandle, code)
			GLES20.glCompileShader(shaderHandle)
			var compileStatus:IntArray = IntArray(1)
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

			if(compileStatus[0] == 0){
				GLES20.glDeleteShader(shaderHandle)
				shaderHandle = 0
			}
		}
		if (shaderHandle == 0)
			throw RuntimeException("Error creating shader.")

		return shaderHandle

	}

	fun createGlProgram(vertexShaderHandle:Int, fragmentShaderHandle:Int, bindAttributes:Array<String>) : Int{
		var programHandle = GLES20.glCreateProgram()
		if (programHandle != 0){
			GLES20.glAttachShader(programHandle, vertexShaderHandle)
			GLES20.glAttachShader(programHandle, fragmentShaderHandle)

			for((index, value) in bindAttributes.withIndex()){
				GLES20.glBindAttribLocation(programHandle, index, value)
			}

			GLES20.glLinkProgram(programHandle)

			var linkStatus:IntArray = IntArray(1)
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

			if (linkStatus[0] == 0){
				GLES20.glDeleteProgram(programHandle)
				programHandle = 0
			}
		}
		if (programHandle == 0)
			throw RuntimeException("Error creating program.")

		return programHandle
	}

	override fun onDrawFrame(p0: GL10) {
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

		val time = SystemClock.uptimeMillis() % 10000L
		val angleInDegrees = (360.0f / 10000.0f) * time.toInt()

		GLES20.glUseProgram(perVertexProgramHandle)

		mVPMatrixHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_MVPMatrix")
		mVMatrixHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_MVMatrix")
		lightHandle = GLES20.glGetUniformLocation(perVertexProgramHandle, "u_LightPos")
		positionHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_Position")
		colorHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_Color")
		normalHandle = GLES20.glGetAttribLocation(perVertexProgramHandle, "a_Normal")

		Matrix.setIdentityM(lightMatrix, 0)
		Matrix.translateM(lightMatrix, 0, 0.0f, 0.0f, -5.0f)
		Matrix.rotateM(lightMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f)
		Matrix.translateM(lightMatrix, 0, 0.0f, 0.0f, 2.0f)

		Matrix.multiplyMV(lightPosInWorldSpace, 0, lightMatrix, 0, lightPosInModelSpace, 0)
		Matrix.multiplyMV(lightPosInEyeSpace, 0, viewMatrix, 0, lightPosInWorldSpace, 0)

		Matrix.setIdentityM(modelMatrix, 0)
		Matrix.translateM(modelMatrix, 0, 4.0f, 0.0f, -7.0f)
		Matrix.rotateM(modelMatrix, 0, angleInDegrees, 1.0f, 0.0f, 0.0f)
		drawCube()

		Matrix.setIdentityM(modelMatrix, 0)
		Matrix.translateM(modelMatrix, 0, -4.0f, 0.0f, -7.0f)
		Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f)
		drawCube()

		Matrix.setIdentityM(modelMatrix, 0)
		Matrix.translateM(modelMatrix, 0, 0.0f, 4.0f, -7.0f)
		Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)
		drawCube()

		Matrix.setIdentityM(modelMatrix, 0)
		Matrix.translateM(modelMatrix, 0, 0.0f, -4.0f, -7.0f)
		drawCube()

		Matrix.setIdentityM(modelMatrix, 0)
		Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -5.0f)
		Matrix.rotateM(modelMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f)
		drawCube()

		GLES20.glUseProgram(pointProgramHandle)
		drawLight()
	}

	override fun onSurfaceChanged(p0: GL10, width: Int, height: Int) {
		GLES20.glViewport(0, 0, width, height)

		val ratio = width.toFloat() / height.toFloat()
		val left = -ratio
		val right = ratio
		val bottom = -1.0f
		val top = 1.0f
		val near = 1.0f
		val far = 10.0f

		Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)
	}

	private fun drawCube(){
		cubePositions.position(0)
		GLES20.glVertexAttribPointer(positionHandle, positionDataSize, GLES20.GL_FLOAT, false, 0, cubePositions)
		GLES20.glEnableVertexAttribArray(positionHandle)

		cubeColors.position(0)
		GLES20.glVertexAttribPointer(colorHandle, colorDataSize, GLES20.GL_FLOAT, false, 0, cubeColors)
		GLES20.glEnableVertexAttribArray(colorHandle)

		cubeNormals.position(0)
		GLES20.glVertexAttribPointer(normalHandle, normalDataSize, GLES20.GL_FLOAT, false, 0, cubeNormals)
		GLES20.glEnableVertexAttribArray(normalHandle)

		Matrix.multiplyMM(shaderMatrix, 0, viewMatrix, 0, modelMatrix, 0)
		GLES20.glUniformMatrix4fv(mVMatrixHandle, 1, false, shaderMatrix, 0)

		Matrix.multiplyMM(shaderMatrix, 0, projectionMatrix, 0, shaderMatrix, 0)
		GLES20.glUniformMatrix4fv(mVMatrixHandle, 1, false, shaderMatrix, 0)

		GLES20.glUniform3f(lightHandle, lightPosInEyeSpace[0], lightPosInEyeSpace[1], lightPosInEyeSpace[2])

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
	}

	private fun drawLight(){
		var pointMVPMatrixHandle = GLES20.glGetUniformLocation(pointProgramHandle, "u_MVPMatrix")
		var pointPositionHandle = GLES20.glGetAttribLocation(pointProgramHandle, "a_Position")

		GLES20.glVertexAttrib3f(pointPositionHandle, lightPosInModelSpace[0], lightPosInModelSpace[1], lightPosInModelSpace[2])
		GLES20.glDisableVertexAttribArray(pointPositionHandle)

		Matrix.multiplyMM(shaderMatrix, 0, viewMatrix, 0, lightMatrix, 0)
		Matrix.multiplyMM(shaderMatrix, 0, projectionMatrix, 0, shaderMatrix, 0)
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, shaderMatrix, 0)

		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
	}

}