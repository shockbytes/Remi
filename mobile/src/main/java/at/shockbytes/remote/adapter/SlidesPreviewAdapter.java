package at.shockbytes.remote.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import at.shockbytes.remote.R;
import at.shockbytes.remote.network.model.SlidesResponse;
import at.shockbytes.remote.util.RemiUtils;
import at.shockbytes.util.adapter.BaseAdapter;

/**
 * @author Martin Macheiner
 *         Date: 17.10.2017.
 */

public class SlidesPreviewAdapter extends BaseAdapter<SlidesResponse.SlidesEntry> {

    public SlidesPreviewAdapter(Context cxt, List<SlidesResponse.SlidesEntry> data) {
        super(cxt, data);
    }

    @Override
    public BaseAdapter<SlidesResponse.SlidesEntry>.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                                 int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_slides_preview, parent, false));
    }

    private class ViewHolder extends BaseAdapter<SlidesResponse.SlidesEntry>.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(SlidesResponse.SlidesEntry slidesEntry) {
            content = slidesEntry;

            ImageView imgView = (ImageView) itemView;
            imgView.setImageBitmap(RemiUtils.base64ToImage(slidesEntry.getBase64Image()));
        }
    }

}
