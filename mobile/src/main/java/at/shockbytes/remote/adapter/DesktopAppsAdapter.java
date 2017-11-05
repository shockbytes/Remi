package at.shockbytes.remote.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import at.shockbytes.remote.R;
import at.shockbytes.remote.network.model.DesktopApp;
import at.shockbytes.remote.util.RemiUtils;
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

    @Override
    public void addEntityAtFirst(DesktopApp entity) {
        // Only add entity if it's not already added
        int location = this.getLocation(this.data, entity);
        if (location < 0) {
            super.addEntityAtFirst(entity);
        }
    }

    class ViewHolder extends BaseAdapter<DesktopApp>.ViewHolder {

        @BindView(R.id.item_desktop_app_imgview_os)
        ImageView imgViewOs;

        @BindView(R.id.item_desktop_app_txt_name)
        TextView txtName;

        @BindView(R.id.item_desktop_app_txt_ip)
        TextView txtIp;

        @BindView(R.id.item_desktop_app_txt_trusted_connection)
        TextView txtTrusted;

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(DesktopApp s) {
            content = s;

            imgViewOs.setImageResource(RemiUtils.getOperatingSystemIcon(s.getOs()));
            txtName.setText(s.getName());
            txtIp.setText(s.getIp());

            if (s.isTrusted()) {
                txtTrusted.setText(R.string.desktop_apps_trusted);
                txtTrusted.setTextColor(Color.WHITE);
            } else {
                txtTrusted.setText(R.string.desktop_apps_untrusted);
                txtTrusted.setTextColor(ContextCompat.getColor(context, R.color.error_light));
            }

        }
    }
}
