package at.shockbytes.remote.fragment.help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import at.shockbytes.remote.R;
import at.shockbytes.remote.fragment.BaseFragment;
import butterknife.BindView;

/**
 * @author Martin Macheiner
 *         Date: 18.10.2017.
 */

public class GenericHelpFragment extends BaseFragment {

    enum HelpType {
        APPS, FILES, SLIDES
    }

    private static String ARG_HELP_TYPE = "arg_help_type";

    public static GenericHelpFragment newInstance(HelpType helptype) {
        GenericHelpFragment fragment = new GenericHelpFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_HELP_TYPE, helptype);
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.fragment_help_generic_txt_title)
    protected TextView txtTitle;

    @BindView(R.id.fragment_help_generic_txt_content)
    protected TextView txtContent;

    @BindView(R.id.fragment_help_generic_imgview)
    protected ImageView imgView;


    private HelpType helpType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helpType = (HelpType) getArguments().getSerializable(ARG_HELP_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_generic, container, false);
    }

    @Override
    protected void setupViews() {
        txtTitle.setText(getTitleIdByHelpType());
        txtContent.setText(getTextIdByHelpType());
        imgView.setImageResource(getImageIdByHelpType());
    }

    public int getTextIdByHelpType() {

        int textId = 0;
        switch (helpType) {

            case APPS:
                textId = R.string.help_apps;
                break;
            case FILES:
                textId = R.string.help_files;
                break;
            case SLIDES:
                textId = R.string.help_slides;
                break;
        }
        return textId;
    }

    public int getImageIdByHelpType() {

        int imageId = 0;
        switch (helpType) {

            case APPS:
                imageId = R.drawable.ic_help_content_apps;
                break;
            case FILES:
                imageId = R.drawable.ic_help_content_files;
                break;
            case SLIDES:
                imageId = R.drawable.ic_help_content_slides;
                break;
        }
        return imageId;
    }

    public int getTitleIdByHelpType() {

        int titleId = 0;
        switch (helpType) {

            case APPS:
                titleId = R.string.tab_apps;
                break;
            case FILES:
                titleId = R.string.tab_files;
                break;
            case SLIDES:
                titleId = R.string.tab_slides;
                break;
        }
        return titleId;
    }


}
