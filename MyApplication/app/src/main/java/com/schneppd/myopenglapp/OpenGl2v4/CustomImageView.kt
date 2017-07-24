package com.schneppd.myopenglapp.OpenGl2v4

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import java.util.*


/**
 * Created by david.schnepp on 19/07/2017.
 */

class CustomImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs), View.OnTouchListener {
    enum class TouchEventComposition{
        UNDECIDED, ONE_FINGER, TWO_FINGER, MULTIPLE_FINGER
    }

    enum class FingerMovement{
        NOT_ANALYSED ,STATIC, STATIC_WIGLE, MOVEMENT
    }

    val MAX_TOUCH_FINGER = 2
    val GESTURE_NOISE_POSITION_LIMIT = 10 // pixel
    val GESTURE_NOISE_DELAY_LIMIT = 300 // millisec

    var touchStart:Long? = null
    var numberTouchFinger = 0
    var touchComposition:TouchEventComposition = TouchEventComposition.UNDECIDED

    
    var gestureOneFingerDragStartIndex = -1
    var gestureOneFingerDragStart:Long? = null
    var gestureTwoFingerRotationStartIndex = -1
    var gestureTwoFingerRotationStart:Long? = null
    var gestureTwoFingerScaleStartIndex = -1
    var gestureTwoFingerScaleStart:Long? = null

    var lastSingleFingerGestureStop:Long? = null
    var dragGestureCooldown:Long = 0


    // touchInex, (time_creation, coords)
    //manage only the last 9 movements of each 2 fingers
    //val movementsCache = SparseArray<LinkedHashMap<Long, Point>>(MAX_TOUCH_FINGER)
    val movementsCache = Array(3 , {i -> Array(9, {j -> longArrayOf(0L, 0L, 0L)})})
    var movementsCacheIndex = 0
    var startGestureInformations = Array(3 , {i -> longArrayOf(0L, 0L, 0L)})

