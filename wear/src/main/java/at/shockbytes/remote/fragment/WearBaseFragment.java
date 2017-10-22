package at.shockbytes.remote.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public abstract class WearBaseFragment extends Fragment {

    public WearBaseFragment() { }

    private Unbinder unbinder;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (unbinder == null) {
            unbinder = ButterKnife.bind(this, view);
        }
        setupViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        unbinder = null;
    }

    protected abstract void setupViews();

}
