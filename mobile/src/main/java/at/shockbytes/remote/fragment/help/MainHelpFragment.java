package at.shockbytes.remote.fragment.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.shockbytes.remote.R;
import at.shockbytes.remote.fragment.BaseFragment;
import butterknife.OnClick;

/**
 * @author Martin Macheiner
 *         Date: 17.10.2017.
 */

public class MainHelpFragment extends BaseFragment {

    public static MainHelpFragment newInstance() {
        MainHelpFragment fragment = new MainHelpFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_main, container, false);
    }

    @Override
    protected void setupViews() {
        // No need to setup views
    }

    @OnClick(R.id.fragment_help_main_btn_apps)
    protected void onClickApps() {
        showFragment(GenericHelpFragment.newInstance(GenericHelpFragment.HelpType.APPS));
    }

    @OnClick(R.id.fragment_help_main_btn_mouse)
    protected void onClickMouse() {
        showFragment(MouseHelpFragment.newInstance());
    }

    @OnClick(R.id.fragment_help_main_btn_files)
    protected void onClickFiles() {
        showFragment(GenericHelpFragment.newInstance(GenericHelpFragment.HelpType.FILES));
    }

    @OnClick(R.id.fragment_help_main_btn_slides)
    protected void onClickSlides() {
        showFragment(GenericHelpFragment.newInstance(GenericHelpFragment.HelpType.SLIDES));
    }

    private void showFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

}
