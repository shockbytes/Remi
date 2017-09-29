package at.shockbytes.remote.adapter;

import android.content.Context;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.List;

import at.shockbytes.remote.R;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public class AppsAdapter extends BaseAdapter<String> {

    public interface OnOverflowMenuItemClickListener<T> {

        void onOverflowMenuItemClicked(int itemId, T content);

    }

    private OnOverflowMenuItemClickListener<String> listener;

    public AppsAdapter(Context cxt, List<String> data) {
        super(cxt, data);
    }

    public void setOnOverflowMenuItemClickListener(OnOverflowMenuItemClickListener<String> listener) {
        this.listener = listener;
    }

    @Override
    public BaseAdapter<String>.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_app, parent, false));
    }

    class ViewHolder extends BaseAdapter<String>.ViewHolder
            implements PopupMenu.OnMenuItemClickListener {

        @BindView(R.id.item_app_txt_name)
        TextView txtApp;

        @BindView(R.id.item_app_imgbtn_overflow)
        ImageButton imgBtnOverflow;

        private PopupMenu popupMenu;

        ViewHolder(View itemView) {
            super(itemView);

            popupMenu = new PopupMenu(context, imgBtnOverflow);
            popupMenu.getMenuInflater().inflate(R.menu.popup_apps_item, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(this);
            tryShowIconsInPopupMenu(popupMenu);
        }

        @Override
        public void bind(String s) {
            content = s;
            txtApp.setText(s);
            txtApp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_app, 0, 0, 0);
        }

        @OnClick(R.id.item_app_imgbtn_overflow)
        void onClickOverflow() {
            popupMenu.show();
        }

        private void tryShowIconsInPopupMenu(PopupMenu menu) {

            try {
                Field fieldPopup = menu.getClass().getDeclaredField("mPopup");
                fieldPopup.setAccessible(true);
                MenuPopupHelper popup = (MenuPopupHelper) fieldPopup.get(menu);
                popup.setForceShowIcon(true);
            } catch (Exception e) {
                Log.d("Remi", "Cannot force to show icons in PopupMenu");
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (listener != null) {
                listener.onOverflowMenuItemClicked(item.getItemId(), content);
                return true;
            }
            return false;
        }
    }
}
