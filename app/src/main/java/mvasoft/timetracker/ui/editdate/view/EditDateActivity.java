package mvasoft.timetracker.ui.editdate.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import mvasoft.timetracker.R;
import mvasoft.timetracker.data.event.DayDescriptionSavedEvent;
import mvasoft.timetracker.ui.common.EventBusSupportActivity;

public class EditDateActivity extends EventBusSupportActivity {

    public static Bundle makeArgs(long date) {
        Bundle res = new Bundle();
        res.putLong("EXTRA_DATE", date);
        return res;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_date);
        EditDateFragment fragment = (EditDateFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        Bundle extras = getIntent().getExtras();
        if (fragment != null && extras != null)
            fragment.setDate(extras.getLong("EXTRA_DATE", 0));
    }

    @Override
    protected boolean shouldRegisterToEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDayDescriptionSavedEvent(DayDescriptionSavedEvent e) {
        if (e.wasSaved)
            Toast.makeText(this, R.string.msg_day_description_saved, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, R.string.msg_day_description_unable_save, Toast.LENGTH_SHORT).show();
    }
}
