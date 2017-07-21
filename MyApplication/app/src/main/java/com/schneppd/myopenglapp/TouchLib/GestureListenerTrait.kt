package com.schneppd.myopenglapp.TouchLib

/**
 * Created by david.schnepp on 21/07/2017.
 */
interface GestureListenerTrait: MovementListenerTrait {
    var gestureDragStart:Long?
    var gestureRotationStart:Long?
    var gestureScaleStart:Long?

    override fun onInitTrait(){
        super.onInitTrait()

        gestureDragStart = null
        gestureRotationStart = null
        gestureScaleStart = null
    }
}