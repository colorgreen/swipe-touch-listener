package com.colorgreen.swiper;


import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.ArrayList;
import java.util.List;

public class OnSwipeTouchListener implements OnTouchListener {

    private List< SwipeAction > actions = new ArrayList< SwipeAction >();

    public void attachToView( View v ) {
        v.setOnTouchListener( this );
    }

    @Override
    public boolean onTouch( View v, MotionEvent event ) {

        if( actions.size() == 0 ) return false;

//        for( SwipeAction a : actions) {
//            if( a.isExtended() ){
//                a.onTouch( v, event );
//                return true;
//            }
//        }

        for( SwipeAction action : actions ) {
            if( action.isBlocked() ) continue;

            action.onTouch( v, event );
        }

        return true;
    }

    public void addAction( SwipeAction action ){
        actions.add( action );
    }
}