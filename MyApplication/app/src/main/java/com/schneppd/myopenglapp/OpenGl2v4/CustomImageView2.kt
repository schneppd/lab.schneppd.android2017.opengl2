package com.schneppd.myopenglapp.OpenGl2v4

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import java.util.*

/**
 * Created by david.schnepp on 25/07/2017.
 */
class CustomImageView2(context: Context, attrs: AttributeSet) : ImageView(context, attrs), View.OnTouchListener {
    enum class TouchEventComposition{
        UNDECIDED, ONE_FINGER, TWO_FINGER, MULTIPLE_FINGER
    }

    var touchComposition:TouchEventComposition = TouchEventComposition.UNDECIDED
    var scaleFactor = 1.0f
    val scaleDetector:ScaleGestureDetector
    var touchStart = 0L
    var dragGestureCooldown = 0L
    val delayToDetectDragStartGesture = 145L //wait 150millisec
    val delayToDetectDragStartAfterTwoFingerGestureStop = 950L //wait 950millisec

    val noiseMovementIgnoredBeforeDetectDragStartGesture = 3
    val noiseMovementIgnoredBeforeDetectDragAfterTwoFingerGestureStop = 5
    var numberMovementToIgnore = 0






    companion object {
        var isScaleListenerTriggered = false
        var number2FingerMovementUnknown = 0
        var isRotationGestureDetected = false
        var isScaleGestureDetected = false
        var isGestureConfirmed = false
        var startUnknownMovement:MotionEvent? = null
        var scaleCorrectionCache = FloatArray(4)
        var nbOnScaledCached = 0
        var backupMotionEvent:LinkedList<MotionEvent> = LinkedList<MotionEvent>() //used to deal with edge case on scale detected
        var onScaleStart = 0L
        var nbOnScaleAfterOnScaleStart = 0
        var nbOnMoveAfterOnScaleStart = 0
        var isScaleGestureEdgeCaseTriggered = false
    }


    init {
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        this.setOnTouchListener(this)
    }

    ///process touch
    override fun onTouch(p0: View?, p1: MotionEvent): Boolean {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        when(p1.action and MotionEvent.ACTION_MASK){
            MotionEvent.ACTION_CANCEL -> cancelTouchGesture(p1)
            MotionEvent.ACTION_UP -> endTouchGesture(p1)
            MotionEvent.ACTION_DOWN -> startTouchGesture(p1)
            MotionEvent.ACTION_MOVE -> moveTouchPointer(p1)
            MotionEvent.ACTION_POINTER_DOWN -> addTouchPointerToGesture(p1)
            MotionEvent.ACTION_POINTER_UP -> removeTouchPointerToGesture(p1)
        }

        scaleDetector.onTouchEvent(p1)

        return true
    }

    fun startTouchGesture(event:MotionEvent){
        Log.d("CustomImageView2", "startTouchGesture +++++++++")
        touchStart = Date().time
        numberMovementToIgnore = noiseMovementIgnoredBeforeDetectDragStartGesture
    }
    fun cancelTouchGesture(event:MotionEvent){
        Log.d("CustomImageView2", "cancelTouchGesture +++++++++")
        endTouchGesture(event)
    }
    fun endTouchGesture(event:MotionEvent){
        isScaleListenerTriggered = false
        number2FingerMovementUnknown = 0
        isRotationGestureDetected = false
        isScaleGestureDetected = false
        startUnknownMovement = null

        nbOnScaledCached = 0
        isGestureConfirmed = false

        nbOnScaleAfterOnScaleStart = 0
        isGestureConfirmed = false
        nbOnMoveAfterOnScaleStart = 0
        backupMotionEvent.clear()
        isScaleGestureEdgeCaseTriggered = false

        Log.d("CustomImageView2", "endTouchGesture +++++++++")
    }
    fun addTouchPointerToGesture(event:MotionEvent){
        Log.d("CustomImageView2", "addTouchPointerToGesture +++++++++")
        val nbFinger = event.pointerCount

        if(nbFinger == 1 && touchComposition == TouchEventComposition.UNDECIDED && event.eventTime > dragGestureCooldown){
                touchComposition = TouchEventComposition.ONE_FINGER
        }

        if(nbFinger >= 2)
            touchComposition = TouchEventComposition.TWO_FINGER

    }
    fun removeTouchPointerToGesture(event:MotionEvent){
        Log.d("CustomImageView2", "removeTouchPointerToGesture ${event.eventTime} +++++++++")
        val nbFinger = event.pointerCount
        if(nbFinger == 1 && touchComposition == TouchEventComposition.TWO_FINGER){
            numberMovementToIgnore = noiseMovementIgnoredBeforeDetectDragAfterTwoFingerGestureStop
            //dragGestureCooldown = event.eventTime + delayToDetectDragStartAfterTwoFingerGestureStop
            touchComposition = TouchEventComposition.ONE_FINGER
        }
    }
    fun moveTouchPointer(event:MotionEvent){
        //.d("CustomImageView2", "moveTouchPointer +++++++++")
        val nbFinger = event.pointerCount
        val time = event.eventTime
        if(nbFinger == 1){
            if(numberMovementToIgnore == 0){
                for(p in 0..(nbFinger-1)){
                    val pointerId = event.getPointerId(p)
                    val pointerX = event.getX(p)
                    val pointerY = event.getY(p)
                    Log.d("CustomImageView2", "t:${time} drag to pointer:${pointerId} x:${pointerX} y:${pointerY}")
                }
            }
            else{
                numberMovementToIgnore--
            }
        }
        else{
            Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer ${isRotationGestureDetected} ${isScaleGestureDetected} ${isScaleListenerTriggered} ${nbOnMoveAfterOnScaleStart} ${nbOnScaleAfterOnScaleStart}")
            startUnknownMovement ?: run{
                startUnknownMovement = event
            }
            if(number2FingerMovementUnknown == 6 && !isScaleListenerTriggered) { //it's a rotation
                number2FingerMovementUnknown = 0
                isRotationGestureDetected = true
                Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer rotation detected")
            }
            if(!isRotationGestureDetected && !isScaleGestureDetected){
                number2FingerMovementUnknown++
            }

            //record sample data to detect if need correction (false positive)
            if(!isGestureConfirmed){
                backupMotionEvent.add(event)
            }

            if(isScaleListenerTriggered && nbOnScaleAfterOnScaleStart >= 5 && !isScaleGestureDetected && !isScaleGestureDetected && !isRotationGestureDetected) {

                if (!isScaleGestureEdgeCaseTriggered) {
                    isScaleGestureEdgeCaseTriggered = true
                    //edge case
                    isScaleGestureDetected = true
                    isGestureConfirmed = true
                    number2FingerMovementUnknown = 0
                    backupMotionEvent.clear()
                    Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer edgecase ScaleGesture")
                }
            }

            if((isRotationGestureDetected || isScaleGestureDetected) && doesContinueCurrentGesture(event)){
                if(isRotationGestureDetected){
                    Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer continue rotation")
                }
                if(isScaleGestureDetected){
                    Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer continue scale")
                }

            }
            else{
                //correct if break gesture
                if(isRotationGestureDetected){
                    Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer switch to scale")
                }
                if(isScaleGestureDetected){
                    Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer switch to rotation")
                }
            }

        }

    }

