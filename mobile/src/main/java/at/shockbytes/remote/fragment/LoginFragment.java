package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.network.RemiClient;
import butterknife.OnClick;
import butterknife.OnLongClick;
import rx.functions.Action1;


public class LoginFragment extends BaseFragment {

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() { }

    @Inject
    protected RemiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @OnLongClick(R.id.fragment_login_imgview_icon)
    protected boolean onClickDebugEntryIcon() {

        Toast.makeText(getContext(), "Developer access", Toast.LENGTH_SHORT).show();
        // TODO
        return true;
    }

    @OnClick(R.id.fragment_login_btn_search)
    protected void onClickBtnSearch() {

        client.connect("http://10.59.0.243:8080").subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                Snackbar.make(getView(), "Connected!", Snackbar.LENGTH_SHORT).show();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
                Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                client.disconnect().subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        Toast.makeText(getContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 5000); */

    }

}
