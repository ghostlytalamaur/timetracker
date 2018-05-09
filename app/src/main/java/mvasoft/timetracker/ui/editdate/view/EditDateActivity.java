package mvasoft.timetracker.ui.editdate.view;

import android.os.Bundle;
import android.support.annotation.Nullable;

import dagger.android.support.DaggerAppCompatActivity;
import mvasoft.timetracker.R;

public class EditDateActivity extends DaggerAppCompatActivity {

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
}
