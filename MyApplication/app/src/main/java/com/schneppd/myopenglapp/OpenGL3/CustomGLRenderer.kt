package com.schneppd.myopenglapp.OpenGL3

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.schneppd.myopenglapp.OpenGL3.Triangle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by david.schnepp on 10/07/2017.
 */
class CustomGLRenderer : GLSurfaceView.Renderer{
    companion object Static {
        val TAG = "CustomGLRenderer"
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


    override fun onSurfaceCreated(p0: GL10, p1: EGLConfig?) {
        // Set the background frame color
        GLES30.glClearColor ( 1.0f, 1.0f, 1.0f, 0.0f )
        mTriangle.prepareGL()
    }

    override fun onDrawFrame(p0: GL10) {
        // Clear the color buffer
        GLES30.glClear ( GLES30.GL_COLOR_BUFFER_BIT )
        mTriangle.drawGL()

    }

    override fun onSurfaceChanged(p0: GL10, width: Int, height: Int) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES30.glViewport(0, 0, width, height)
    }

}