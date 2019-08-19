package com.colorgreen.swiping;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.colorgreen.swiper.OnSwipeTouchListener;
import com.colorgreen.swiper.SwipeAction;
import com.colorgreen.swiper.SwipeActionListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView( R.id.main_layout )
    RelativeLayout mainLayout;
    @BindView( R.id.bar )
    LinearLayout bar;
    @BindView( R.id.bottombar )
    LinearLayout bottombar;
    @BindView( R.id.expand_button )
    Button expandButton;
    @BindView( R.id.collapse_button)
    Button collapseButton;

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

                // for drawing layouts over buttons
                bar.setZ(10);
                bottombar.setZ(10);

                final int targetHeight = mainLayout.getHeight();

                final SwipeAction swipeAction = new SwipeAction();
                swipeAction.setDirection( SwipeAction.DragDirection.Down );
                swipeAction.setDragThreshold( 0.4f );
                swipeAction.setSteps( new float[]{ 0, targetHeight * 0.3f, targetHeight } );

                bottombar.setY( targetHeight );
                final SwipeAction bottomSwipeAction = new SwipeAction();
                bottomSwipeAction.setDirection( SwipeAction.DragDirection.Up );
                bottomSwipeAction.setDragThreshold( 0.2f );
                bottomSwipeAction.setSteps( new float[]{ targetHeight, targetHeight - targetHeight * 0.7f, 0 } );

                bottomSwipeAction.setSwipeActionListener( new SwipeActionListener() {
                    @Override
                    public void onDragStart( float val, float totalFriction ) {}

                    @Override
                    public void onDrag( float val, float totalFriction ) {
                        bottombar.setY( val );
                    }

                    @Override
                    public void onDragEnd( float val, float totalFriction ) {
                        swipeAction.setBlocked( bottomSwipeAction.isExtended() );
                    }
                } );

                swipeAction.setSwipeActionListener( new SwipeActionListener() {
                    @Override
                    public void onDragStart( float val, float totalFriction ) {}

                    @Override
                    public void onDrag( float val, float friction ) {
                        bar.setLayoutParams( new RelativeLayout.LayoutParams( bar.getWidth(), (int) val ) );
                        bar.setBackgroundColor( interpolateColor( lightBlue, darkBlue, val / targetHeight ) );
                    }

                    @Override
                    public void onDragEnd( float val, float totalFriction ) {
                        bottomSwipeAction.setBlocked( swipeAction.isExtended() );
                    }
                } );

                listener.addAction( swipeAction );
                listener.addAction( bottomSwipeAction );

                expandButton.setOnClickListener( v -> {
                    if( !swipeAction.isBlocked() )
                        swipeAction.expand();
                } );

                collapseButton.setOnClickListener( v -> {
                        if( !swipeAction.isBlocked() )
                            swipeAction.collapse();
                } );
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