    init {
        this.setOnTouchListener(this)
        //init finger position matrix
        /*
        for(i in 0..(MAX_TOUCH_FINGER - 1)){
            movementsCache.put(i, LinkedHashMap<Long, Point>())
        }
        */
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
            else -> unknownTouchGestureStep(p1)
        }
        return true
    }

    ///process start / end
    fun startTouchGesture(event:MotionEvent){
        touchStart = Date().time
        addTouchPointer(event)
        Log.d("TouchTest", "startTouchGesture *********")
    }

    fun cancelTouchGesture(event:MotionEvent){
        Log.d("TouchTest", "cancelTouchGesture *********")
        endTouchGesture(event)
    }

    fun endTouchGesture(event:MotionEvent){
        touchStart = null
        numberTouchFinger = 0
        touchComposition = TouchEventComposition.UNDECIDED
        dragGestureCooldown = 0
        lastSingleFingerGestureStop = null
        //clear gestures

        if(gestureOneFingerDragStartIndex > -1) onDragStop(event)

        movementsCacheIndex = 0
        Log.d("TouchTest", "endTouchGesture *********")
    }

    ///process steps in touch lifecycle
    fun addTouchPointerToGesture(event:MotionEvent){
        addTouchPointer(event)
        Log.d("TouchTest", "addTouchPointerToGesture +++++++++")
    }

    fun removeTouchPointerToGesture(event:MotionEvent){
        removeTouchPointer(event)
        Log.d("TouchTest", "removeTouchPointerToGesture +++++++++")
    }

    fun unknownTouchGestureStep(event:MotionEvent){
        Log.d("TouchTest", "unknownTouchGestureStep +++++++++")
    }

    fun moveTouchPointer(event:MotionEvent){
        //try to resolve
        resolveTouchComposition()
        Log.d("TouchTest", "moveTouchPointer ---------- ${touchComposition}")
        Log.d("TouchTest", "pointerHistory: ----------")

        val historySize = event.historySize
        val pointerCount = event.pointerCount

        for(h in 0..(historySize-1)){
            val oldTime = event.getHistoricalEventTime(h)
            for(p in 0..(pointerCount-1)){
                val pointerId = event.getPointerId(p)
                val pointerX = event.getHistoricalX(p, h)
                val pointerY = event.getHistoricalY(p, h)
                Log.d("TouchTest", "t:${oldTime} pointer:${pointerId} x:${pointerX} y:${pointerY}")
            }
        }

        val time = event.eventTime
        for(p in 0..(pointerCount-1)){
            val pointerId = event.getPointerId(p)
            val pointerX = event.getX(p)
            val pointerY = event.getY(p)
            Log.d("TouchTest", "t:${time} pointer:${pointerId} x:${pointerX} y:${pointerY}")
            rememberTouchMovementStep(event)
        }
        Log.d("TouchTest", "end pointerHistory: ----------")
        resolveGesture(event)
    }



    ///utlity functions

    fun addTouchPointer(event:MotionEvent){
        numberTouchFinger += 1

        resolveTouchComposition()
        Log.d("TouchTest", "addTouchPointer t:${event.eventTime}")
    }
    fun removeTouchPointer(event:MotionEvent){
        if(numberTouchFinger == 2)//going from two finger gesture to one finger gesture
            dragGestureCooldown = event.downTime + GESTURE_NOISE_DELAY_LIMIT
        if(numberTouchFinger > 1)
            numberTouchFinger -= 1

        resolveTouchComposition()
        Log.d("TouchTest", "removeTouchPointer t:${event.eventTime}")
    }

    //try to get number of finger used in touchGesture
    fun resolveTouchComposition(){
        if(numberTouchFinger == 1 && (touchComposition == TouchEventComposition.UNDECIDED || touchComposition == TouchEventComposition.TWO_FINGER)){
            val now = Date()
            val delayValidateSingleTouchEvent = Date(touchStart!! + 20)
            if(now > delayValidateSingleTouchEvent)
                touchComposition = TouchEventComposition.ONE_FINGER
        }

        if(numberTouchFinger == 2)
            touchComposition = TouchEventComposition.TWO_FINGER
        if(numberTouchFinger > 2)
            touchComposition = TouchEventComposition.MULTIPLE_FINGER
    }



    fun getPointerId(event:MotionEvent):Int{
        val actionIndex = event.actionIndex
        val idPointer = event.getPointerId(actionIndex)
        return idPointer
    }
    



    fun rememberTouchMovementStep(event:MotionEvent){
        val pointerCount = event.pointerCount
        val time = event.eventTime
        var hasAddedPointer = false
        for(p in 0..(pointerCount-1)){
            val pointerId = event.getPointerId(p)
            if(pointerId < MAX_TOUCH_FINGER){
                val pointerX = event.getX(p).toLong()
                val pointerY = event.getY(p).toLong()
                Log.d("TouchTest", "t:${time} pointer:${pointerId} x:${pointerX} y:${pointerY}")
                val matrix = movementsCache[pointerId]

                if(movementsCacheIndex == 0){
                    //save first element
                    matrix[movementsCacheIndex][0] = time
                    matrix[movementsCacheIndex][1] = pointerX
                    matrix[movementsCacheIndex][2] = pointerY
                    hasAddedPointer = true
                }
                else{
                    if(movementsCacheIndex == 9)
                        return //should not occur

                    val lastPointX = matrix[movementsCacheIndex][1]
                    val lastPointY = matrix[movementsCacheIndex][2]
                    //test if noiseInput
                    var canRecord = false
                    if((pointerX > lastPointX + GESTURE_NOISE_POSITION_LIMIT) || (pointerY > lastPointY + GESTURE_NOISE_POSITION_LIMIT))
                        canRecord = true
                    if(!canRecord && (pointerX < lastPointX - GESTURE_NOISE_POSITION_LIMIT) || (pointerY < lastPointY - GESTURE_NOISE_POSITION_LIMIT))
                        canRecord = true

                    if(canRecord){
                        matrix[movementsCacheIndex][0] = time
                        matrix[movementsCacheIndex][1] = pointerX
                        matrix[movementsCacheIndex][2] = pointerY
                        hasAddedPointer = true
                    }
                }
            }


        }
        if(hasAddedPointer)
            movementsCacheIndex++
    }



    ///identify gesture
    fun resolveGesture(event:MotionEvent){
        Log.d("TouchTest", "resolveGesture *********")
        val timeEvent = event.eventTime
        if(isDragging(event)) {
            endOngoingTwoFingerGestures(event)
            if(gestureOneFingerDragStartIndex == -1)
                onDragStart(event)
            else
                onDrag(event)
        }
        else if(touchComposition == TouchEventComposition.TWO_FINGER || touchComposition == TouchEventComposition.MULTIPLE_FINGER){
            endOngoingOneFingerGestures(event)
            if(isRotating(event)){
                gestureTwoFingerScaleStart?.let{
                    onScale(event)
                }
                if(gestureTwoFingerRotationStartIndex == -1)
                    onRotateStart(event)
                else
                    onRotate(event)
            }
            else if(isScalling(event)){
                gestureTwoFingerRotationStart?.let{
                    onRotateStop(event)
                }
                if(gestureTwoFingerScaleStartIndex == -1)
                    onScaleStart(event)
                else
                    onScale(event)
            }
            else{
                Log.d("TouchTest", "no gesture detected")
            }

        }
    }

    fun endOngoingOneFingerGestures(event:MotionEvent){
        gestureOneFingerDragStart?.let{
            onDragStop(event)
        }
    }
    fun endOngoingTwoFingerGestures(event:MotionEvent){
        gestureTwoFingerRotationStart?.let{
            onRotateStop(event)
        }
        gestureTwoFingerScaleStart?.let{
            onScale(event)
        }
    }

    fun isDragging(event:MotionEvent):Boolean{
        val timeEvent = event.eventTime
        if(touchComposition == TouchEventComposition.ONE_FINGER && timeEvent > dragGestureCooldown)
            return true
        return false
    }

    fun isRotating(event:MotionEvent):Boolean{
        if(movementsCacheIndex == 4){//need 3 moves to tell if rotation
            var lastGestureInformations = Array(2 , {i -> Array((movementsCacheIndex - 1), { j -> FingerMovement.NOT_ANALYSED})})
            var lastElement:LongArray? = null
            val fingerSize = event.size
            val limitWigle = 30L
            val movementsCacheIndexStart = movementsCacheIndex - 1
            val movementsCacheIndexCountStart = movementsCacheIndexStart - 1
            for (finger in 1 downTo 0) {
                val initialX = movementsCache[finger][movementsCacheIndexStart][1]
                val initialY = movementsCache[finger][movementsCacheIndexStart][2]

                for (indexMovement in movementsCacheIndexCountStart downTo 0) {
                    if (indexMovement < movementsCacheIndex) {

                        val previousX = movementsCache[finger][indexMovement + 1][1]
                        val previousY = movementsCache[finger][indexMovement + 1][2]

                        val currentX = movementsCache[finger][indexMovement][1]
                        val currentY = movementsCache[finger][indexMovement][2]

                        if (previousX == currentX && previousY == currentY) {
                            lastGestureInformations[finger][indexMovement] = FingerMovement.STATIC
                        } else if (
                        ((previousX >= currentX && currentX > (initialX - limitWigle)) || (previousX < currentX && currentX < (initialX + limitWigle)))
                                && ((previousY >= currentY && currentY > (initialY - limitWigle)) || (previousY < currentY && currentY < (initialY + limitWigle)))
                                       ) {
                            lastGestureInformations[finger][indexMovement] = FingerMovement.STATIC_WIGLE
                        } else {
                            lastGestureInformations[finger][indexMovement] = FingerMovement.MOVEMENT
                        }

                    }

                }
            }
            //resolve each finger action
            //-1 unknown, 1 static all the way, 2 movement all the way
            val fingersFinalAction = arrayOf(-1, -1)

            val startMovementTestIndex = movementsCacheIndexCountStart - 1
            for (finger in 1 downTo 0) {
                var fingerActionStart = lastGestureInformations[finger][movementsCacheIndexCountStart]
                //val previousAction = lastGestureInformations[finger][movementsCacheIndexCountStart]

                var isMovementDetected = false
                var isStaticDetected = false
                var isMovementConstant = false
                var isStaticConstant = false

                for(indexMovement in startMovementTestIndex downTo 0) {
                    val newAction = lastGestureInformations[finger][indexMovement]

                    if(indexMovement == startMovementTestIndex){
                        //first test
                        if((fingerActionStart == FingerMovement.STATIC || fingerActionStart == FingerMovement.STATIC_WIGLE)
                                && (newAction == FingerMovement.STATIC || newAction == FingerMovement.STATIC_WIGLE)){
                            isStaticDetected = true
                            isStaticConstant = true
                        }
                        else if(fingerActionStart == FingerMovement.MOVEMENT && newAction == FingerMovement.MOVEMENT){
                            isMovementDetected = true
                            isMovementConstant = true
                        }
                    }
                    else{
                        if((fingerActionStart == FingerMovement.STATIC || fingerActionStart == FingerMovement.STATIC_WIGLE)
                                && (newAction == FingerMovement.STATIC || newAction == FingerMovement.STATIC_WIGLE)){
                            isStaticDetected = true
                            if(isMovementConstant)
                                isMovementConstant = false

                        }
                        else if(fingerActionStart == FingerMovement.MOVEMENT && newAction == FingerMovement.MOVEMENT){
                            isMovementDetected = true
                            if(isStaticConstant)
                                isStaticConstant = false
                        }
                    }


                }

                if(isMovementDetected && isMovementConstant){
                    fingersFinalAction[finger] = 2
                }
                else if(isStaticDetected && isStaticConstant){
                    fingersFinalAction[finger] = 1
                }
                val b = 12

            }

        }

        return false
    }
    fun isScalling(event:MotionEvent):Boolean{
        if(movementsCacheIndex == 4){//need 3 moves to tell if rotation
            var lastGestureInformations = Array(2 , {i -> Array((movementsCacheIndex - 2), { j -> FingerMovement.NOT_ANALYSED})})
            var lastElement:LongArray? = null
            val fingerSize = event.size
            val limitWigle = 30L
            val movementsCacheIndexStart = movementsCacheIndex - 1
            val movementsCacheIndexCountStart = movementsCacheIndexStart - 1
            for (finger in 1 downTo 0) {
                val initialX = movementsCache[finger][movementsCacheIndexStart][1]
                val initialY = movementsCache[finger][movementsCacheIndexStart][2]

                for (indexMovement in movementsCacheIndexCountStart downTo 0) {
                    if (indexMovement < movementsCacheIndex) {

                        val previousX = movementsCache[finger][indexMovement + 1][1]
                        val previousY = movementsCache[finger][indexMovement + 1][2]

                        val currentX = movementsCache[finger][indexMovement][1]
                        val currentY = movementsCache[finger][indexMovement][2]

                        if (previousX == currentX && previousY == currentY) {
                            lastGestureInformations[finger][indexMovement] = FingerMovement.STATIC
                        } else if (
                        ((previousX >= currentX && currentX > (initialX - limitWigle)) || (previousX < currentX && currentX < (initialX + limitWigle)))
                                && ((previousY >= currentY && currentY > (initialY - limitWigle)) || (previousY < currentY && currentY < (initialY + limitWigle)))
                                       ) {
                            lastGestureInformations[finger][indexMovement] = FingerMovement.STATIC_WIGLE
                        } else {
                            lastGestureInformations[finger][indexMovement] = FingerMovement.MOVEMENT
                        }

                    }

                }
            }
            //resolve each finger action
            //-1 unknown, 1 static all the way, 2 movement all the way
            val fingersFinalAction = arrayOf(-1, -1)

            for (finger in 1 downTo 0) {
                var fingerActionStart = lastGestureInformations[finger][movementsCacheIndexStart]
                val previousAction = lastGestureInformations[finger][movementsCacheIndexStart]

                var isMovementDetected = false
                var isStaticDetected = false
                var isMovementConstant = false
                var isStaticConstant = false

                for(indexMovement in movementsCacheIndexCountStart downTo 0) {
                    val newAction = lastGestureInformations[finger][indexMovement]

                    if(indexMovement == movementsCacheIndexCountStart){
                        //first test
                        if((fingerActionStart == FingerMovement.STATIC || fingerActionStart == FingerMovement.STATIC_WIGLE)
                                && (newAction == FingerMovement.STATIC || newAction == FingerMovement.STATIC_WIGLE)){
                            isStaticDetected = true
                            isStaticConstant = true
                        }
                        else if(fingerActionStart == FingerMovement.MOVEMENT && newAction == FingerMovement.MOVEMENT){
                            isMovementDetected = true
                            isMovementConstant = true
                        }
                    }
                    else{
                        if((fingerActionStart == FingerMovement.STATIC || fingerActionStart == FingerMovement.STATIC_WIGLE)
                                && (newAction == FingerMovement.STATIC || newAction == FingerMovement.STATIC_WIGLE)){
                            isStaticDetected = true
                            if(isMovementConstant)
                                isMovementConstant = false

                        }
                        else if(fingerActionStart == FingerMovement.MOVEMENT && newAction == FingerMovement.MOVEMENT){
                            isMovementDetected = true
                            if(isStaticConstant)
                                isStaticConstant = false
                        }
                    }


                }

                if(isMovementDetected && isMovementConstant){
                    fingersFinalAction[finger] = 2
                }
                else if(isStaticDetected && isStaticConstant){
                    fingersFinalAction[finger] = 1
                }

            }

        }
        return false
    }

    fun onDragStart(event:MotionEvent){
        val time = event.eventTime
        gestureOneFingerDragStart = time // record start of drag


        val startPointX = event.getX(0).toLong()
        val startPointY = event.getY(0).toLong()

        gestureOneFingerDragStartIndex = movementsCacheIndex--
        movementsCacheIndex = 0

        startGestureInformations[0][0] = movementsCache[0][movementsCacheIndex][0]
        startGestureInformations[0][1] = movementsCache[0][movementsCacheIndex][1]
        startGestureInformations[0][2] = movementsCache[0][movementsCacheIndex][2]
        Log.d("TouchTest", "onDragStart t:${time} x:${startPointX} y:${startPointY}")
    }

    fun onDragStop(event:MotionEvent){
        gestureOneFingerDragStartIndex = -1
        gestureOneFingerDragStart = null
        lastSingleFingerGestureStop = event.eventTime

        Log.d("TouchTest", "onDragStop t:${event.eventTime}")
    }

    fun onDrag(event:MotionEvent){
        movementsCacheIndex = 0
        Log.d("TouchTest", "onDrag t:${event.eventTime} x:${event.getX(0)} y:${event.getY(0)}")
    }



    fun onRotateStart(event:MotionEvent){

    }

    fun onRotateStop(event:MotionEvent){

    }

    fun onRotate(event:MotionEvent){
    }




    fun onScaleStart(event:MotionEvent){

    }

    fun onScaleStop(event:MotionEvent){
        
    }

    fun onScale(event:MotionEvent){

    }

}