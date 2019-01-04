package mvasoft.timetracker.ui.common;

import androidx.annotation.DrawableRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public interface FabProvider {
    void setImageResource(@DrawableRes int id);
    void setVisibility(int visibility);
    void setClickListener(FloatingActionButton.OnClickListener listener);
    void removeClickListener(FloatingActionButton.OnClickListener listener);
}
