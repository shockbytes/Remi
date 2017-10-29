package at.shockbytes.remote.fragment;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.KeyboardAdapter;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.model.text.RemiKeyEvent;
import at.shockbytes.remote.network.model.text.StandardRemiKeyEvent;
import at.shockbytes.remote.util.RemiUtils;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author Martin Macheiner
 *         Date: 10.10.2017.
 */

public class KeyboardFragment extends BottomSheetDialogFragment
        implements BaseAdapter.OnItemClickListener<RemiKeyEvent> {

    public static KeyboardFragment newInstance() {
        KeyboardFragment fragment = new KeyboardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public KeyboardFragment() {
    }


    @BindView(R.id.fragment_keyboard_root)
    protected View rootView;

    @BindView(R.id.fragment_keyboard_txt_out)
    protected TextView txtOut;

    @BindView(R.id.fragment_keyboard_recyclerview)
    protected RecyclerView recyclerView;

    @BindView(R.id.fragment_keyboard_btn_switch_text)
    protected Button btnSwitchText;

    @BindView(R.id.fragment_keyboard_btn_switch_arrows)
    protected AppCompatImageButton btnSwitchNumbers;

    @BindView(R.id.fragment_keyboard_imgbtn_caps)
    protected AppCompatImageButton imgBtnCaps;

    @BindView(R.id.fragment_keyboard_viewflipper)
    protected ViewFlipper viewFlipper;

    @BindViews({R.id.fragment_keyboard_imgbtn_up, R.id.fragment_keyboard_imgbtn_left,
            R.id.fragment_keyboard_imgbtn_down, R.id.fragment_keyboard_imgbtn_right})
    protected List<AppCompatImageButton> imageButtonsArrows;

    @Inject
    protected RemiClient client;

    @Inject
    protected SharedPreferences preferences;

    private KeyboardAdapter adapter;

    private Unbinder unbinder;

    private boolean isCapsLock;
    private boolean useDarkTheme;

    private final BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback
            = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);
        isCapsLock = false;
        useDarkTheme = preferences.getBoolean(getString(R.string.prefs_key_keyboard_theme), true);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_keyboard, null);
        unbinder = ButterKnife.bind(this, contentView);
        dialog.setContentView(contentView);
        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        setupViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onItemClick(RemiKeyEvent remiKeyEvent, View view) {

        // capsLockSensitive() will prevent space, backspace and
        // enter commands from being used in caps lock mode on desktop app
        client.writeText(remiKeyEvent.getKeyCode(), isCapsLock && remiKeyEvent.capsLockSensitive())
                .subscribe();

        if (remiKeyEvent instanceof StandardRemiKeyEvent) {
            txtOut.append(remiKeyEvent.getDisplayString());
        }
    }

    @OnClick(R.id.fragment_keyboard_btn_switch_text)
    protected void onClickSwitchText() {
        switchKeyboard(true);
    }

    @OnClick(R.id.fragment_keyboard_btn_switch_arrows)
    protected void onClickSwitchArrows() {
        switchKeyboard(false);
    }

    @OnClick(R.id.fragment_keyboard_imgbtn_caps)
    protected void onClickCapsLock() {
        isCapsLock = !isCapsLock;
        tintCapsLock();
    }

    @OnClick(R.id.fragment_keyboard_imgbtn_up)
    protected void onClickBtnArrowUp() {
        client.writeText(RemiUtils
                .getArrowKeyEvent(RemiUtils.ArrowDirection.UP).getKeyCode(), false)
                .subscribe();
    }

    @OnClick(R.id.fragment_keyboard_imgbtn_left)
    protected void onClickBtnArrowLeft() {
        client.writeText(RemiUtils
                .getArrowKeyEvent(RemiUtils.ArrowDirection.LEFT).getKeyCode(), false)
                .subscribe();
    }

    @OnClick(R.id.fragment_keyboard_imgbtn_down)
    protected void onClickBtnArrowDown() {
        client.writeText(RemiUtils
                .getArrowKeyEvent(RemiUtils.ArrowDirection.DOWN).getKeyCode(), false)
                .subscribe();
    }

    @OnClick(R.id.fragment_keyboard_imgbtn_right)
    protected void onClickBtnArrowRight() {
        client.writeText(RemiUtils
                .getArrowKeyEvent(RemiUtils.ArrowDirection.RIGHT).getKeyCode(), false)
                .subscribe();
    }

    private void setupViews() {

        adapter = new KeyboardAdapter(getContext(), new ArrayList<RemiKeyEvent>(), useDarkTheme);
        adapter.setOnItemClickListener(this);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 10);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.item(position).getRowSpan();
            }
        });
        adapter.setData(RemiUtils.getKeyboard());
        recyclerView.setAdapter(adapter);

        applyKeyboardTheme();
        switchKeyboard(true);
    }

    private void applyKeyboardTheme() {

        int bgColorRes = useDarkTheme ? R.color.keyboard_theme_dark : R.color.keyboard_theme_light;
        int capsColor = !useDarkTheme ? R.color.keyboard_theme_dark : R.color.keyboard_theme_light;
        int txtColorRes = useDarkTheme ? R.color.keyboard_theme_txt_dark : R.color.keyboard_theme_txt_light;

        rootView.setBackgroundResource(bgColorRes);
        txtOut.setTextColor(ContextCompat.getColor(getContext(), txtColorRes));
        imgBtnCaps.setSupportImageTintList(
                ColorStateList.valueOf(ContextCompat.getColor(getContext(), capsColor)));

        tintArrowControls();
    }

    private void switchKeyboard(boolean isTextKeyboardSelected) {
        tintKeyboardSwitchControls(isTextKeyboardSelected);
        changeKeyboardLayout(isTextKeyboardSelected);
    }

    private void changeKeyboardLayout(boolean isTextKeyboardSelected) {

        if (isTextKeyboardSelected && viewFlipper.getDisplayedChild() == 0) {
            viewFlipper.setInAnimation(getContext(), R.anim.slide_in_right);
            viewFlipper.setOutAnimation(getContext(), R.anim.slide_out_left);
            viewFlipper.showNext();
        } else if (!isTextKeyboardSelected && viewFlipper.getDisplayedChild() == 1) {
            viewFlipper.setInAnimation(getContext(), android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(getContext(), android.R.anim.slide_out_right);
            viewFlipper.showPrevious();
        }
    }

    private void tintKeyboardSwitchControls(boolean isTextKeyboardSelected) {

        int txtColorRes = useDarkTheme ? R.color.keyboard_theme_txt_dark : R.color.keyboard_theme_txt_light;

        if (isTextKeyboardSelected) {
            btnSwitchText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            btnSwitchNumbers.setSupportImageTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(getContext(), txtColorRes)));
        } else {
            btnSwitchText.setTextColor(ContextCompat.getColor(getContext(), txtColorRes));
            btnSwitchNumbers.setSupportImageTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorAccent)));
        }
    }

    private void tintCapsLock() {

        int capsColor;
        if (isCapsLock) {
            capsColor = R.color.colorAccent;
        } else {
            capsColor = !useDarkTheme ? R.color.keyboard_theme_dark : R.color.keyboard_theme_light;
        }
        imgBtnCaps.setSupportImageTintList(
                ColorStateList.valueOf(ContextCompat.getColor(getContext(), capsColor)));
    }

    private void tintArrowControls() {

        int tintColorRes = useDarkTheme
                ? R.color.keyboard_theme_keys_dark
                : R.color.keyboard_theme_keys_light;

        for (AppCompatImageButton imgbtn : imageButtonsArrows) {
            imgbtn.setSupportImageTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(getContext(), tintColorRes)));
        }
    }

}
