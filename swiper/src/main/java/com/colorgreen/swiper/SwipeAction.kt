package com.colorgreen.swiper

import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder


private const val SLOW_FACTOR = 4.5f

/**
 * @constructor
 * @param _direction     default drag is DragDirection.Down
 * @param _steps
 * @param _dragThreshold default threshold is 0.5f
 */
class SwipeAction(
    _direction: DragDirection = DragDirection.Down,
    _steps: FloatArray,
    _dragThreshold: Float = 0.5f,
    _swipeActionListener: SwipeActionListener? = null
) {
    private var currentStep: Float = 0.toFloat()
    private var lastPosition: Float = 0.toFloat()
    private var startPosition: Float = 0.toFloat()

    private lateinit var velocityTracker: VelocityTracker
    private var flingAnimation: FlingAnimation? = null
    private var startEvent: MotionEvent? = null

    private var swipeActionListener: SwipeActionListener? = _swipeActionListener

    /**
     * Set threshold above which drag is executed. If drag is shorter than threshold, drag is no executed
     * and view is returned to start position. Else drag is executed and is animated to end position
     *
     * @param dragThreshold percent above which drag is executed
     */
    private var dragThreshold: Float = _dragThreshold
    var direction: DragDirection = _direction
        set(value) {
            field = value
            if (this.steps != null)
                checkSteps(this.steps)
        }
    var steps: FloatArray = _steps
        /**
         * Set values, which your drag respects. For direction Left and Up value have to be in descending
         * order. For direction Right and Down values have to be ascending.
         * Example for direction left: [ 0, -300, -600 ]
         * Watch out for using etc. view.getX() function in onCreate() of activity. It will propably return 0.
         * Use view.getViewTreeObserver() to obtain axis properties.
         *
         * @param steps Array in ascending or descending order, depends of direction.
         * @see [stackoverflow.com](https://stackoverflow.com/questions/3591784/views-getwidth-and-getheight-returns-0)
         */
        set(value) {
            checkSteps(value)
            field = value
            lastPosition = steps[step]
            currentStep = lastPosition
        }
    var step = 0
        private set
    var isDragging = false
        private set
    var isBlocked = false

    enum class DragDirection {
        Right, Up, Left, Down
    }

    fun setSwipeActionListener(newSwipeActionListener: SwipeActionListener){
        swipeActionListener = newSwipeActionListener
    }

    fun isExtended(): Boolean {
        return step > 0
    }

    fun onTouch(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN)
            velocityTracker = VelocityTracker.obtain()

        if (isDragging)
            velocityTracker.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> startDrag(event)
            MotionEvent.ACTION_MOVE -> onMove(startEvent, event)
            MotionEvent.ACTION_UP -> endDrag(startEvent, event)
        }

        return true
    }

    fun pushToStep(stepIndex: Int) {
        val nextStep = steps[stepIndex]
        val velocity = (steps[stepIndex] - lastPosition) * SLOW_FACTOR
        pushToStep(steps[stepIndex], velocity)
    }

    fun expand() {
        pushToStep(steps.size - 1)
    }

    fun collapse() {
        pushToStep(0)
    }

    ////////////////////////////////// PRIVATE FUNCTION ////////////////////////////////////////////
    private fun startDrag(event: MotionEvent) {
        if (isDragging) {
            flingAnimation?.run {
                if (this.isRunning) {
                    this.cancel()
                    startEvent = MotionEvent.obtain(event)
                }
            }
        } else {
            swipeActionListener?.onDragStart(currentStep, 0f)
            currentStep = steps[step]
            startEvent = MotionEvent.obtain(event)
            isDragging = true
        }
        startPosition = lastPosition
    }

    private fun onMove(e1: MotionEvent?, e2: MotionEvent) {
        val diff = getDiff(e1, e2) + startPosition
        val friction = Math.abs((lastPosition - steps[0]) / (steps[steps.size - 1] - steps[0]))

        var iOnDrag: Boolean

        when (direction) {
            DragDirection.Left, DragDirection.Up -> {
                iOnDrag = when (step) {
                    steps.size - 1 -> steps[step - 1] >= diff && diff >= steps[steps.size - 1]
                    0 -> steps[0] >= diff && diff >= steps[step + 1]
                    else -> steps[step - 1] >= diff && diff >= steps[step + 1]
                }
            }
            else -> {
                iOnDrag = when (step) {
                    steps.size - 1 -> steps[step - 1] <= diff && diff <= steps[steps.size - 1]
                    0 -> steps[0] <= diff && diff <= steps[step + 1]
                    else -> steps[step - 1] <= diff && diff <= steps[step + 1]
                }

            }
        }

        if (iOnDrag) {
            swipeActionListener?.onDrag(diff, friction)
            lastPosition = diff
        }
    }

    private fun getNextStep(position: Float, checkThreshold: Boolean = true): Float {
        val nextStep: Float

        if (direction == DragDirection.Left || direction == DragDirection.Up) {
            nextStep = if (step + 1 == steps.size) {
                if (position < steps[step])
                    steps[step]
                else
                    steps[step - 1]

            } else {
                if (position > steps[step])
                    steps[if (step != 0) step - 1 else step]
                else
                    steps[step + 1]

            }
        } else {
            nextStep = if (step + 1 == steps.size) {
                if (position > steps[step])
                    steps[step]
                else
                    steps[step - 1]

            } else {
                if (position < steps[step])
                    steps[if (step != 0) step - 1 else step]
                else
                    steps[step + 1]

            }
        }
        return if (checkThreshold && steps[step] != nextStep && Math.abs(currentStep - position) < Math.abs(nextStep - steps[step]) * dragThreshold) steps[step] else nextStep
    }

    private fun calculateVelocity(diff: Float): Float {
        velocityTracker.computeCurrentVelocity(1000)

        var velocity = velocityTracker.xVelocity
        if (direction == DragDirection.Up || direction == DragDirection.Down)
            velocity = velocityTracker.yVelocity

        val nextStep = getNextStep(diff, false)

        if (Math.abs(velocity) < Math.abs(nextStep - lastPosition) * SLOW_FACTOR) {
            velocity = (getNextStep(diff) - lastPosition) * SLOW_FACTOR
        }

        return velocity
    }

    private fun endDrag(e1: MotionEvent?, e2: MotionEvent) {
        val diff = getDiff(e1, e2)
        val velocity = calculateVelocity(lastPosition + diff / SLOW_FACTOR)
        val nextStep = getNextStep(lastPosition + velocity / SLOW_FACTOR)

        pushToStep(nextStep, velocity)
    }


    private fun pushToStep(nextStep: Float, velocity: Float) {

        val floatValueHolder = FloatValueHolder(lastPosition)
        flingAnimation = FlingAnimation(floatValueHolder)
            .setStartVelocity(velocity)
            .setStartValue(lastPosition)
            .addUpdateListener { _, value, _ ->
                lastPosition = value
                val friction = Math.abs((lastPosition - steps[0]) / (steps[steps.size - 1] - steps[0]))
                if (swipeActionListener != null)
                    swipeActionListener!!.onDrag(value, friction)
            }
            .addEndListener { _, canceled, value, _ ->
                lastPosition = value
                val friction = Math.abs((lastPosition - steps[0]) / (steps[steps.size - 1] - steps[0]))
                if (swipeActionListener != null)
                    swipeActionListener!!.onDrag(value, friction)

                if (!canceled) {
                    step = getStepIndex(nextStep)
                    isDragging = false
                    if (swipeActionListener != null)
                        swipeActionListener!!.onDragEnd(nextStep, 1f)
                }
            }

        val min = Math.min(Math.min(currentStep, nextStep), lastPosition)
        val max = Math.max(Math.max(currentStep, nextStep), lastPosition)

        flingAnimation!!.setMinValue(min).setMaxValue(max)
        flingAnimation!!.start()
    }

    private fun getDiff(e1: MotionEvent?, e2: MotionEvent): Float {
        return if (direction == DragDirection.Up || direction == DragDirection.Down) e2.rawY - e1!!.rawY else e2.rawX - e1!!.rawX
    }

    private fun getStepIndex(stepValue: Float): Int {
        return steps.indexOf(stepValue)
    }

    private fun checkSteps(steps: FloatArray) {
        if (steps.size < 2)
            throw RuntimeException("There have to be minimum two steps")

        if (steps[0] == steps[steps.size - 1])
            throw RuntimeException("First and last step are the same. See setSteps() documentation for more details")

        if (direction == DragDirection.Down || direction == DragDirection.Right) {
            if (!isAsc(steps))
                throw RuntimeException("Steps values are not correct for this direction")
        } else if (!isDesc(steps))
            throw RuntimeException("Steps values are not correct for this direction")
    }

    private fun isAsc(tab: FloatArray): Boolean {
        for (i in 1 until tab.size)
            if (tab[i - 1] > tab[i])
                return false
        return true
    }

    private fun isDesc(tab: FloatArray): Boolean {
        for (i in 1 until tab.size)
            if (tab[i - 1] < tab[i])
                return false
        return true
    }
}
