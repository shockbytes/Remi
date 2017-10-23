package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public abstract class BaseFragment extends Fragment {

    public BaseFragment() { }

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

    protected void showSnackbar(String text) {
        if (getView() != null) {
            Snackbar.make(getView(), text, Snackbar.LENGTH_LONG).show();
        }
    }

}
