package at.shockbytes.remote.fragment;

import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;

import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.communication.CommunicationManager;
import at.shockbytes.remote.core.WearMainActivity;
import at.shockbytes.remote.core.WearRemiApp;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

/**
 * @author Martin Macheiner
 *         Date: 22.10.2017.
 */

public class WearSlidesFragment extends WearBaseFragment
        implements Chronometer.OnChronometerTickListener,
        WearMainActivity.OnWristNavigationListener, WearMainActivity.OnSlidesControlListener {

    public static WearSlidesFragment newInstance() {
        WearSlidesFragment fragment = new WearSlidesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private long minutes;

    @Inject
    protected CommunicationManager gateway;

    @Inject
    protected Vibrator vibrator;

    @BindView(R.id.wear_fragment_slides_chronometer)
    protected Chronometer chronometer;

    @BindViews({R.id.wear_fragment_slides_btn_next, R.id.wear_fragment_slides_btn_previous})
    protected List<AppCompatImageButton> imgBtnControls;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WearRemiApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wear_fragment_slides, container, false);
    }

    @Override
    protected void setupViews() {
        minutes = 0;
        chronometer.setOnChronometerTickListener(this);
        setControlsEnabled(false);
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {

        if (SystemClock.elapsedRealtime() - chronometer.getBase() > (60000L * minutes)) {
            vibrator.vibrate(100);
            minutes++;
        }
    }

    @OnClick(R.id.wear_fragment_slides_btn_previous)
    protected void onClickPreviousSlide() {
        gateway.sendSlidesPreviousMessage();
    }

    @OnClick(R.id.wear_fragment_slides_btn_next)
    protected void onClickNextSlide() {
        gateway.sendSlidesNextMessage();
    }

    @Override
    public void onNext() {
        onClickNextSlide();
    }

    @Override
    public void onBack() {
        onClickPreviousSlide();
    }

    @Override
    public void onStopTimer() {
        stopTimer();
    }

    @Override
    public void onStartSlideshow() {
        startSlideshow();
    }

    @Override
    public void onResetTimer() {
        stopTimer();
        setControlsEnabled(false);
    }

    @Override
    public void onGoogleSlidesFullscreen() {
        gateway.sendSlidesFullscreenRequest(CommunicationManager.SlidesProduct.GOOGLE_SLIDES);
    }

    @Override
    public void onPowerpointFullscreen() {
        gateway.sendSlidesFullscreenRequest(CommunicationManager.SlidesProduct.POWERPOINT);
    }

    protected void startSlideshow() {

        setControlsEnabled(true);

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void setControlsEnabled(boolean isEnabled) {
        for (AppCompatImageButton imgBtnControl : imgBtnControls) {
            imgBtnControl.setEnabled(isEnabled);
            imgBtnControl.animate().alpha(isEnabled ? 1 : 0).start();
        }
    }

    private void stopTimer() {
        chronometer.stop();
        chronometer.setText("--:--");
    }


}
