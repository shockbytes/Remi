package at.shockbytes.remote.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import at.shockbytes.remote.R;
import at.shockbytes.remote.network.model.text.RemiKeyEvent;
import at.shockbytes.remote.network.model.text.SpaceRemiKeyEvent;
import at.shockbytes.remote.network.model.text.StandardRemiKeyEvent;
import at.shockbytes.util.adapter.BaseAdapter;

/**
 * @author Martin Macheiner
 *         Date: 10.10.2017.
 */

public class KeyboardAdapter extends BaseAdapter<RemiKeyEvent> {

    private int tintColorRes;

    public KeyboardAdapter(Context cxt, List<RemiKeyEvent> data, boolean useDarkTheme) {
        super(cxt, data);
        tintColorRes = useDarkTheme
                ? R.color.keyboard_theme_keys_dark
                : R.color.keyboard_theme_keys_light;
    }

    public RemiKeyEvent item(int position) {
        return data.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).layout();
    }

    @Override
    public BaseAdapter<RemiKeyEvent>.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(viewType, parent, false));
    }

    private class ViewHolder extends BaseAdapter<RemiKeyEvent>.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(RemiKeyEvent remiKeyEvent) {
            content = remiKeyEvent;

            if (remiKeyEvent instanceof StandardRemiKeyEvent) {
                Button keyBtn = (Button) itemView;
                keyBtn.setTextColor(ContextCompat.getColor(context, tintColorRes));
                keyBtn.setText(remiKeyEvent.getDisplayString());
            } else if (remiKeyEvent instanceof SpaceRemiKeyEvent) {
                AppCompatButton keyBtn = (AppCompatButton) itemView;
                keyBtn.setTextColor(ContextCompat.getColor(context, tintColorRes));
            } else {
                AppCompatImageButton keyBtn = (AppCompatImageButton) itemView;
                keyBtn.setSupportImageTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(context, tintColorRes)));
            }

        }
    }

}
