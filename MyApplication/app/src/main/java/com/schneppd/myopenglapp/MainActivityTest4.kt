package com.schneppd.myopenglapp

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.schneppd.myopenglapp.Multitouch.MoveGestureDetector
import com.schneppd.myopenglapp.Multitouch.RotateGestureDetector
import kotlinx.android.synthetic.main.content_main.*

/**
 * Created by david.schnepp on 17/07/2017.
 */
class MainActivityTest4 : MainActivityBase(), View.OnTouchListener {

    private var mScaleFactor = 1.0f
    private var mRotationDegrees = 0f
    private var mFocusX = 0f
    private var mFocusY = 0f

    var mScaleDetector: ScaleGestureDetector? = null
    var mRotateDetector: RotateGestureDetector? = null
    var mMoveDetector: MoveGestureDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mScaleDetector = ScaleGestureDetector(applicationContext, ScaleListener())
        mRotateDetector = RotateGestureDetector(applicationContext, RotateListener())
        mMoveDetector = MoveGestureDetector(applicationContext, MoveListener())

        ivUserPicture.setOnTouchListener { view, motionEvent -> this.onTouch(view, motionEvent) }

    }

    override fun onTouch(v:View, event: MotionEvent):Boolean {
        mScaleDetector?.onTouchEvent(event)
        mRotateDetector?.onTouchEvent(event)
        //mMoveDetector?.onTouchEvent(event)

        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor // scale change since previous event
            return true
        }
    }

    private inner class RotateListener : RotateGestureDetector.SimpleOnRotateGestureListener() {
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            mRotationDegrees -= detector.rotationDegreesDelta
            return true
        }
    }

    private inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            val d = detector.focusDelta
            mFocusX += d.x
            mFocusY += d.y

            return true
        }
    }



}