package com.colorgreen.swiper

import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

import java.util.ArrayList

class OnSwipeTouchListener : OnTouchListener {

    private val actions = ArrayList<SwipeAction>()

    fun attachToView(v: View) {
        v.setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if(actions.isEmpty()) return false

        for (action in actions) {
            if (action.isBlocked) continue

            action.onTouch(event)
        }

        return true
    }

    fun addAction(action: SwipeAction) {
        actions.add(action)
    }
}