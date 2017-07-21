package com.schneppd.myopenglapp.TouchLib

import android.view.MotionEvent
import android.view.View

/**
 * Created by david.schnepp on 21/07/2017.
 */
interface TouchListenerTrait : View.OnTouchListener {
    val MAX_NUMBER_FINGER_PROCESSED
        get() = 2

    var touchStart:Long?
    var numberFinger:Int

    fun onInitTrait(){
        touchStart = null
        numberFinger = 0
    }

    override fun onTouch(p0: View?, p1: MotionEvent): Boolean {
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

    fun cancelTouchGesture(p1: MotionEvent){

    }
    fun endTouchGesture(p1: MotionEvent){

    }
    fun startTouchGesture(p1: MotionEvent){

    }

    fun getPointerId(event:MotionEvent):Int{
        val actionIndex = event.actionIndex
        val idPointer = event.getPointerId(actionIndex)
        return idPointer
    }

    //to define in higher level interfaces
    fun moveTouchPointer(p1: MotionEvent)
    fun addTouchPointerToGesture(p1: MotionEvent)
    fun removeTouchPointerToGesture(p1: MotionEvent)
    fun unknownTouchGestureStep(p1: MotionEvent)



}