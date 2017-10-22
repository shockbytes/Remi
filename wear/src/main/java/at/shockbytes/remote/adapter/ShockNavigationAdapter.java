package at.shockbytes.remote.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;

import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 23.03.2017.
 */

public class ShockNavigationAdapter
        extends WearableNavigationDrawerView.WearableNavigationDrawerAdapter {

    private List<NavigationItem> items;

    private Context context;

    public ShockNavigationAdapter(@NonNull Context context,
                                  List<NavigationItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public String getItemText(int i) {
        return context.getString(items.get(i).text);
    }

    @Override
    public Drawable getItemDrawable(int i) {
        return ContextCompat.getDrawable(context, items.get(i).drawable);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public static class NavigationItem {

        protected int text;
        protected int drawable;

        public NavigationItem(@StringRes int text, @DrawableRes int drawable) {
            this.text = text;
            this.drawable = drawable;
        }

    }
}
