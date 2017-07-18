package com.schneppd.myopenglapp

import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.MotionEventCompat
import android.util.Log
import android.view.*
import android.view.MotionEvent
import kotlinx.android.synthetic.main.content_main.*

/**
 * Created by david.schnepp on 17/07/2017.
 */
class MainActivityTest5 : MainActivityBase()
        //, GestureDetector.OnGestureListener
        //, GestureDetector.OnDoubleTapListener
       // , ScaleGestureDetector.OnScaleGestureListener
        , View.OnTouchListener
{
    /*
    var isScaleMotionDetected = false
    lateinit var gestureDetector: GestureDetectorCompat
    lateinit var scaleDetector: ScaleGestureDetector
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //gestureDetector = GestureDetectorCompat(this, this)
        //gestureDetector.setOnDoubleTapListener(this)

        //scaleDetector = ScaleGestureDetector(this, this)

        //ivUserPicture.setOnTouchListener { view, motionEvent -> this.onTouch(view, motionEvent) }
        ivUserPicture.setOnTouchListener(this)
    }

    var isTouchInProgress = false
    override fun onTouch(v:View, event: MotionEvent):Boolean {
        when(event.action and MotionEvent.ACTION_MASK){
            MotionEvent.ACTION_UP -> {
                isTouchInProgress = false
            }
            MotionEvent.ACTION_DOWN -> {
                isTouchInProgress = true
            }
            MotionEvent.ACTION_CANCEL -> {
                val b = 1
            }
            MotionEvent.ACTION_HOVER_MOVE -> {
                val b = 1
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val b = 1
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val b = 1
            }
            MotionEvent.ACTION_MOVE -> {
                val b = 1
                /*
                val x = event.x
                val y = event.y

                val widthSurface = ivUserPicture.width
                val heightSurface = ivUserPicture.height

                val centerX = widthSurface / 2
                val centerY = heightSurface / 2

                val distX = if(x > centerX) x-centerX else x
                val distY = if(y > centerY) y-centerY else y
                */
            }
        }
        return true
    }
/*

    override fun onTouchEvent(event: MotionEvent): Boolean {


        return super.onTouchEvent(event)

    }
*/
    fun isDragMotion(event: MotionEvent) : Boolean{
        return event.pointerCount == 1
    }

    fun onDrag(event: MotionEvent) : Boolean{
        when(event.action){
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y

                val widthSurface = ivUserPicture.width
                val heightSurface = ivUserPicture.height

                val centerX = widthSurface / 2
                val centerY = heightSurface / 2

                val distX = if(x > centerX) x-centerX else x
                val distY = if(y > centerY) y-centerY else y
            }
        }
        return true
    }

    /*
    override fun onDown(event: MotionEvent):Boolean{
        Log.d(MainActivity.DEBUG_TAG,"onDown: ${event}")
        return true
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent,
                         velocityX: Float, velocityY: Float): Boolean {
        Log.d(MainActivity.DEBUG_TAG, "onFling: ${event1} ${event2}")
        return true
    }


    override fun onLongPress(event: MotionEvent) {
        Log.d(MainActivity.DEBUG_TAG, "onLongPress: ${event}")
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float,
                          distanceY: Float): Boolean {
        Log.d(MainActivity.DEBUG_TAG, "onScroll: ${e1} ${e2}")
        return true
    }


    override fun onShowPress(event: MotionEvent) {
        Log.d(MainActivity.DEBUG_TAG, "onShowPress: ${event}")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d(MainActivity.DEBUG_TAG, "onSingleTapUp: ${event}")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        Log.d(MainActivity.DEBUG_TAG, "onDoubleTap: ${event}")
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        Log.d(MainActivity.DEBUG_TAG, "onDoubleTapEvent: ${event}")
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        Log.d(MainActivity.DEBUG_TAG, "onSingleTapConfirmed: ${event}")
        return true
    }
    */

/*
    override fun onScaleEnd(detector:ScaleGestureDetector) {
        isScaleMotionDetected = false
    }

    override fun onScaleBegin(detector:ScaleGestureDetector) : Boolean {
        isScaleMotionDetected = true
        return true
    }

    override fun onScale(detector:ScaleGestureDetector) : Boolean {
        val scale = detector.scaleFactor
        Log.d("User scal", "zoom ongoing, scale: " + scale)
        svUserModel.scaleRenderedElement(scale)
        return false
    }
    */
}