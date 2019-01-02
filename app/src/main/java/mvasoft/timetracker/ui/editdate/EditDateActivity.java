package mvasoft.timetracker.ui.editdate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import mvasoft.timetracker.R;
import mvasoft.timetracker.data.event.DayDescriptionSavedEvent;
import mvasoft.timetracker.databinding.ActivityEditDateBinding;
import mvasoft.timetracker.ui.common.BindingSupportActivity;

public class EditDateActivity extends
        BindingSupportActivity<ActivityEditDateBinding, EditDateActivityViewModel> implements
        DatesViewFragment.OnDateSelected {

    public static Bundle makeArgs(long date) {
        Bundle res = new Bundle();
        res.putLong("EXTRA_DATE", date);
        return res;
    }

    @Override
    public void onDateSelected(long unixTime) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragment_container, EditDateFragment.makeInstance(unixTime))
                .commit();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = new DatesViewFragment();
        fm.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_date;
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
