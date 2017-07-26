package com.schneppd.myopenglapp.OpenGl2v4

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import org.rajawali3d.math.vector.Vector3

import org.rajawali3d.view.SurfaceView

/**
 * Created by david.schnepp on 12/07/2017.
 */

class CustomGLSurfaceView(context: Context, attrs:AttributeSet) : SurfaceView(context, attrs), View.OnTouchListener {

    val renderer = CustomGLRenderer(this.context)

    init {
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2)
        /*
        //required for a translucent window
        setZOrderOnTop(true)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        //holder.setFormat(PixelFormat.RGBA_8888)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setTransparent(true)
        */
        setTransparent(true)
        setSurfaceRenderer(renderer)
    }

    fun scaleRenderedElement(userScale:Float){
        //renderer.userScale = userScale
    }

    fun scaleCurrentModel(userScale:Double){
        renderer.scaleModel(userScale)
    }

    fun moveCurrentModel(position: PointF){
        //calculate position in Gl scene

        //renderer.userScale = userScale
        val touchX = position.x.toDouble()
        val touchY = position.y.toDouble()

        val surfaceWidth = this.width.toDouble()
        val surfaceHeight = this.height.toDouble()

        val glRangeX = surfaceWidth / 2.0
        val glRangeY = surfaceHeight / 2.0

        val glPosX = (touchX - glRangeX) / glRangeX
        val glPosY = ((touchY - glRangeY) / glRangeY) * -1.0
        val glPosZ = 0.0

        val newPos = Vector3(glPosX, glPosY, glPosZ)
        renderer.moveModel(newPos)
    }

    fun moveCurrentModel(position: PointL){
        //calculate position in Gl scene

        //renderer.userScale = userScale
        val touchX = position.x
        val touchY = position.y

        val surfaceWidth = this.width.toDouble()
        val surfaceHeight = this.height.toDouble()

        val glRangeX = surfaceWidth / 2.0
        val glRangeY = surfaceHeight / 2.0

        val glPosX = (touchX - glRangeX) / glRangeX
        val glPosY = ((touchY - glRangeY) / glRangeY) * -1.0
        val glPosZ = 0.0

        val newPos = Vector3(glPosX, glPosY, glPosZ)
        renderer.moveModel(newPos)
    }

    fun rotateCurrentModel(angle:Double){
        renderer.userRotation = angle
    }

    fun loadModel(){
        renderer.loadModel()
    }

    override fun onTouch(p0: View?, p1: MotionEvent): Boolean {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val b = p1.action
        return true
    }


}