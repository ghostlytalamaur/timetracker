package mvasoft.timetracker.ui.common;

import android.widget.EditText;

public class EditTextUtils {

    private EditTextUtils() {}

    public static void makeEditNotEditable(EditText edit) {
        if (edit != null) {
            edit.setKeyListener(null);
            edit.setFocusable(false);
            edit.setFocusableInTouchMode(false);
            edit.setClickable(true);
        }
    }
}
