package com.colorgreen.swiper;

import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.animation.FloatValueHolder;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class SwipeAction {
    private static final float SLOW_FACTOR = 4.5f;

    public enum DragDirection {Right, Up, Left, Down}
    private DragDirection direction = DragDirection.Down;

    private float[] steps;
    private int csIter = 0;
    private float dragThreshold = 0.5f;
    private float currentStep;
    private float lastPosition;
    private float startPosition;

    private boolean isDragging = false;
    private boolean blocked = false;

    private FlingAnimation flingAnimation;

    private VelocityTracker velocityTracker;
    private MotionEvent startEvent;

    public SwipeAction() { }

    /**
     * @param direction default drag is DragDirection.Down
     * @param steps
     * @param dragThreshold default threshold is 0.5f
     */
    public SwipeAction( DragDirection direction, float[] steps, float dragThreshold ) {
        this.direction = direction;
        setSteps( steps );
        this.dragThreshold = dragThreshold;
    }

    public boolean onTouch( View v, MotionEvent event ) {
        if( event.getAction() == MotionEvent.ACTION_DOWN )
            velocityTracker = VelocityTracker.obtain();

        if( isDragging )
            velocityTracker.addMovement( event );

        switch( event.getAction() ) {
            case MotionEvent.ACTION_DOWN:
                startDrag( event );
                break;
            case MotionEvent.ACTION_MOVE:
                onMove( startEvent, event );
                break;
            case MotionEvent.ACTION_UP:
                endDrag( startEvent, event );
                break;
        }

        return true;
    }


    /**
     * Set threshold above which drag is executed. If drag is shorter than threshold, drag is no executed
     * and view is returned to start position. Else drag is executed and is animated to end position
     *
     * @param dragThreshold percent above which drag is executed
     */
    public void setDragThreshold( float dragThreshold ) {
        this.dragThreshold = dragThreshold;
    }

    public void onDragStart( float val, float totalFriction ) {}

    public void onDrag( float val, float totalFriction ) {}

    public void onDragEnd( float val, float totalFriction ) {}

    /**
     * Set values, which your drag respects. For direction Left and Up value have to be in descending
     * order. For direction Right and Down values have to be ascending.
     * Example for direction left: [ 0, -300, -600 ]
     * Watch out for using etc. view.getX() function in onCreate() of activity. It will propably return 0.
     * Use view.getViewTreeObserver() to obtain axis properties.
     * @see <a href="https://stackoverflow.com/questions/3591784/views-getwidth-and-getheight-returns-0">stackoverflow.com</a>
     * @param steps Array in ascending or descending order, depends of direction.
     */
    public void setSteps( float[] steps ) {
        checkSteps( steps );
        this.steps = steps;
        currentStep = lastPosition = steps[csIter];
    }

    public DragDirection getDirection() {
        return direction;
    }

    public void setDirection( DragDirection direction ) {
        this.direction = direction;
        if( this.steps != null )
            checkSteps( this.steps );
    }

    public float getDragThreshold() {
        return dragThreshold;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public boolean isExtended(){
        return csIter > 0;
    }

    public int getStep(){
        return csIter;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked( boolean blocked ) {
        this.blocked = blocked;
    }

    ////////////////////////////////// PRIVATE FUNCTION ////////////////////////////////////////////


    private void startDrag( MotionEvent event ) {
        if( isDragging ) {
            if( flingAnimation != null && flingAnimation.isRunning() ) {
                flingAnimation.cancel();
                startEvent = MotionEvent.obtain( event );
            }
        } else {
            onDragStart( currentStep, 0 );
            currentStep = steps[csIter];
            startEvent = MotionEvent.obtain( event );
            isDragging = true;
        }
        startPosition = lastPosition;
    }

    private void onMove( MotionEvent e1, MotionEvent e2 ) {
        final float diff = getDiff( e1, e2 ) + startPosition;
        final float friction = Math.abs( (steps[steps.length-1]-steps[0])/diff);

        if( direction == DragDirection.Left || direction == DragDirection.Up ) {
            if( csIter + 1 == steps.length ) {
                if( steps[csIter - 1] >= diff && diff >= steps[steps.length - 1] ) {
                    onDrag( diff, friction );
                    lastPosition = diff;
                }
            } else if( csIter == 0 ) {
                if( steps[0] >= diff && diff >= steps[csIter + 1] ) {
                    onDrag( diff, friction  );
                    lastPosition = diff;
                }
            } else {

                if( steps[csIter - 1] >= diff && diff >= steps[csIter + 1] ) {
                    onDrag( diff, friction  );
                    lastPosition = diff;
                }
            }
        } else {
            if( csIter + 1 == steps.length ) {
                if( steps[csIter - 1] <= diff && diff <= steps[steps.length - 1] ) {
                    onDrag( diff, friction  );
                    lastPosition = diff;
                }
            } else if( csIter == 0 ) {
                if( steps[0] <= diff && diff <= steps[csIter + 1] ) {
                    onDrag( diff, friction  );
                    lastPosition = diff;
                }
            } else {

                if( steps[csIter - 1] <= diff && diff <= steps[csIter + 1] ) {
                    onDrag( diff, friction  );
                    lastPosition = diff;
                }
            }
        }
    }

    private float getNextStep( float position ) {
        return getNextStep( position, true );
    }

    private float getNextStep( float position, boolean checkThreshold ) {
        float nextStep = 0;

        if( direction == DragDirection.Left || direction == DragDirection.Up ) {
            if( csIter + 1 == steps.length ) {
                if( position < steps[csIter] )
                    nextStep = steps[csIter];
                else
                    nextStep = steps[csIter - 1];

            } else {
                if( position > steps[csIter] )
                    nextStep = steps[csIter != 0 ? csIter - 1 : csIter];
                else
                    nextStep = steps[csIter + 1];

            }
        } else {
            if( csIter + 1 == steps.length ) {
                if( position > steps[csIter] )
                    nextStep = steps[csIter];
                else
                    nextStep = steps[csIter - 1];

            } else {
                if( position < steps[csIter] )
                    nextStep = steps[csIter != 0 ? csIter - 1 : csIter];
                else
                    nextStep = steps[csIter + 1];

            }
        }

        if( checkThreshold && steps[csIter] != nextStep && Math.abs( currentStep - position ) < ( Math.abs( nextStep - steps[csIter] ) * dragThreshold ) )
            return steps[csIter];

        return nextStep;
    }

    private float calculateVelocity( float diff ) {
        velocityTracker.computeCurrentVelocity( 1000 );

        float velocity = velocityTracker.getXVelocity();
        if( direction == DragDirection.Up || direction == DragDirection.Down )
            velocity = velocityTracker.getYVelocity();

        float nextStep = getNextStep( diff, false );

        if( Math.abs( velocity ) < Math.abs( nextStep - lastPosition ) * SLOW_FACTOR ) {
            velocity = ( getNextStep( diff ) - lastPosition ) * SLOW_FACTOR;
        }

        return velocity;
    }

    private void endDrag( MotionEvent e1, MotionEvent e2 ) {
        final float diff = getDiff( e1, e2 );
        final float velocity = calculateVelocity( lastPosition + diff / SLOW_FACTOR );
        final float nextStep = getNextStep( lastPosition + velocity / SLOW_FACTOR );

        final float friction =  Math.abs((steps[steps.length-1]-steps[0])/diff);

        FloatValueHolder floatValueHolder = new FloatValueHolder( lastPosition );
        flingAnimation = new FlingAnimation( floatValueHolder )
                .setStartVelocity( velocity )
                .setStartValue( lastPosition )
                .addUpdateListener( new DynamicAnimation.OnAnimationUpdateListener() {
                    @Override
                    public void onAnimationUpdate( DynamicAnimation animation, float value, float velocity ) {
                        lastPosition = value;
                        onDrag( value, friction  );
                    }
                } )
                .addEndListener( new DynamicAnimation.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd( DynamicAnimation animation, boolean canceled, float value, float velocity ) {
                        lastPosition = value;
                        onDrag( value, friction  );

                        if( !canceled ) {
                            csIter = getStepIndex( nextStep );
                            isDragging = false;
                            onDragEnd( nextStep, 1 );
                        }
                    }
                } );

        float _min = min( currentStep, nextStep, lastPosition );
        float _max = max( currentStep, nextStep, lastPosition );

        flingAnimation.setMinValue( _min ).setMaxValue( _max );
        flingAnimation.start();
    }


    private float getDiff( MotionEvent e1, MotionEvent e2 ) {
        if( direction == DragDirection.Up || direction == DragDirection.Down )
            return e2.getY() - e1.getY();
        return e2.getX() - e1.getX();
    }

    private int getStepIndex( float val ) {
        for( int i = 0; i < steps.length; i++ )
            if( val == steps[i] )
                return i;
        return -1;
    }

    private void checkSteps( float[] steps ) {
        if( steps.length < 2 )
            throw new RuntimeException( "There have to be minimum two steps" );

        if( steps[0] == steps[steps.length - 1] )
            throw new RuntimeException( "First and last step are the same. See setSteps() documentation for more details" );

        if( direction == DragDirection.Down || direction == DragDirection.Right ) {
            if( !isAsc( steps ) )
                throw new RuntimeException( "Steps values are not correct for this direction" );
        } else if( !isDesc( steps ) )
            throw new RuntimeException( "Steps values are not correct for this direction" );
    }

    private static float min( float a, float b, float c ) {
        return Math.min( Math.min( a, b ), c );
    }

    private static float max( float a, float b, float c ) {
        return Math.max( Math.max( a, b ), c );
    }

    private boolean isAsc( float[] tab ) {
        for( int i = 1; i < tab.length; i++ )
            if( tab[i - 1] > tab[i] )
                return false;
        return true;
    }

    private boolean isDesc( float[] tab ) {
        for( int i = 1; i < tab.length; i++ )
            if( tab[i - 1] < tab[i] )
                return false;
        return true;
    }
}
