package at.shockbytes.remote.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.network.RemiClient;
import butterknife.BindView;

public class MouseFragment extends BaseFragment
        implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private static final String ARG_PERMISSION = "arg_permission";

    public static MouseFragment newInstance(boolean hasPermission) {
        MouseFragment fragment = new MouseFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PERMISSION, hasPermission);
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.fragment_mouse_background)
    protected View mouseView;

    @Inject
    protected RemiClient client;

    @Inject
    protected Vibrator vibrator;

    @Inject
    protected SharedPreferences prefs;

    private int oldMouseX;
    private int oldMouseY;
    private int mouseDensity;
    private float scrollDensity;
    private GestureDetectorCompat detector;

    private boolean hasPermission;

    public MouseFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);
        hasPermission = getArguments().getBoolean(ARG_PERMISSION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mouse, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        oldMouseX = oldMouseY = 0;
        mouseDensity = prefs.getInt(getString(R.string.prefs_key_simple_mouse), 2);
        scrollDensity = prefs.getInt(getString(R.string.prefs_key_scrolling), 3) / 10f;
    }

    @Override
    protected void setupViews() {

        if (hasPermission) {
            detector = new GestureDetectorCompat(getActivity().getApplicationContext(), this);
            mouseView.setOnTouchListener(this);
        } else {
            Snackbar.make(getView(), "No permission for mouse!", Snackbar.LENGTH_LONG).show();
        }
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
        client.sendLeftClick().subscribe();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (e2.getPointerCount() == 2) {
            int amount = (int) (Math.round(distanceY) * scrollDensity);
            client.sendScroll(amount).subscribe();
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        vibrator.vibrate(50);
        client.sendRightClick().subscribe();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        /* TODO Remove?
        At first close keyboard if opened
        if (mBottomSheetKeyboard.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetKeyboard.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return false;
        } */
        detector.onTouchEvent(event);

        int action = event.getAction();
        int fingers = event.getPointerCount();
        if (fingers == 1 && action == MotionEvent.ACTION_DOWN) {
            oldMouseX = (int) event.getX();
            oldMouseY = (int) event.getY();
        } else if (fingers == 1 && action == MotionEvent.ACTION_MOVE) {

            int deltaX = (int) ((event.getX() - oldMouseX) * mouseDensity);
            int deltaY = (int) ((event.getY() - oldMouseY) * mouseDensity);

            client.sendMouseMove(deltaX, deltaY).subscribe();

            oldMouseX = (int) event.getX();
            oldMouseY = (int) event.getY();
        }
        return true;
    }

}
