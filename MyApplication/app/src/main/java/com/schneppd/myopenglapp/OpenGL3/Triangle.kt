package com.schneppd.myopenglapp.OpenGL3

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import android.opengl.GLES30
import android.content.ContentValues.TAG
import android.util.Log


/**
 * Created by david.schnepp on 10/07/2017.
 */
class Triangle {
    companion object Static {
        val vertexShaderCode:String = """
            #version 300 es
            in vec4 vPosition;
            void main() {
                gl_Position = vPosition;
            }
        """

        val fragmentShaderCode:String = """
            #version 300 es
            precision mediump float;
            out vec4 fragColor;
            void main(){
                fragColor = vec4 ( 1.0, 0.0, 0.0, 1.0 );
            }
        """

        val verticesData:FloatArray = floatArrayOf(
                0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f
        )

        val TAG = "OpenGL3/Triangle"
    }

    private var verticesBuffer: FloatBuffer
    private var programObject:Int = 0
    private var mPositionHandle:Int = 0
    private var mColorHandle:Int = 0
    private var mMVPMatrixHandle:Int = 0

    init{
        // initialize vertex byte buffer for shape coordinates
        var bb: ByteBuffer = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                verticesData.size * 4)
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())
        // create a floating point buffer from the ByteBuffer
        verticesBuffer = bb.asFloatBuffer()
        // add the coordinates to the FloatBuffer
        verticesBuffer.put(verticesData)
        // set the buffer to read the first coordinate
        verticesBuffer.position(0)
    }

    fun prepareGL(){
        var vertexShader: Int
        var fragmentShader: Int
        var lProgramObject:Int
        var linked = IntArray(1)

        // Load the vertex/fragment shaders
        vertexShader = LoadShader ( GLES30.GL_VERTEX_SHADER, vertexShaderCode )
        fragmentShader = LoadShader ( GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode )

        // Create the program object
        lProgramObject = GLES30.glCreateProgram()
        //error
        if ( lProgramObject == 0 )
        {
            return
        }
        GLES30.glAttachShader (lProgramObject, vertexShader)
        GLES30.glAttachShader (lProgramObject, fragmentShader)

        // Bind vPosition to attribute 0
        GLES30.glBindAttribLocation ( lProgramObject, 0, "vPosition" );

        // Link the program
        GLES30.glLinkProgram ( lProgramObject )

        // Check the link status
        GLES30.glGetProgramiv ( lProgramObject, GLES30.GL_LINK_STATUS, linked, 0 )

        if ( linked[0] == 0 )
        {
            Log.e ( TAG, "Error linking program:" );
            Log.e ( TAG, GLES30.glGetProgramInfoLog ( lProgramObject ) )
            GLES30.glDeleteProgram ( lProgramObject );
            return
        }

        // Store the program object
        programObject = lProgramObject




    }

    fun drawGL(){
        // Use the program object
        GLES30.glUseProgram ( programObject )
        // Load the vertex data
        GLES30.glVertexAttribPointer ( 0, 3, GLES30.GL_FLOAT, false, 0, verticesBuffer )
        GLES30.glEnableVertexAttribArray ( 0 )
        GLES30.glDrawArrays ( GLES30.GL_TRIANGLES, 0, 3 )
    }



    ///
    // Create a shader object, load the shader source, and
    // compile the shader.
    //
    private fun LoadShader(type: Int, shaderSrc: String): Int {
        var shader: Int = 0
        val compiled = IntArray(1)

        // Create the shader object
        shader = GLES30.glCreateShader(type)

        if (shader == 0) {
            return 0
        }

        // Load the shader source
        GLES30.glShaderSource(shader, shaderSrc)

        // Compile the shader
        GLES30.glCompileShader(shader)

        // Check the compile status
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)

        if (compiled[0] == 0) {
            //Log.e(TAG, GLES30.glGetShaderInfoLog(shader))
            GLES30.glDeleteShader(shader)
            return 0
        }

        return shader
    }


}