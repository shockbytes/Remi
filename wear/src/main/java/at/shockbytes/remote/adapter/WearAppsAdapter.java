package at.shockbytes.remote.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import at.shockbytes.remote.R;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public class WearAppsAdapter extends BaseAdapter<String> {

    public WearAppsAdapter(Context cxt, List<String> data) {
        super(cxt, data);
    }

    @Override
    public BaseAdapter<String>.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_wear_app, parent, false));
    }

    class ViewHolder extends BaseAdapter<String>.ViewHolder {

        @BindView(R.id.item_wear_app_txt_name)
        TextView txtApp;

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(String s) {
            content = s;
            txtApp.setText(s);
            txtApp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_app, 0, 0, 0);
        }

    }
}
