package com.colorgreen.swiper;

public interface SwipeActionListener {

    void onDragStart( float val, float totalFriction );

    void onDrag( float val, float totalFriction );

    void onDragEnd( float val, float totalFriction );
}
