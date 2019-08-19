package com.colorgreen.swiper

interface SwipeActionListener {

    fun onDragStart(value: Float, totalFriction: Float)

    fun onDrag(value: Float, totalFriction: Float)

    fun onDragEnd(value: Float, totalFriction: Float)
}
