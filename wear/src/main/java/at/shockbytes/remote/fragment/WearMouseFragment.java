package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.communication.CommunicationManager;
import at.shockbytes.remote.core.WearRemiApp;
import butterknife.OnClick;

public class WearMouseFragment extends WearBaseFragment {

    public static WearMouseFragment newInstance() {
        WearMouseFragment fragment = new WearMouseFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected CommunicationManager gateway;

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
    protected void setupViews() {
    }

    @OnClick(R.id.wear_fragment_mouse_btn_right)
    protected void onClickMouseRight(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        gateway.sendMouseRightClickMessage();
    }

    @OnClick(R.id.wear_fragment_mouse_btn_left)
    protected void onClickMouseLeft(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        gateway.sendMouseLeftClickMessage();
    }

}
