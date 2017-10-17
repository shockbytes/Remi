package at.shockbytes.remote.fragment;


import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.remote.R;
import at.shockbytes.remote.adapter.SlidesPreviewAdapter;
import at.shockbytes.remote.core.RemiApp;
import at.shockbytes.remote.network.RemiClient;
import at.shockbytes.remote.network.model.SlidesResponse;
import at.shockbytes.remote.util.RemiUtils;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

public class SlidesFragment extends BaseFragment
        implements PopupMenu.OnMenuItemClickListener,
        BaseAdapter.OnItemClickListener<SlidesResponse.SlidesEntry> {

    private static final String ARG_SELECTED_SLIDES = "arg_selected_slides";

    public static SlidesFragment newInstance(String selectedSlides) {
        SlidesFragment fragment = new SlidesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_SLIDES, selectedSlides);
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.fragment_slides_imgbtn_overflow)
    protected ImageButton imgBtnOverflow;

    @BindView(R.id.fragment_slides_btn_start)
    protected Button btnStart;

    @BindView(R.id.fragment_slides_chronometer)
    protected Chronometer chronometer;

    @BindView(R.id.fragment_slides_preview_progressbar)
    protected ProgressBar progressBarPreview;

    @BindView(R.id.fragment_slides_preview_txt)
    protected TextView txtPreview;

    @BindView(R.id.fragment_slides_preview_container)
    protected View previewContainer;

    @BindView(R.id.fragment_slides_rv_slideshow)
    protected RecyclerView recyclerViewPreview;

    @BindView(R.id.fragment_slides_imgview_preview)
    protected ImageView imgViewPreview;

    @BindViews({R.id.fragment_slides_btn_previous, R.id.fragment_slides_btn_next})
    protected List<ImageButton> btnNavigationControls;

    @Inject
    protected RemiClient client;

    protected PopupMenu popupMenu;

    private String selectedSlides;
    private boolean isSlidesMode;
    private int currentSlide;

    private SlidesPreviewAdapter adapter;

    private SlidesObserver slidesObserver;

    public SlidesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RemiApp) getActivity().getApplication()).getAppComponent().inject(this);

        selectedSlides = getArguments().getString(ARG_SELECTED_SLIDES);
        isSlidesMode = selectedSlides != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_slides, container, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTimer();

        if (slidesObserver != null && !slidesObserver.isDisposed()) {
            slidesObserver.dispose();
        }
    }

    @Override
    protected void setupViews() {
        resetViews();
        loadSlidesIfAvailable();
    }

    @Override
    public void onItemClick(SlidesResponse.SlidesEntry slidesEntry, View view) {
        currentSlide = slidesEntry.getSlideNumber() - 1;
        showPreviewSlide(currentSlide); // Slide numbers start with 1
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.popup_menu_slides_fullscreen_google:
                client.sendSlidesFullscreenCommand(RemiClient.SlidesProduct.GOOGLE_SLIDES)
                        .subscribe();
                return true;

            case R.id.popup_menu_slides_fullscreen_powerpoint:
                client.sendSlidesFullscreenCommand(RemiClient.SlidesProduct.POWERPOINT)
                        .subscribe();
                return true;

            case R.id.popup_menu_slides_stop_timer:
                stopTimer();
                return true;

            case R.id.popup_menu_slides_reset:
                resetViews();
                return true;
        }
        return false;
    }

    @OnClick(R.id.fragment_slides_imgbtn_overflow)
    protected void onClickPopupOverflow() {
        popupMenu.show();
    }

    @OnClick(R.id.fragment_slides_btn_start)
    protected void onClickSlidesStart() {

        btnStart.animate().scaleX(0.5f).scaleY(0.5f).alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                btnStart.setVisibility(View.INVISIBLE);
            }
        }).start();

        for (ImageButton imgBtnControl : btnNavigationControls) {
            imgBtnControl.setEnabled(true);
            imgBtnControl.animate().alpha(1).start();
        }

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        if (isSlidesMode) {
            currentSlide = 0;
            showPreviewSlide(currentSlide);
        }
    }

    @OnClick(R.id.fragment_slides_btn_next)
    protected void onClickSlidesNext() {
        client.sendSlidesNextCommand().subscribe();
        updatePreviewSlides(true);
    }

    @OnClick(R.id.fragment_slides_btn_previous)
    protected void onClickSlidesPrevious() {
        client.sendSlidesPreviousCommand().subscribe();
        updatePreviewSlides(false);
    }

    private void stopTimer() {
        chronometer.stop();
        chronometer.setText("--:--");
    }

    private void resetViews() {

        // Setup timer and control buttons
        stopTimer();
        for (ImageButton imgBtnControl : btnNavigationControls) {
            imgBtnControl.setEnabled(false);
            imgBtnControl.animate().alpha(0).start();
        }

        // Setup start button
        btnStart.setVisibility(View.VISIBLE);
        btnStart.animate().scaleX(1f).scaleY(1f).alpha(1).start();

        // Setup popup menu for fullscreen
        popupMenu = new PopupMenu(getContext(), imgBtnOverflow);
        popupMenu.getMenuInflater().inflate(R.menu.popup_slides, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);

        // Setup preview container
        imgViewPreview.setImageResource(android.R.color.transparent);
        previewContainer.setVisibility(View.VISIBLE);
        txtPreview.setText(R.string.fragment_slides_open_in_files);
        progressBarPreview.setVisibility(View.GONE);

        // Setup RecyclerView for preview
        recyclerViewPreview
                .setLayoutManager(new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL, false));
        adapter = new SlidesPreviewAdapter(getContext(), new ArrayList<SlidesResponse.SlidesEntry>());
        adapter.setOnItemClickListener(this);
        recyclerViewPreview.setAdapter(adapter);
    }

    private void updatePreviewSlides(boolean isNext) {
        if (isSlidesMode) {
            currentSlide = isNext ? currentSlide + 1 : currentSlide - 1;
            showPreviewSlide(currentSlide);
        }
    }

    private void showPreviewSlide(int slide) {
        if (slide < adapter.getItemCount()) {
            recyclerViewPreview.scrollToPosition(slide);
            imgViewPreview.setImageBitmap(RemiUtils
                    .base64ToImage(adapter.getData().get(slide).getBase64Image()));
        }
    }

    private void loadSlidesIfAvailable() {

        // Check if slides are available
        if (isSlidesMode) {
            txtPreview.setText(R.string.fragment_slides_requesting_slides);
            progressBarPreview.setVisibility(View.VISIBLE);
            slidesObserver = client.requestSlides(selectedSlides).subscribeWith(new SlidesObserver());
        } else {
            txtPreview.setText(R.string.fragment_slides_open_in_files);
            progressBarPreview.setVisibility(View.GONE);
        }
    }

    private class SlidesObserver extends DisposableObserver<SlidesResponse> {

        @Override
        public void onNext(@NonNull SlidesResponse slidesResponse) {
            adapter.setData(slidesResponse.getSlides());
            currentSlide = 0;
            imgViewPreview.setImageBitmap(RemiUtils
                    .base64ToImage(slidesResponse.getSlides().get(currentSlide).getBase64Image()));
            previewContainer.setVisibility(View.GONE);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            e.printStackTrace();
            txtPreview.setText(R.string.fragment_slides_loading_error);
            progressBarPreview.setVisibility(View.GONE);
            dispose();
        }

        @Override
        public void onComplete() {
            dispose();
        }
    }

}
