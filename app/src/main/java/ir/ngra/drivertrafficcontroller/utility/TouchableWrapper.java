package ir.ngra.drivertrafficcontroller.utility;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import ir.ngra.drivertrafficcontroller.views.fragments.Home;

public class TouchableWrapper extends FrameLayout {

    public TouchableWrapper(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        Home.MapMove = true;
//        switch (event.getAction()) {
//
//            case MotionEvent.ACTION_DOWN:
//
//                break;
//
//            case MotionEvent.ACTION_UP:
//
//                break;
//        }
        return super.dispatchTouchEvent(event);
    }
}