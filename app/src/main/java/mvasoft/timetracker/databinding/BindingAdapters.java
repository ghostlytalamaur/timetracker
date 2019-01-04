package mvasoft.timetracker.databinding;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseMethod;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;

import com.drextended.actionhandler.listener.ActionClickListener;


public class BindingAdapters {

    @BindingAdapter("srcCompat")
    public static void setSrcCompat(FloatingActionButton fab, Drawable drawable) {
        fab.setImageDrawable(drawable);
        if (fab.getDrawable() instanceof Animatable)
            ((Animatable) fab.getDrawable()).start();
    }

    @BindingAdapter("srcCompat")
    public static void setSrcCompat(ImageView view, Drawable drawable) {
        view.setImageDrawable(drawable);
        if (view.getDrawable() instanceof Animatable)
            ((Animatable) view.getDrawable()).start();
    }

    /**
     * Binding adapter to assign an action to a view using android data binding approach.
     * Sample:
     * <pre>
     * &lt;Button
     *     android:layout_width="wrap_content"
     *     android:layout_height="wrap_content"
     *
     *     android:actionHandler="@{someActionHandler}"
     *     android:actionType='@{"send_message"}'
     *     android:actionTypeLongClick='@{"show_menu"}'
     *     android:model="@{user}"
     *
     *     android:text="@string/my_button_text"/&gt;
     * </pre>
     *
     * @param view                The View to bind an action
     * @param actionHandler       The action handler which will handle an action
     * @param actionType          The action type, which will be handled on view clicked
     * @param actionTypeLongClick The action type, which will be handled on view long clicked
     * @param model               The model which will be handled
     */
    @BindingAdapter(
            value = {"actionHandler", "actionType", "actionTypeLongClick", "model"},
            requireAll = false
    )
    public static void setActionHandler(final View view, final ActionClickListener actionHandler, final String actionType, final String actionTypeLongClick, final Object model) {
        if (actionHandler != null) {
            if (actionType != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        actionHandler.onActionClick(view, actionType, model);
                    }
                });
            }

            if (actionTypeLongClick != null) {
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        actionHandler.onActionClick(view, actionTypeLongClick, model);
                        return true;
                    }
                });
            }
        }
    }

    @BindingAdapter(value = {"activated"})
    public static void setActivated(final View view, boolean isActivated) {
        if (view.isActivated() == isActivated)
            return;

        view.setActivated(isActivated);
    }

    @InverseMethod("fromString")
    public static String toString(Long oldValue, Long value) {
        if (value == null)
            return "0";
        else
            return String.valueOf(value);
    }

    public static Long fromString(Long defValue, String str) {
        try {
            return Long.valueOf(str);
        }
        catch (NumberFormatException e) {
            return defValue;
        }
    }
}
