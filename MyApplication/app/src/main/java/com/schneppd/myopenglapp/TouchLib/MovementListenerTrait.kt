package com.schneppd.myopenglapp.TouchLib

import android.graphics.Point
import android.util.SparseArray
import java.util.LinkedHashMap

/**
 * Created by david.schnepp on 21/07/2017.
 */
interface MovementListenerTrait : TouchListenerTrait {
    var movementCache:SparseArray<LinkedHashMap<Long, Point>>

    override fun onInitTrait(){
        super.onInitTrait()
        movementCache = SparseArray<LinkedHashMap<Long, Point>>(MAX_NUMBER_FINGER_PROCESSED)
        for(i in 0..(MAX_NUMBER_FINGER_PROCESSED - 1)){
            movementCache.put(i, LinkedHashMap<Long, Point>())
        }
    }
}