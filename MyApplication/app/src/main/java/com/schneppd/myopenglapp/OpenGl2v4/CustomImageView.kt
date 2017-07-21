package com.schneppd.myopenglapp.OpenGl2v4

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import collections.forEach
import java.util.*
import kotlin.collections.HashMap


/**
 * Created by david.schnepp on 19/07/2017.
 */

class CustomImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs), View.OnTouchListener {
    enum class TouchEventComposition{
        UNDECIDED, ONE_FINGER, TWO_FINGER, MULTIPLE_FINGER
    }

    val MAX_TOUCH_FINGER = 2

    var touchStart:Long? = null
    var numberTouchFinger = 0
    var touchComposition:TouchEventComposition = TouchEventComposition.UNDECIDED

    var startGestureInformations:SparseArray<LinkedHashMap<Long, Point>>? = null
    var gestureOneFingerDragStart:Long? = null
    var gestureTwoFingerRotationStart:Long? = null
    var gestureTwoFingerScaleStart:Long? = null

    var lastSingleFingerGestureStop:Long? = null
    var lastTwhoFingerGestureStop:Long? = null


    // touchInex, (time_creation, coords)
    //manage only the last 9 movements of each 2 fingers
    val movementsCache = SparseArray<LinkedHashMap<Long, Point>>(MAX_TOUCH_FINGER)

    init {
        this.setOnTouchListener(this)
        //init matrix
        for(i in 0..(MAX_TOUCH_FINGER - 1)){
            movementsCache.put(i, LinkedHashMap<Long, Point>())
        }
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
        touchStart = null
        numberTouchFinger = 0
        touchComposition = TouchEventComposition.UNDECIDED
        resetTouchMatrix()

        Log.d("TouchTest", "cancelTouchGesture *********")
    }

    fun endTouchGesture(event:MotionEvent){
        touchStart = null
        numberTouchFinger = 0
        resetTouchMatrix()
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
    //
    fun resetTouchMatrix(){
        for(i in 0..(MAX_TOUCH_FINGER - 1)){
            movementsCache[i].clear()
        }
    }
    fun addTouchPointer(event:MotionEvent){
        numberTouchFinger += 1
        createTouchData(event)

        resolveTouchComposition()
    }
    fun removeTouchPointer(event:MotionEvent){
        if(numberTouchFinger > 1)
            numberTouchFinger -= 1
        deleteTouchData(event)

        resolveTouchComposition()
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

    fun deleteTouchData(event:MotionEvent){
        val idPointer = getPointerId(event)
        if(idPointer < MAX_TOUCH_FINGER)
            movementsCache[idPointer].clear()
    }

    fun createTouchData(event:MotionEvent){
        val idPointer = getPointerId(event)
        if(idPointer < MAX_TOUCH_FINGER)
            movementsCache[idPointer].clear()
    }

    fun rememberTouchMovementStep(event:MotionEvent){
        val pointerCount = event.pointerCount
        val time = event.eventTime
        for(p in 0..(pointerCount-1)){
            val pointerId = event.getPointerId(p)
            if(pointerId < MAX_TOUCH_FINGER){
                val pointerX = event.getX(p).toInt()
                val pointerY = event.getY(p).toInt()
                Log.d("TouchTest", "t:${time} pointer:${pointerId} x:${pointerX} y:${pointerY}")
                val point = Point(pointerX, pointerY)
                val matrix = movementsCache[pointerId]

                if(matrix.size == 0){
                    //save first element
                    matrix.put(time, point)
                }
                else{
                    val lastPoint = matrix.values.last()
                    //test if noiseInput
                    val limitNoise = 10
                    var canRecord = false
                    if((pointerX > lastPoint.x + limitNoise) || (pointerY > lastPoint.y + limitNoise))
                        canRecord = true
                    if(!canRecord && (pointerX < lastPoint.x - limitNoise) || (pointerY < lastPoint.y - limitNoise))
                        canRecord = true

                    if(canRecord)
                        matrix.put(time, point)
                }
            }


        }
    }



    ///identify gesture
    fun resolveGesture(event:MotionEvent){
        Log.d("TouchTest", "resolveGesture *********")

        if(touchComposition == TouchEventComposition.ONE_FINGER){
            //drag only option
            gestureOneFingerDragStart?.let{
                onDrag(event)
            } ?:run{
                gestureOneFingerDragStart = Date().time // record start of drag
                onDrag(event)
            }
        }
        else if(touchComposition == TouchEventComposition.TWO_FINGER || touchComposition == TouchEventComposition.MULTIPLE_FINGER){
            gestureOneFingerDragStart?.let{
                onDragStop(event)
            }
        }
    }

    fun clearCacheUpTo(time:Long){
        movementsCache.forEach { fingerIndex, cacheFingerMovement -> run{
            for(key in cacheFingerMovement.keys){
                if(key <= time) cacheFingerMovement.remove(key)
            }
        } }

    }

    fun onDragStart(event:MotionEvent){
        val time = event.eventTime
        val startPoint = Point(event.getX(0).toInt(), event.getY(0).toInt())
        val startInfos = LinkedHashMap<Long, Point>()
        startInfos.put(time, startPoint)

        val recordedInfos = SparseArray<LinkedHashMap<Long, Point>>(1)
        recordedInfos.put(0, startInfos)

        clearCacheUpTo(time)

        startGestureInformations = recordedInfos
        Log.d("TouchTest", "onDragStart t:${time} x:${startPoint.x} y:${startPoint.y}")
    }

    fun onDragStop(event:MotionEvent){
        startGestureInformations = null
        gestureOneFingerDragStart = null
        lastSingleFingerGestureStop = Date().time

        Log.d("TouchTest", "onDragStop t:${event.eventTime}")
    }

    fun onDrag(event:MotionEvent){
        Log.d("TouchTest", "onDrag t:${event.eventTime} x:${event.getX(0)} y:${event.getY(0)}")
    }



    fun onRotateStart(){

    }

    fun onRotateStop(){
        startGestureInformations = null
    }

    fun onRotate(){
    }




    fun onScaleStart(){

    }

    fun onScaleStop(){
        startGestureInformations = null
    }

    fun onScale(){

    }

}