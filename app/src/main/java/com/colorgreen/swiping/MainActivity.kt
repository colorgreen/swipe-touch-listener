package com.colorgreen.swiping

import android.graphics.Color
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.colorgreen.swiper.OnSwipeTouchListener
import com.colorgreen.swiper.SwipeAction
import com.colorgreen.swiper.SwipeActionListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listener = OnSwipeTouchListener()

        bar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                bar.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val targetHeight = main_layout.height
                // for drawing layouts over buttons
                bar.z = 10f
                bottombar.y = main_layout.height.toFloat()
                bottombar.z = 10f

                val swipeAction = SwipeAction(
                    SwipeAction.DragDirection.Down,
                    floatArrayOf(0f, targetHeight * 0.3f, targetHeight.toFloat()),
                    0.4f
                )
                val bottomSwipeAction = SwipeAction(
                    SwipeAction.DragDirection.Up,
                    floatArrayOf(targetHeight.toFloat(), targetHeight - targetHeight * 0.7f, 0f),
                    0.2f
                )

                bottomSwipeAction.setSwipeActionListener(object : SwipeActionListener {
                    override fun onDragStart(value: Float, totalFriction: Float) {}

                    override fun onDrag(value: Float, totalFriction: Float) {
                        bottombar.y = value
                    }

                    override fun onDragEnd(value: Float, totalFriction: Float) {
                        swipeAction.isBlocked = bottomSwipeAction.isExtended()
                    }
                })

                swipeAction.setSwipeActionListener(object : SwipeActionListener {
                    override fun onDragStart(value: Float, totalFriction: Float) {}

                    override fun onDrag(value: Float, totalFriction: Float) {
                        bar!!.layoutParams = RelativeLayout.LayoutParams(bar!!.width, value.toInt())
                        bar!!.setBackgroundColor(
                            interpolateColor(
                                ContextCompat.getColor(this@MainActivity, R.color.lightblue),
                                ContextCompat.getColor(this@MainActivity, R.color.darkblue),
                                value / targetHeight
                            )
                        )
                    }

                    override fun onDragEnd(value: Float, totalFriction: Float) {
                        bottomSwipeAction.isBlocked = swipeAction.isExtended()
                    }
                })

                listener.addAction(swipeAction)
                listener.addAction(bottomSwipeAction)

                expand_button.setOnClickListener {
                    if (!swipeAction.isBlocked)
                        swipeAction.expand()
                }

                collapse_button.setOnClickListener {
                    if (!swipeAction.isBlocked)
                        swipeAction.collapse()
                }
            }
        })

        listener.attachToView(main_layout)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun interpolate(a: Float, b: Float, proportion: Float): Float {
        return a + (b - a) * proportion
    }

    /**
     * Returns an interpoloated color, between `a` and `b`
     */
    private fun interpolateColor(a: Int, b: Int, proportion: Float): Int {
        val hsva = FloatArray(3)
        val hsvb = FloatArray(3)
        Color.colorToHSV(a, hsva)
        Color.colorToHSV(b, hsvb)
        for (i in 0..2) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion)
        }
        return Color.HSVToColor(hsvb)
    }
}
