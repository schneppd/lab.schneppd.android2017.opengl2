package com.schneppd.myopenglapp.OpenGl2v4

import android.content.Context
import android.graphics.PointF
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

    var executorTouchGesture:CustomGLSurfaceView? = null






    companion object {
        var isScaleListenerTriggered = false
        var number2FingerMovementUnknown = 0
        var isRotationGestureDetected = false
        var isScaleGestureDetected = false
        var isGestureConfirmed = false
        var startUnknownMovement:FloatArray? = null
        var scaleCorrectionCache = FloatArray(4)
        var nbOnScaledCached = 0
        var backupMotionEvent = LinkedList<FloatArray>() //used to deal with edge case on scale detected
        var onScaleStart = 0L
        var nbOnScaleAfterOnScaleStart = 0
        var nbOnMoveAfterOnScaleStart = 0
        var isScaleGestureEdgeCaseTriggered = false
    }


    init {
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        this.setOnTouchListener(this)
    }

    fun linkWith(executor:CustomGLSurfaceView){
        executorTouchGesture = executor
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
                    val pos = PointL(pointerX.toDouble(), pointerY.toDouble())
                    executorTouchGesture?.moveCurrentModel(pos)

                }
            }
            else{
                numberMovementToIgnore--
            }
        }
        else{
            Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer isRotationGestureDetected ${isRotationGestureDetected} isScaleGestureDetected ${isScaleGestureDetected} isScaleListenerTriggered ${isScaleListenerTriggered} ${nbOnMoveAfterOnScaleStart} ${nbOnScaleAfterOnScaleStart}")
            startUnknownMovement ?: run{
                startUnknownMovement = extractStorablePositionFrom(event)
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
                val newPosition = extractStorablePositionFrom(event)

                backupMotionEvent.add(newPosition)
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
                    val newPosition = extractStorablePositionFrom(event)
                    val angle = angleBetween2Lines(startUnknownMovement!!, newPosition)
                    Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer angle: ${angle}")
                    executorTouchGesture?.rotateCurrentModel(angle.toDouble())
                }
                if(isScaleGestureDetected){
                    val newPosition = extractStorablePositionFrom(event)
                    val scale = scaleBetween2Lines(startUnknownMovement!!, newPosition)
                    Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer scale: ${scale}")
                    executorTouchGesture?.scaleCurrentModel(scale.toDouble())
                }

            }
            else{
                //correct if break gesture
                if(isRotationGestureDetected){
                    recordeSwitchNewMovement(event)
                    Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer switch to scale")
                }
                if(isScaleGestureDetected){
                    recordeSwitchNewMovement(event)
                    Log.d("CustomImageView2", "t:${time} ${nbFinger} moveTouchPointer switch to rotation")
                }
            }

        }

    }

    fun recordeSwitchNewMovement(event:MotionEvent){
        if(isRotationGestureDetected){
            isRotationGestureDetected = false
            isScaleGestureDetected = true
        }
        else{
            isRotationGestureDetected = true
            isScaleGestureDetected = false
        }
        startUnknownMovement = extractStorablePositionFrom(event)
    }

    fun extractStorablePositionFrom(event:MotionEvent) : FloatArray{
        val newPosition = FloatArray(4)
        newPosition[0] = event.getX(0)
        newPosition[1] = event.getY(0)
        newPosition[2] = event.getX(1)
        newPosition[3] = event.getY(1)
        return newPosition
    }

    fun angleBetween2Lines(origin:FloatArray, newPosition:FloatArray) : Float{
        val distFirstPositionX = origin[0] - origin[2]
        val distFirstPositionY = origin[3] - origin[1]
        val distSecondPositionX = newPosition[0] - newPosition[2]
        val distSecondPositionY = newPosition[3] - newPosition[1]

        val angle1 = Math.atan2(distFirstPositionY.toDouble(), distFirstPositionX.toDouble())
        val angle2  = Math.atan2(distSecondPositionY.toDouble(), distSecondPositionX.toDouble())

        var calculatedAngle = Math.toDegrees(angle1 - angle2)
        if(calculatedAngle < 0.0) calculatedAngle += 360.0
        return calculatedAngle.toFloat()
    }

    fun scaleBetween2Lines(origin:FloatArray, newPosition:FloatArray) : Float{
        val startDistance = distanceBetween2Points(origin)
        val endDistance = distanceBetween2Points(newPosition)

        val scale = endDistance / startDistance
        return scale.toFloat()
    }

    fun distanceBetween2Points(points:FloatArray) : Double{
        val distXOrigin = points[2] - points[0]
        val distYOrigin = points[3] - points[1]

        val distance = Math.sqrt( (Math.pow(distXOrigin.toDouble(), 2.0) + Math.pow(distYOrigin.toDouble(), 2.0)) )
        return distance
    }

    fun doesContinueCurrentGesture(event:MotionEvent):Boolean{
        if(isRotationGestureDetected){
            val startMovement = startUnknownMovement!!
            val newPosition = extractStorablePositionFrom(event)

            val startDistance = distanceBetween2Points(startMovement)
            val endDistance = distanceBetween2Points(newPosition)
            val changeDistance = Math.abs(startDistance - endDistance)

            val threshold = 150
			Log.d("CustomImageView2", "doesContinueCurrentGesture test ${startDistance} ${endDistance}")
            if(changeDistance < threshold){
				Log.d("CustomImageView2", "doesContinueCurrentGesture rotation continue checked")
				return true
			}

        }
        else if(isScaleGestureDetected){
            return true
            /*
            //idea: use rotate angle to test move
            // calculate dist finger is increasing, decreasing
            val startMovement = startUnknownMovement!!
            val newPosition = extractStorablePositionFrom(event)
            val minThresholdMovement = FloatArray(4)
            val maxThresholdMovement = FloatArray(4)

            //create boundaries detection
            val threshold = 150f
            startMovement.forEachIndexed { index, fl -> run{
                    var newPosition = fl
                    //the translation is applied on x coordinates
                    if(index == 0){
                        minThresholdMovement[index] = fl - threshold
                        maxThresholdMovement[index] = fl + threshold
                    }
                    else if(index == 2){
                        minThresholdMovement[index] = fl - threshold
                        maxThresholdMovement[index] = fl + threshold
                    }
                    else{
                        minThresholdMovement[index] = fl
                        maxThresholdMovement[index] = fl
                    }

                }
            }


            val firstFingerMinSegment = FloatArray(4)
            firstFingerMinSegment[0] = minThresholdMovement[0]
            firstFingerMinSegment[1] = minThresholdMovement[1]
            firstFingerMinSegment[2] = newPosition[0]
            firstFingerMinSegment[3] = newPosition[1]
            val secondFingerMinSegment = FloatArray(4)
            secondFingerMinSegment[0] = minThresholdMovement[0]
            secondFingerMinSegment[1] = minThresholdMovement[1]
            secondFingerMinSegment[2] = newPosition[2]
            secondFingerMinSegment[3] = newPosition[3]
            val firstFingerMaxSegment = FloatArray(4)
            firstFingerMaxSegment[0] = maxThresholdMovement[0]
            firstFingerMaxSegment[1] = maxThresholdMovement[1]
            firstFingerMaxSegment[2] = newPosition[0]
            firstFingerMaxSegment[3] = newPosition[1]
            val secondFingerMaxSegment = FloatArray(4)
            secondFingerMaxSegment[0] = maxThresholdMovement[0]
            secondFingerMaxSegment[1] = maxThresholdMovement[1]
            secondFingerMaxSegment[2] = newPosition[2]
            secondFingerMaxSegment[3] = newPosition[3]


            val minCheckFirstFinger = angleBetween2Lines(minThresholdMovement, firstFingerMinSegment)
            val minCheckSecondFinger = angleBetween2Lines(minThresholdMovement, secondFingerMinSegment)
            val maxCheckFirstFinger = angleBetween2Lines(maxThresholdMovement, firstFingerMaxSegment)
            val maxCheckSecondFinger = angleBetween2Lines(maxThresholdMovement, secondFingerMaxSegment)

            val validFirstFingerPosition = (minCheckFirstFinger in 0f..180f) && (maxCheckFirstFinger in 180f..360f)
            val validSecondFingerPosition = (minCheckSecondFinger in 0f..180f) && (maxCheckSecondFinger in 180f..360f)

            Log.d("CustomImageView2", "doesContinueCurrentGesture scaleDown test ${minCheckFirstFinger} ${validFirstFingerPosition} ${minCheckSecondFinger} ${validSecondFingerPosition}")
            if(validFirstFingerPosition && validSecondFingerPosition){
                Log.d("CustomImageView2", "doesContinueCurrentGesture scale checked")
                return true
            }
            else{
                Log.d("CustomImageView2", "doesContinueCurrentGesture scale failed")
            }
            */


            /*
            val startDistance = distanceBetween2Points(startMovement)
            val endDistance = distanceBetween2Points(newPosition)

            val minCheckFirstFinger = angleBetween2Lines(minThresholdMovement, newPosition)
            val maxCheckFirstFinger = angleBetween2Lines(maxThresholdMovement, )

            if(startDistance <= endDistance){
                Log.d("CustomImageView2", "doesContinueCurrentGesture scaleUp test ${minCheck} ${maxCheck}")
                if((minCheck in 0f..180f) && (maxCheck in 180f..360f)){
                    //scale in boundaries
                    Log.d("CustomImageView2", "doesContinueCurrentGesture scale continue checked")
                    return true
                }
            }
            else{
                Log.d("CustomImageView2", "doesContinueCurrentGesture scaleDown test ${minCheck} ${maxCheck}")
            }
            */




        }
		Log.d("CustomImageView2", "doesContinueCurrentGesture failed")
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