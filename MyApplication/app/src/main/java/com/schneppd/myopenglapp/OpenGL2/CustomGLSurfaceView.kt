package com.schneppd.myopenglapp.OpenGL2

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by schneppd on 7/9/17.
 */
class CustomGLSurfaceView(context:Context, attrs:AttributeSet) : GLSurfaceView(context, attrs){

	val mRenderer: CustomGLRenderer = CustomGLRenderer()
	val TOUCH_SCALE_FACTOR =  180.0f / 320f
	var mPreviousX = 0f
	var mPreviousY = 0f

	init {
		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2)
		//required for a translucent window
		setZOrderOnTop(true)
		setEGLConfigChooser(8, 8, 8, 8, 16, 0)
		//holder.setFormat(PixelFormat.RGBA_8888)
		holder.setFormat(PixelFormat.TRANSLUCENT)
		// Set the Renderer for drawing on the GLSurfaceView
		setRenderer(mRenderer)

		// Render the view only when there is a change in the drawing data
		renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY



	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		mPreviousX = x
		mPreviousY = y

		when(event.action){
			MotionEvent.ACTION_MOVE -> {
				var dx = event.x - mPreviousX
				var dy = event.y - mPreviousY

				// reverse direction of rotation above the mid-line
				if (y > height / 2){
					dx *= -1
				}
				// reverse direction of rotation to left of the mid-line
				if (x < width / 2){
					dy *= -1
				}

				mRenderer.angle = mRenderer.angle + ((dx + dy) * TOUCH_SCALE_FACTOR) // = 180.0f / 320
				requestRender()
			}
		}

		return super.onTouchEvent(event)
	}
}