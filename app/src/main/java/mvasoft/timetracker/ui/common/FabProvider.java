package mvasoft.timetracker.ui.common;

import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;

public interface FabProvider {
    void setImageResource(@DrawableRes int id);
    void setVisibility(int visibility);
    void setClickListener(FloatingActionButton.OnClickListener listener);
    void removeClickListener(FloatingActionButton.OnClickListener listener);
}