    fun doesContinueCurrentGesture(event:MotionEvent):Boolean{
        if(isRotationGestureDetected){
            val startMovement = startUnknownMovement!!
            val xFinger0StartMovement = startMovement.getX(0)
            val xFinger1StartMovement = startMovement.getX(1)
            val distStartMovement = xFinger0StartMovement - xFinger1StartMovement

            val xFinger0CurrentMovement = event.getX(0)
            val xFinger1CurrentMovement = event.getX(1)
            val distCurrentMovement = xFinger0CurrentMovement - xFinger1CurrentMovement

            val threshold = 30
            if((distCurrentMovement < (distStartMovement + threshold)) && (distCurrentMovement > (distStartMovement - threshold)))
                return true
        }
        else if(isScaleGestureDetected){
            // calculate dist finger is increasing, decreasing
        }
        return false
    }


    private class ScaleListener : ScaleGestureDetector.OnScaleGestureListener{
        override fun onScaleBegin(p0: ScaleGestureDetector): Boolean {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            Log.d("CustomImageView2", "onScaleBegin ${p0.scaleFactor} ${p0.eventTime}")
            onScaleStart = p0.eventTime
            nbOnScaleAfterOnScaleStart = 0
            isScaleListenerTriggered = true
            isGestureConfirmed = false
            nbOnMoveAfterOnScaleStart = 0
            isScaleGestureEdgeCaseTriggered = false
            if(number2FingerMovementUnknown <= 5 && !isRotationGestureDetected){
                number2FingerMovementUnknown = 0
                isScaleGestureDetected = true
                Log.d("CustomImageView2", "t:${p0.eventTime} moveTouchPointer scale detected")
            }

            return true
        }

        override fun onScaleEnd(p0: ScaleGestureDetector) {
            Log.d("CustomImageView2", "onScaleEnd ${p0.scaleFactor} ${p0.eventTime}")
            isScaleListenerTriggered = false
            startUnknownMovement = null
            isRotationGestureDetected = false
            isScaleGestureDetected = false
            nbOnScaledCached = 0
            isGestureConfirmed = false
            backupMotionEvent.clear()
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
        override fun onScale(p0: ScaleGestureDetector?): Boolean {
            nbOnScaleAfterOnScaleStart++
            if(nbOnScaledCached < 4){
                p0?.let{
                    scaleCorrectionCache[nbOnScaledCached] = p0.scaleFactor
                    nbOnScaledCached++
                }

            }
            else{
                //test and correct
                var maxScale = 1f
                var minScale = 1f
                for(scale in scaleCorrectionCache){
                    if(scale < minScale) minScale = scale
                    if(scale > maxScale) maxScale = scale
                }
                if(minScale > 0.98f && maxScale < 1.02){
                    //rotate gesture
                    if(isScaleGestureDetected){
                        //cancel scale TODO
                        isScaleGestureDetected= false
                        isRotationGestureDetected= true
                        Log.d("CustomImageView2", "onScale correct gesture to rotation")
                    }
                }
                else{
                    //scale gesture
                    if(isRotationGestureDetected){
                        //cancel rotate TODO
                        isRotationGestureDetected= false
                        isScaleGestureDetected= true
                        Log.d("CustomImageView2", "onScale correct gesture to scale")
                    }
                }
                isGestureConfirmed = true
            }
            return true
        }
    }
}