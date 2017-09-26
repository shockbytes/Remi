package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.shockbytes.remote.R;
import butterknife.OnClick;


public class LoginFragment extends BaseFragment {

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @OnClick(R.id.fragment_login_btn_search)
    protected void onClickBtnSearch() {
        Snackbar.make(getView(), "Search for devices...", Snackbar.LENGTH_SHORT).show();
    }

}
