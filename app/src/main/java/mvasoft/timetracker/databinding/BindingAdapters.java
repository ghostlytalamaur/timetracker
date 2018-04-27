package mvasoft.timetracker.databinding;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;

public class BindingAdapters {

    @BindingAdapter("app:srcCompat")
    public static void setSrcCompat(FloatingActionButton fab, Drawable drawable) {
        fab.setImageDrawable(drawable);
        if (fab.getDrawable() instanceof Animatable)
            ((Animatable) fab.getDrawable()).start();
    }
}
