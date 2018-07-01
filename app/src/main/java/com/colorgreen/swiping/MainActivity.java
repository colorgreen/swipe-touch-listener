package com.colorgreen.swiping;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.colorgreen.swiper.OnSwipeTouchListener;
import com.colorgreen.swiping.R;

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

        bar.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bar.getViewTreeObserver().removeOnGlobalLayoutListener( this );

                int startBarHeight = bar.getHeight();
                int targetHeight = mainLayout.getHeight();

                OnSwipeTouchListener listener = new OnSwipeTouchListener() {
                    @Override
                    public void onDrag( float val ) {
                        bar.setLayoutParams( new RelativeLayout.LayoutParams( bar.getWidth(), (int) val ) );
                    }
                };

                listener.setDirection( OnSwipeTouchListener.DragDirection.Down );
                listener.setDragThreshold( 0.4f );
                listener.setSteps( new float[]{ startBarHeight, targetHeight * 0.3f, targetHeight } );

                listener.attachToView( mainLayout );
            }
        } );

        bottombar.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bottombar.getViewTreeObserver().removeOnGlobalLayoutListener( this );

                int targetHeight = mainLayout.getHeight();
                int startBarHeight = targetHeight - dpToPx( 50 );

                bottombar.setY( startBarHeight );

                OnSwipeTouchListener listener = new OnSwipeTouchListener() {
                    @Override
                    public void onDrag( float val ) {
                        bottombar.setY( val );
                    }
                };

                listener.setDirection( OnSwipeTouchListener.DragDirection.Up );
                listener.setDragThreshold( 0.2f );
                listener.setSteps( new float[]{ startBarHeight, startBarHeight - targetHeight * 0.7f, 0 } );

                listener.attachToView( mainLayout );
            }
        } );
    }

    public static int dpToPx( int dp ) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * ( metrics.densityDpi / 160f );
        return Math.round( px );
    }
}
