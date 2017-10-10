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
import at.shockbytes.remote.network.model.RemiFile;
import at.shockbytes.remote.util.RemiUtils;
import at.shockbytes.util.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */

public class FilesAdapter extends BaseAdapter<RemiFile> {

    public interface OnOverflowMenuItemClickListener<T> {

        void onOverflowMenuItemClicked(int itemId, T content);

    }

    private OnOverflowMenuItemClickListener<RemiFile> listener;

    private boolean isFileTransferPermitted;

    public FilesAdapter(Context cxt, List<RemiFile> data, boolean isFileTransferPermitted) {
        super(cxt, data);
        this.isFileTransferPermitted = isFileTransferPermitted;
    }

    public void setOnOverflowMenuItemClickListener(OnOverflowMenuItemClickListener<RemiFile> listener) {
        this.listener = listener;
    }

    @Override
    public BaseAdapter<RemiFile>.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.item_app_and_file, parent, false));
    }

    class ViewHolder extends BaseAdapter<RemiFile>.ViewHolder
            implements PopupMenu.OnMenuItemClickListener {

        @BindView(R.id.item_app_and_file_txt_name)
        TextView txtApp;

        @BindView(R.id.item_app_and_file_imgbtn_overflow)
        ImageButton imgBtnOverflow;

        private PopupMenu popupMenu;

        ViewHolder(View itemView) {
            super(itemView);

            popupMenu = new PopupMenu(context, imgBtnOverflow);
            popupMenu.getMenuInflater().inflate(R.menu.popup_files_item, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(this);
            tryShowIconsInPopupMenu(popupMenu);
        }

        @Override
        public void bind(RemiFile file) {
            content = file;
            hidePopupIconsIfNecessary();

            txtApp.setText(file.getName());
            txtApp.setCompoundDrawablesWithIntrinsicBounds(
                    RemiUtils.getDrawableResourceForFileType(file), 0, 0, 0);
        }

        @OnClick(R.id.item_app_and_file_imgbtn_overflow)
        void onClickOverflow() {
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (listener != null) {
                listener.onOverflowMenuItemClicked(item.getItemId(), content);
                return true;
            }
            return false;
        }

        private void hidePopupIconsIfNecessary() {
            // Only non directory files can be transferred to the phone
            imgBtnOverflow.setVisibility(content.isDirectory() ? View.GONE : View.VISIBLE);
            // Only executables can be added to apps
            popupMenu.getMenu().getItem(0).setVisible(content.isExecutable());
            // Sending to phone must be permitted by the desktop
            popupMenu.getMenu().getItem(1).setVisible(isFileTransferPermitted);
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
    }
}
