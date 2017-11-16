package at.shockbytes.remote.fragment


import android.os.Bundle
import android.os.SystemClock
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import at.shockbytes.remote.R
import at.shockbytes.remote.adapter.SlidesPreviewAdapter
import at.shockbytes.remote.core.RemiApp
import at.shockbytes.remote.network.RemiClient
import at.shockbytes.remote.network.model.SlidesResponse
import at.shockbytes.remote.util.RemiUtils
import at.shockbytes.util.adapter.BaseAdapter
import butterknife.BindView
import butterknife.BindViews
import butterknife.OnClick
import io.reactivex.annotations.NonNull
import io.reactivex.observers.DisposableObserver
import java.util.*
import javax.inject.Inject

class SlidesFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener, BaseAdapter.OnItemClickListener<SlidesResponse.SlidesEntry> {

    @BindView(R.id.fragment_slides_imgbtn_overflow)
    lateinit var imgBtnOverflow: ImageButton

    @BindView(R.id.fragment_slides_btn_start)
    lateinit var btnStart: Button

    @BindView(R.id.fragment_slides_chronometer)
    lateinit var chronometer: Chronometer

    @BindView(R.id.fragment_slides_preview_progressbar)
    lateinit var progressBarPreview: ProgressBar

    @BindView(R.id.fragment_slides_preview_txt)
    lateinit var txtPreview: TextView

    @BindView(R.id.fragment_slides_preview_container)
    lateinit var previewContainer: View

    @BindView(R.id.fragment_slides_rv_slideshow)
    lateinit var recyclerViewPreview: RecyclerView

    @BindView(R.id.fragment_slides_imgview_preview)
    lateinit var imgViewPreview: ImageView

    @BindViews(R.id.fragment_slides_btn_previous, R.id.fragment_slides_btn_next)
    lateinit var btnNavigationControls: List<@JvmSuppressWildcards View>

    @Inject
    lateinit var client: RemiClient

    private lateinit var popupMenu: PopupMenu

    private var selectedSlides: String? = null
    private var isSlidesMode: Boolean = false
    private var currentSlide: Int = 0

    private lateinit var adapter: SlidesPreviewAdapter

