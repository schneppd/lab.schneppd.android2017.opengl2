package com.schneppd.myopenglapp.OpenGl2v4

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet

import org.rajawali3d.view.SurfaceView

/**
 * Created by david.schnepp on 12/07/2017.
 */
class CustomGLSurfaceView(context: Context, attrs:AttributeSet) : SurfaceView(context, attrs){

    val renderer = CustomGLRenderer(this.context)


    init {
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2)
        //required for a translucent window
        setZOrderOnTop(true)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        //holder.setFormat(PixelFormat.RGBA_8888)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        // Set the Renderer for drawing on the GLSurfaceView
        // Render the view only when there is a change in the drawing data
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        setTransparent(true)

        setSurfaceRenderer(renderer)
    }


}