package at.shockbytes.remote.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import at.shockbytes.remote.R;
import at.shockbytes.remote.network.model.DesktopApp;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public class DesktopAppsAdapter extends BaseAdapter<DesktopApp> {

    public DesktopAppsAdapter(Context cxt, List<DesktopApp> data) {
        super(cxt, data);
    }

    @Override
    public BaseAdapter<DesktopApp>.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_desktop_app, parent, false));
    }

    class ViewHolder extends BaseAdapter<DesktopApp>.ViewHolder {

        @BindView(R.id.item_desktop_app_imgview_os)
        ImageView imgViewOs;

        @BindView(R.id.item_desktop_app_txt_name)
        TextView txtName;

        @BindView(R.id.item_desktop_app_txt_ip)
        TextView txtIp;

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(DesktopApp s) {
            content = s;

            imgViewOs.setImageResource(s.getOperatingSystemIcon());
            txtName.setText(s.getName());
            txtIp.setText(s.getIp());
        }
    }
}
