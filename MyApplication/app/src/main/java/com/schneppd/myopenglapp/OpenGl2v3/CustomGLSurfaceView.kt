package com.schneppd.myopenglapp.OpenGl2v3

import android.app.ActivityManager
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 * Created by schneppd on 7/11/17.
 */
class CustomGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs){

	val mRenderer = CustomGLRenderer3()

	init {
		//test if opengl2 supported
		val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
		val configurationInfo = activityManager.deviceConfigurationInfo
		val isEs2Supported = configurationInfo.reqGlEsVersion >= 0x20000

		if(isEs2Supported) {
			setEGLContextClientVersion(2)
			setZOrderOnTop(true)
			setEGLConfigChooser(8, 8, 8, 8, 16, 0)
			holder.setFormat(PixelFormat.TRANSLUCENT)
			setRenderer(mRenderer)

		}
		else{
			//should throw error

		}

	}

}