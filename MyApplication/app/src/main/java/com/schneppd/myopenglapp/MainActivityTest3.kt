package com.schneppd.myopenglapp

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import it.sephiroth.android.library.uigestures.UIGestureRecognizer
import it.sephiroth.android.library.uigestures.UIGestureRecognizerDelegate
import it.sephiroth.android.library.uigestures.UITapGestureRecognizer
import it.sephiroth.android.library.uigestures.UIPinchGestureRecognizer
import it.sephiroth.android.library.uigestures.UIRotateGestureRecognizer
import kotlinx.android.synthetic.main.content_main.*

/**
 * Created by david.schnepp on 17/07/2017.
 */
class MainActivityTest3 : MainActivityBase(), View.OnTouchListener, UIGestureRecognizer.OnActionListener, UIGestureRecognizerDelegate.Callback {
    lateinit var uIGestureRecognizerDelegate:UIGestureRecognizerDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uIGestureRecognizerDelegate = UIGestureRecognizerDelegate(null)
        uIGestureRecognizerDelegate.setCallback(this)

        val recognizer1 = UITapGestureRecognizer(this)
        recognizer1.setNumberOfTapsRequired(1)
        recognizer1.setNumberOfTouchesRequired(1)
        recognizer1.tag = "single-tap"
        recognizer1.setActionListener(this)

        val recognizer2 = UITapGestureRecognizer(this)
        recognizer2.setNumberOfTapsRequired(2)
        recognizer2.setNumberOfTouchesRequired(1)
        recognizer2.tag = "double-tap"
        recognizer2.setActionListener(this)

        val recognizer3 = UIPinchGestureRecognizer(this)
        recognizer3.tag = "UIPinchGestureRecognizer"
        recognizer3.setActionListener(this)
        val recognizer4 = UIRotateGestureRecognizer(this)
        recognizer4.tag = "UIRotateGestureRecognizer"
        recognizer4.setActionListener(this)

        recognizer1.requireFailureOf(recognizer2)
        //recognizer3.requireFailureOf(recognizer4)
        //recognizer4.requireFailureOf(recognizer3)

        uIGestureRecognizerDelegate.addGestureRecognizer(recognizer1)
        uIGestureRecognizerDelegate.addGestureRecognizer(recognizer2)
        uIGestureRecognizerDelegate.addGestureRecognizer(recognizer3)
        uIGestureRecognizerDelegate.addGestureRecognizer(recognizer4)

        ivUserPicture.setOnTouchListener { view, motionEvent -> onTouch(view, motionEvent) }
    }

    override fun onTouch(view:View, motionEvent:MotionEvent) : Boolean{
        return uIGestureRecognizerDelegate.onTouchEvent(view, motionEvent)
    }

    override fun onGestureRecognized(recognizer:UIGestureRecognizer ){
        Log.d("test gesture", "onGestureRecognized(" + recognizer + "). state: " + recognizer.state)
    }

    override fun shouldBegin(recognizer:UIGestureRecognizer ) : Boolean{
        return true
    }

    override fun shouldRecognizeSimultaneouslyWithGestureRecognizer(currentRecognizer:UIGestureRecognizer, recognizer:UIGestureRecognizer) : Boolean{
        return true
    }
    override fun shouldReceiveTouch(recognizer:UIGestureRecognizer ) : Boolean{
        return true
    }

}