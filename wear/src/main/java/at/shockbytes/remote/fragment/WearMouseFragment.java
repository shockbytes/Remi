package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.core.WearRemiApp;
import at.shockbytes.remote.communication.CommunicationManager;
import butterknife.BindView;

public class WearMouseFragment extends WearBaseFragment
        implements GestureDetector.OnGestureListener, View.OnTouchListener {

    public static WearMouseFragment newInstance() {
        WearMouseFragment fragment = new WearMouseFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.wear_fragment_mouse_background)
    protected View mouseView;

    @Inject
    protected CommunicationManager gateway;

    @Inject
    protected Vibrator vibrator;

    private int oldMouseX;
    private int oldMouseY;
    private int mouseDensity;
    private float scrollDensity;
    private GestureDetectorCompat detector;

    public WearMouseFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WearRemiApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wear_fragment_mouse, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Mouse density and scroll density are hardcoded, because there is no prefence screen
        oldMouseX = oldMouseY = 0;
        mouseDensity = 5;
        scrollDensity = 6/ 10f;
    }

    @Override
    protected void setupViews() {
        detector = new GestureDetectorCompat(getActivity().getApplicationContext(), this);
        mouseView.setOnTouchListener(this);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        gateway.sendMouseLeftClickMessage();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (e2.getPointerCount() == 2) {
            int amount = (int) (Math.round(distanceY) * scrollDensity);
            gateway.sendMouseScrollMessage(amount);
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        vibrator.vibrate(50);
        gateway.sendMouseRightClickMessage();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        detector.onTouchEvent(event);

        int action = event.getAction();
        int fingers = event.getPointerCount();
        if (fingers == 1 && action == MotionEvent.ACTION_DOWN) {
            oldMouseX = (int) event.getX();
            oldMouseY = (int) event.getY();
        } else if (fingers == 1 && action == MotionEvent.ACTION_MOVE) {

            int deltaX = (int) ((event.getX() - oldMouseX) * mouseDensity);
            int deltaY = (int) ((event.getY() - oldMouseY) * mouseDensity);

            gateway.sendMouseMoveMessage(deltaX, deltaY);

            oldMouseX = (int) event.getX();
            oldMouseY = (int) event.getY();
        }
        return true;
    }

}
