package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.network.RemiClient;

public class MouseFragment extends BaseFragment {

    public static MouseFragment newInstance() {
        MouseFragment fragment = new MouseFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected RemiClient client;

    public MouseFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mouse, container, false);
    }

}
