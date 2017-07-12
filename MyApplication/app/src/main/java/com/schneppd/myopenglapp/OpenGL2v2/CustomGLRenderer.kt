package com.schneppd.myopenglapp.OpenGl2v2

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
 * Created by david.schnepp on 11/07/2017.
 */



class CustomGLRenderer : GLSurfaceView.Renderer{
    companion object Static {
        val platformBytesPerFloat = 4
        val strideBytes = platformBytesPerFloat * 7
        val positionOffset = 0
        val positionDataSize = 3
        val colorOffset = 3
        val colorDataSize = 4

        val vertexShaderCode:String = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            attribute vec4 a_Color;
            varying vec4 v_Color;
            void main() {
            v_Color = a_Color;
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
    }

    //matricies
    var modelMatrix = FloatArray(16)
    var viewMatrix = FloatArray(16)
    var projectionMatrix = FloatArray(16)
    var shaderMatrix = FloatArray(16)

    //element to draw
    var triangle1Vertices:FloatBuffer
    var triangle2Vertices:FloatBuffer
    var triangle3Vertices:FloatBuffer

    //GL handles
    var shaderHandle = 0
    var positionHandle = 0
    var colorHandle = 0


    init{
        val triangle1VerticesData = floatArrayOf(
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                0.5f, -0.25f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.559016994f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f
        )
        val triangle2VerticesData = floatArrayOf(
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                0.5f, -0.25f, 0.0f,
                0.0f, 1.0f, 1.0f, 1.0f,
                0.0f, 0.559016994f, 0.0f,
                1.0f, 0.0f, 1.0f, 1.0f
        )
        val triangle3VerticesData = floatArrayOf(
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                0.5f, -0.25f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f,
                0.0f, 0.559016994f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        )
        triangle1Vertices = initBuffer(triangle1VerticesData)
        triangle2Vertices = initBuffer(triangle2VerticesData)
        triangle3Vertices = initBuffer(triangle3VerticesData)

    }

    protected fun initBuffer(verticesData:FloatArray): FloatBuffer{
        val buffer = ByteBuffer.allocateDirect(verticesData.size * platformBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buffer.put(verticesData).position(0)
        return buffer
    }



    override fun onSurfaceCreated(p0: GL10, p1: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = 1.5f

        val lookX = 0.0f
        val lookY = 0.0f
        val lookZ = -5.0f

        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)
        val vertexShaderHandle = createShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShaderHandle = createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        val programHandle = createGlProgram(vertexShaderHandle, fragmentShaderHandle)

        shaderHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix")
        positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        colorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color")

        GLES20.glUseProgram(programHandle)
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

    fun createGlProgram(vertexShaderHandle:Int, fragmentShaderHandle:Int) : Int{
        var programHandle = GLES20.glCreateProgram()
        if (programHandle != 0){
            GLES20.glAttachShader(programHandle, vertexShaderHandle)
            GLES20.glAttachShader(programHandle, fragmentShaderHandle)


            GLES20.glBindAttribLocation(programHandle, 0, "a_Position")
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color")

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
        val angleInDegrees = 360.0f / 10000.0f * time.toInt()

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)
        drawTriangle(triangle1Vertices)

        // Draw one translated a bit down and rotated to be flat on the ground.
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0.0f, -1.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)
        drawTriangle(triangle1Vertices)

        // Draw one translated a bit to the right and rotated to be facing to the left.
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f)
        drawTriangle(triangle1Vertices)
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

    private fun drawTriangle(triangle:FloatBuffer){
        triangle.position(positionOffset)
        GLES20.glVertexAttribPointer(positionHandle, positionDataSize, GLES20.GL_FLOAT, false, strideBytes, triangle)

        GLES20.glEnableVertexAttribArray(positionHandle)

        // Pass in the color information
        triangle.position(colorOffset)
        GLES20.glVertexAttribPointer(colorHandle, colorDataSize, GLES20.GL_FLOAT, false,
                strideBytes, triangle)

        GLES20.glEnableVertexAttribArray(colorHandle)

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(shaderMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(shaderMatrix, 0, projectionMatrix, 0, shaderMatrix, 0)

        GLES20.glUniformMatrix4fv(shaderHandle, 1, false, shaderMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    }


}