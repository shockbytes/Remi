package at.shockbytes.remote.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;

import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.core.WearRemiApp;
import at.shockbytes.remote.communication.CommunicationManager;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

/**
 * @author Martin Macheiner
 *         Date: 22.10.2017.
 */

public class WearSlidesFragment extends WearBaseFragment {

    public static WearSlidesFragment newInstance() {
        WearSlidesFragment fragment = new WearSlidesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected CommunicationManager gateway;

    @BindView(R.id.wear_fragment_slides_chronometer)
    protected Chronometer chronometer;

    @BindViews({R.id.wear_fragment_slides_btn_next, R.id.wear_fragment_slides_btn_previous})
    protected List<ImageButton> imgBtnControls;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WearRemiApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wear_fragment_slides, container, false);
    }

    @Override
    protected void setupViews() {

    }

    @OnClick(R.id.wear_fragment_slides_btn_previous)
    protected void onClickPreviousSlide() {
        gateway.sendSlidesPreviousMessage();
    }

    @OnClick(R.id.wear_fragment_slides_btn_next)
    protected void onClickNextSlide() {
        gateway.sendSlidesNextMessage();
    }

    @OnClick(R.id.wear_fragment_slides_btn_start)
    protected void onClickStartSlideShow() {
        // TODO
    }

}
