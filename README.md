# swipe-touch-listener

Simple touch listener for swiping gestures.

![presentation gif](/screenshots/main.gif)

## Getting Started

### Installing

Add to your .gradle file

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
    
dependencies {
        implementation 'com.github.colorgreen:swipe-touch-listener:v1.3'
}
```

### Usage

* First create listener
```
OnSwipeTouchListener listener = new OnSwipeTouchListener();
```
* Then create SwipeAction
```
SwipeAction swipeAction = new SwipeAction();
```
* Create listener
```
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
        // use setBlocked() for blocking another panels from extending
        bottomSwipeAction.setBlocked( swipeAction.isExtended() );
    }
} );
```
You can override functions like onDragStart, onDrag and onDragEnd. onDragEnd is invoked only when ```val``` reach step.
Set properties for action.
```
swipeAction.setDirection( SwipeAction.DragDirection.Down );
swipeAction.setSteps( new float[]{ startBarHeight, targetHeight * 0.3f, targetHeight } );
swipeAction.setDragThreshold( 0.4f );
```
Threshold is distance in percent between steps above which we accept drag and value will be automatically animated to next step.
For example steps are [ 200,700 ], threshold is 0.2f, so if we drag over 300 ( (700-200)*0.2f) ) value will be animated to 700, else back to 300.

* Add action to listener
```
listener.addAction( swipeAction );
```
We can add multiple actions to listener. See example app for this case. But no more than one view can be extended simultaneously.
* And attach listener to view
```
listener.attachToView( view );
//... or
view.setOnTouchListener( listener );
```

#### Other functions

* setBlocked( boolean )
* pushToStep( int stepIndex )
* expand()
* collapse()
* isExtended()

## Authors

[@colorgreen](https://github.com/colorgreen)

## Version History
* v1.3
    * Swipe action is based now on listener
    * Addes expand, collapse and pushToStep method which allow to control elements without swipe
* v1.2
    * Friction fix
* v1.1
    * Added friction in onDrag
* v1.0
    * Initial Release

## Licence
 Free to use