    private var slidesObserver: SlidesObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity.application as RemiApp).appComponent.inject(this)

        selectedSlides = arguments.getString(ARG_SELECTED_SLIDES)
        isSlidesMode = selectedSlides != null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_slides, container, false)
    }

    override fun onStop() {
        super.onStop()
        stopTimer()

        if (slidesObserver?.isDisposed == false) {
            slidesObserver?.dispose()
            showToast(getString(R.string.slide_request_cancelled))
        }
    }

    override fun setupViews() {
        resetViews()
        loadSlidesIfAvailable()
    }

    override fun onItemClick(slidesEntry: SlidesResponse.SlidesEntry, view: View) {
        currentSlide = slidesEntry.slideNumber - 1
        showSlidePreview(currentSlide) // Slide numbers start with 1
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.popup_menu_slides_fullscreen_google -> {
                client.sendSlidesFullscreenCommand(RemiClient.SlidesProduct.GOOGLE_SLIDES).subscribe()
                return true
            }

            R.id.popup_menu_slides_fullscreen_powerpoint -> {
                client.sendSlidesFullscreenCommand(RemiClient.SlidesProduct.POWERPOINT).subscribe()
                return true
            }

            R.id.popup_menu_slides_stop_timer -> {
                stopTimer()
                return true
            }

            R.id.popup_menu_slides_reset -> {
                resetViews()
                return true
            }
        }
        return false
    }

    @OnClick(R.id.fragment_slides_imgbtn_overflow)
    fun onClickPopupOverflow() {
        popupMenu.show()
    }

    @OnClick(R.id.fragment_slides_btn_start)
    fun onClickSlidesStart() {

        btnStart.animate().scaleX(0.5f).scaleY(0.5f)
                .alpha(0f)
                .withEndAction { btnStart.visibility = View.INVISIBLE }
                .start()

        btnNavigationControls.forEach {
            it.isEnabled = true
            it.animate().alpha(1f).start()
        }

        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()

        if (isSlidesMode) {
            currentSlide = 0
            showSlidePreview(currentSlide)
        }
    }

    @OnClick(R.id.fragment_slides_btn_next)
    fun onClickSlidesNext() {
        client.sendSlidesNextCommand().subscribe()
        updateSlidesPreview(true)
    }

    @OnClick(R.id.fragment_slides_btn_previous)
    fun onClickSlidesPrevious() {
        client.sendSlidesPreviousCommand().subscribe()
        updateSlidesPreview(false)
    }

    private fun stopTimer() {
        chronometer.stop()
        chronometer.text = "--:--"
    }

    private fun resetViews() {

        // Setup timer and control buttons
        stopTimer()
        btnNavigationControls.forEach {
            it.isEnabled = false
            it.animate().alpha(0f).start()
        }

        // Setup start button
        btnStart.visibility = View.VISIBLE
        btnStart.animate().scaleX(1f).scaleY(1f).alpha(1f).start()

        // Setup popup menu for fullscreen
        popupMenu = PopupMenu(context, imgBtnOverflow)
        popupMenu.menuInflater.inflate(R.menu.popup_slides, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(this)

        // Setup preview container
        imgViewPreview.setImageResource(android.R.color.transparent)
        previewContainer.visibility = View.VISIBLE
        txtPreview.setText(R.string.fragment_slides_open_in_files)
        progressBarPreview.visibility = View.GONE

        // Setup RecyclerView for preview
        recyclerViewPreview.layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false)
        adapter = SlidesPreviewAdapter(context, ArrayList())
        adapter.setOnItemClickListener(this)
        recyclerViewPreview.adapter = adapter
    }

    private fun updateSlidesPreview(isNext: Boolean) {
        if (isSlidesMode) {
            currentSlide = if (isNext) currentSlide + 1 else currentSlide - 1
            showSlidePreview(currentSlide)
        }
    }

    private fun showSlidePreview(slide: Int) {
        if (slide < adapter.itemCount) {
            recyclerViewPreview.scrollToPosition(slide)
            imgViewPreview.setImageBitmap(RemiUtils
                    .base64ToImage(adapter.data[slide].base64Image))
        }
    }

    private fun loadSlidesIfAvailable() {

        // Check if slides are available
        if (isSlidesMode && selectedSlides != null) {
            txtPreview.setText(R.string.fragment_slides_requesting_slides)
            progressBarPreview.visibility = View.VISIBLE
            slidesObserver = client.requestSlides(selectedSlides!!).subscribeWith(SlidesObserver())
        } else {
            txtPreview.setText(R.string.fragment_slides_open_in_files)
            progressBarPreview.visibility = View.GONE
        }
    }

    private inner class SlidesObserver : DisposableObserver<SlidesResponse>() {

        override fun onNext(@NonNull slidesResponse: SlidesResponse) {
            adapter.data = slidesResponse.slides
            currentSlide = 0
            imgViewPreview.setImageBitmap(RemiUtils
                    .base64ToImage(slidesResponse.slides[currentSlide].base64Image))
            previewContainer.visibility = View.GONE
            dispose()
        }

        override fun onError(@NonNull e: Throwable) {
            e.printStackTrace()
            txtPreview.setText(R.string.fragment_slides_loading_error)
            progressBarPreview.visibility = View.GONE
            dispose()
        }

        override fun onComplete() {
            dispose()
        }
    }

    companion object {

        private val ARG_SELECTED_SLIDES = "arg_selected_slides"

        fun newInstance(selectedSlides: String?): SlidesFragment {
            val fragment = SlidesFragment()
            val args = Bundle()
            args.putString(ARG_SELECTED_SLIDES, selectedSlides)
            fragment.arguments = args
            return fragment
        }
    }

}
