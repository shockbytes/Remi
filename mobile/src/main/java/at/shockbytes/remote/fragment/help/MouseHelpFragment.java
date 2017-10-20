package at.shockbytes.remote.fragment.help;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import at.shockbytes.remote.R;
import at.shockbytes.remote.fragment.BaseFragment;
import butterknife.BindView;

/**
 * @author Martin Macheiner
 *         Date: 18.10.2017.
 */

public class MouseHelpFragment extends BaseFragment {

    public static MouseHelpFragment newInstance() {
        MouseHelpFragment fragment = new MouseHelpFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.fragment_help_mouse_imgview)
    protected ImageView mImgViewHelp;
    @BindView(R.id.fragment_help_mouse_txt)
    protected TextView mTxtHelp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_mouse, container, false);
    }

    @Override
    protected void setupViews() {
        showMouseHelp();
    }

    private void showMouseHelp() {

        //Mouse specific layout
        mImgViewHelp.setRotation(-30);

        int len = (int) (96 * Resources.getSystem().getDisplayMetrics().density);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(len, len);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mImgViewHelp.setLayoutParams(layoutParams);

        //Initialize drawables for ImageView
        Drawable[] layers = new Drawable[2];
        layers[0] = ContextCompat.getDrawable(getActivity(), R.drawable.ic_help_finger_single);
        layers[1] = ContextCompat.getDrawable(getActivity(), R.drawable.ic_help_finger_twice);
        final TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
        transitionDrawable.setCrossFadeEnabled(true);
        mImgViewHelp.setImageDrawable(transitionDrawable);
        mTxtHelp.setText(R.string.help_mouse_move);

        final Runnable action8 = new Runnable() {
            @Override
            public void run() {

                if (mImgViewHelp != null) {
                    mImgViewHelp.animate().translationX(0).translationY(0).rotation(-30)
                            .setInterpolator(new DecelerateInterpolator())
                            .withStartAction(new Runnable() {
                                @Override
                                public void run() {
                                    transitionDrawable.reverseTransition(500);
                                    mTxtHelp.setText(R.string.help_mouse_move);
                                }
                            }).start();
                }
            }
        };
        final Runnable action7 = new Runnable() {
            @Override
            public void run() {
                //Animate scrolling
                if (mImgViewHelp != null) {
                    mImgViewHelp.animate().translationX(0f).translationY(150f)
                            .setStartDelay(1300).setDuration(700)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(action8).start();
                    mTxtHelp.setText(R.string.help_mouse_scrolling);
                }
            }
        };
        final Runnable action6 = new Runnable() {
            @Override
            public void run() {
                //Animate back to middle
                if (mImgViewHelp != null) {
                    mImgViewHelp.animate().translationX(0f).translationY(-100f)
                            .rotation(0).setStartDelay(1000).setDuration(500)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(action7).withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            transitionDrawable.startTransition(500);
                        }
                    }).start();
                }
            }
        };
        final Runnable action5 = new Runnable() {
            @Override
            public void run() {
                //Right Mouse click up
                if (mImgViewHelp != null) {
                    mImgViewHelp.animate().scaleX(1f).scaleY(1f).rotationX(0)
                            .setStartDelay(1500).setDuration(200)
                            .setInterpolator(new OvershootInterpolator(2f))
                            .withEndAction(action6).start();
                }
            }
        };
        final Runnable action4 = new Runnable() {
            @Override
            public void run() {
                //Right Mouse click down
                if (mImgViewHelp != null) {
                    mImgViewHelp.animate().scaleX(0.8f).rotationX(20).scaleY(0.8f)
                            .setDuration(200).setStartDelay(1000)
                            .setInterpolator(new OvershootInterpolator(2f))
                            .withEndAction(action5).start();
                    mTxtHelp.setText(R.string.help_mouse_right_click);
                }
            }
        };
        final Runnable action3 = new Runnable() {
            @Override
            public void run() {
                //Move mouse to new position
                if (mImgViewHelp != null) {
                    mImgViewHelp.animate().translationX(125f).translationY(-75f)
                            .setStartDelay(1000).setDuration(700)
                            .setInterpolator(new DecelerateInterpolator())
                            .setStartDelay(1000).withEndAction(action4).start();
                }
            }
        };
        final Runnable action2 = new Runnable() {
            @Override
            public void run() {
                //Mouse click up
                if (mImgViewHelp != null) {
                    mImgViewHelp.animate().scaleX(1f).scaleY(1f).rotationX(0)
                            .setStartDelay(0).setDuration(200)
                            .setInterpolator(new OvershootInterpolator(2f))
                            .withEndAction(action3).start();
                }
            }
        };
        Runnable action1 = new Runnable() {
            @Override
            public void run() {
                //Mouse click down
                if (mImgViewHelp != null) {
                    mImgViewHelp.animate().scaleX(0.8f).scaleY(0.8f).rotationX(20)
                            .setDuration(200).setStartDelay(1000)
                            .setInterpolator(new OvershootInterpolator(2f))
                            .withEndAction(action2).start();
                    mTxtHelp.setText(R.string.help_mouse_left_click);
                }
            }
        };
        //Start all animations, starting with this one
        if (mImgViewHelp != null) {
            mImgViewHelp.animate().translationX(-250f)
                    .translationY(150f).setDuration(700)
                    .setInterpolator(new DecelerateInterpolator())
                    .setStartDelay(2000).withEndAction(action1).start();
        }
    }

}
