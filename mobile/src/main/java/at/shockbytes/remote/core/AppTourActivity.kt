package at.shockbytes.remote.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import at.shockbytes.remote.R
import com.hololo.tutorial.library.Step
import com.hololo.tutorial.library.TutorialActivity

/**
 * @author Martin Macheiner
 * Date: 28.10.2017.
 */

class AppTourActivity : TutorialActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSlides()
    }

    private fun addSlides() {

        addFragment(Step.Builder()
                .setTitle(getString(R.string.apptour_title_1))
                .setContent(getString(R.string.apptour_description_1))
                .setBackgroundColor(ContextCompat.getColor(this, R.color.apptour_bg_1))
                .setDrawable(R.drawable.apptour_image_1)
                .build())

        addFragment(Step.Builder()
                .setTitle(getString(R.string.apptour_title_2))
                .setContent(getString(R.string.apptour_description_2))
                .setBackgroundColor(ContextCompat.getColor(this, R.color.apptour_bg_2))
                .setDrawable(R.drawable.apptour_image_2)
                .build())

        addFragment(Step.Builder()
                .setTitle(getString(R.string.apptour_title_3))
                .setContent(getString(R.string.apptour_description_3))
                .setBackgroundColor(ContextCompat.getColor(this, R.color.apptour_bg_3))
                .setDrawable(R.drawable.apptour_image_3)
                .build())

        addFragment(Step.Builder()
                .setTitle(getString(R.string.apptour_title_4))
                .setContent(getString(R.string.apptour_description_4))
                .setBackgroundColor(ContextCompat.getColor(this, R.color.apptour_bg_4))
                .setDrawable(R.drawable.apptour_image_4)
                .build())

        addFragment(Step.Builder()
                .setTitle(getString(R.string.apptour_title_5))
                .setContent(getString(R.string.apptour_description_5))
                .setBackgroundColor(ContextCompat.getColor(this, R.color.apptour_bg_5))
                .setDrawable(R.drawable.apptour_image_5)
                .build())

    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, AppTourActivity::class.java)
        }
    }

}
