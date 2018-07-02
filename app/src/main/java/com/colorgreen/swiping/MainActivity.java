package com.colorgreen.swiping;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.colorgreen.swiper.OnSwipeTouchListener;
import com.colorgreen.swiper.SwipeAction;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView( R.id.main_layout )
    RelativeLayout mainLayout;
    @BindView( R.id.bar )
    LinearLayout bar;
    @BindView( R.id.bottombar )
    LinearLayout bottombar;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        ButterKnife.bind( this );

        final int lightBlue = getColor( R.color.lightblue );
        final int darkBlue = getColor( R.color.darkblue );

        final OnSwipeTouchListener listener = new OnSwipeTouchListener();

        bar.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bar.getViewTreeObserver().removeOnGlobalLayoutListener( this );

                int startBarHeight = bar.getHeight();
                final int targetHeight = mainLayout.getHeight();

                SwipeAction swipeAction = new SwipeAction() {
                    @Override
                    public void onDrag( float val, float friction ) {
                        bar.setLayoutParams( new RelativeLayout.LayoutParams( bar.getWidth(), (int) val ) );
                        bar.setBackgroundColor( interpolateColor( lightBlue, darkBlue, val / targetHeight ) );
                    }
                };

                swipeAction.setDirection( SwipeAction.DragDirection.Down );
                swipeAction.setDragThreshold( 0.4f );
                swipeAction.setSteps( new float[]{ startBarHeight, targetHeight * 0.3f, targetHeight } );

                listener.addAction( swipeAction );
            }
        } );

        bottombar.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bottombar.getViewTreeObserver().removeOnGlobalLayoutListener( this );

                int targetHeight = mainLayout.getHeight();
                bottombar.setY( targetHeight );

                final SwipeAction swipeAction = new SwipeAction() {
                    @Override
                    public void onDrag( float val, float friction ) {
                        bottombar.setY( val );
                    }
                };

                swipeAction.setDirection( SwipeAction.DragDirection.Up );
                swipeAction.setDragThreshold( 0.2f );
                swipeAction.setSteps( new float[]{ targetHeight, targetHeight - targetHeight * 0.7f, 0 } );

                listener.addAction( swipeAction );
            }
        } );

        listener.attachToView( mainLayout );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private float interpolate( float a, float b, float proportion ) {
        return ( a + ( ( b - a ) * proportion ) );
    }

    /**
     * Returns an interpoloated color, between <code>a</code> and <code>b</code>
     */
    private int interpolateColor( int a, int b, float proportion ) {
        float[] hsva = new float[3];
        float[] hsvb = new float[3];
        Color.colorToHSV( a, hsva );
        Color.colorToHSV( b, hsvb );
        for( int i = 0; i < 3; i++ ) {
            hsvb[i] = interpolate( hsva[i], hsvb[i], proportion );
        }
        return Color.HSVToColor( hsvb );
    }
}
