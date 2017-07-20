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
import java.util.*
import kotlin.collections.HashMap


/**
 * Created by david.schnepp on 19/07/2017.
 */
enum class TouchEventComposition{
    UNDECIDED, ONE_FINGER, TWO_FINGER, MULTIPLE_FINGER
}

data class MovementStep(val timeCreation:Long, val x:Int, val y:Int)
data class MovementMatrix(val matrix:Array<MovementStep>)

class CustomImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs), View.OnTouchListener {

    var touchStart:Date? = null
    var numberTouchFinger = 0
    var touchComposition:TouchEventComposition = TouchEventComposition.UNDECIDED

    var singleFingerMovementStart:Date? = null
    var twoFingerMovementStart:Date? = null

    var gestureDragStart:Date? = null
    var gestureRotationStart:Date? = null
    var gestureScaleStart:Date? = null

    // touchInex, (time_creation, coords)
    //manage only the last 9 movements of each 3 fingers
    val movementsCache = SparseArray<LinkedHashMap<Long, Point>>(3)

    init {
        this.setOnTouchListener(this)
        //init matrix
        for(i in 0..2){
            movementsCache.put(i, LinkedHashMap<Long, Point>())
        }
    }

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
        val b = p1.action
        return true
    }

    fun addTouchPointerToGesture(event:MotionEvent){
        val actionIndex = event.actionIndex
        val idPointer = event.getPointerId(actionIndex)
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
        resolveGesture()
    }

    fun startTouchGesture(event:MotionEvent){
        touchStart = Date()
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

    fun resetTouchMatrix(){
        for(i in 0..2){
            movementsCache[i].clear()
        }
    }

    fun stopTouchGesture(event:MotionEvent){
        touchStart = null
        numberTouchFinger = 0
        Log.d("TouchTest", "stopTouchGesture *********")
    }

    fun addTouchPointer(event:MotionEvent){
        numberTouchFinger += 1
        createTouchData(event)
        resolveTouchComposition()
    }

    fun resolveTouchComposition(){
        if(numberTouchFinger == 1 && (touchComposition == TouchEventComposition.UNDECIDED || touchComposition == TouchEventComposition.TWO_FINGER)){
            val now = Date()
            val delayValidateSingleTouchEvent = Date(touchStart!!.time + 20)
            if(now > delayValidateSingleTouchEvent)
                touchComposition = TouchEventComposition.ONE_FINGER
        }

        if(numberTouchFinger == 2)
            touchComposition = TouchEventComposition.TWO_FINGER
        if(numberTouchFinger > 2)
            touchComposition = TouchEventComposition.MULTIPLE_FINGER
    }

    fun removeTouchPointer(event:MotionEvent){
        if(numberTouchFinger > 1)
            numberTouchFinger -= 1
        deleteTouchData(event)
        resolveTouchComposition()
    }

    fun deleteTouchData(event:MotionEvent){
        val actionIndex = event.actionIndex
        val idPointer = event.getPointerId(actionIndex)
        movementsCache[idPointer].clear()
    }

    fun createTouchData(event:MotionEvent){
        val actionIndex = event.actionIndex
        val idPointer = event.getPointerId(actionIndex)
        movementsCache[idPointer].clear()
    }

    fun rememberTouchMovementStep(event:MotionEvent){
        val pointerCount = event.pointerCount
        val time = event.eventTime
        for(p in 0..(pointerCount-1)){
            val pointerId = event.getPointerId(p)
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

    fun resolveGesture(){
        Log.d("TouchTest", "resolveGesture *********")
    }


    fun onDragStart(){

    }

    fun onDragStop(){

    }

    fun onDrag(){

    }

    fun onRotateStart(){

    }

    fun onRotateStop(){

    }

    fun onRotate(){

    }

    fun onScaleStart(){

    }

    fun onScaleStop(){

    }

    fun onScale(){

    }

}