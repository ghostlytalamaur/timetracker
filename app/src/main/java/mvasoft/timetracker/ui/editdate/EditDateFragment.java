package mvasoft.timetracker.ui.editdate;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import mvasoft.dialogs.DialogResultData;
import mvasoft.dialogs.DialogResultListener;
import mvasoft.dialogs.TimePickerFragment;
import mvasoft.timetracker.BR;
import mvasoft.timetracker.R;
import mvasoft.timetracker.core.Injectable;
import mvasoft.timetracker.events.DayDescriptionSavedEvent;
import mvasoft.timetracker.databinding.FragmentEditDateBinding;
import mvasoft.timetracker.ui.common.BindingSupportFragment;
import mvasoft.timetracker.ui.common.EditTextUtils;

public class EditDateFragment extends
        BindingSupportFragment<FragmentEditDateBinding, EditDateViewModel> implements
        DialogResultListener, Injectable {

    private static final int DLG_REQUEST_TARGET_TIME = 1;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null)
            setDate(EditDateFragmentArgs.fromBundle(getArguments()).getDayUnixTime());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        EditTextUtils.makeEditNotEditable(getBinding().edtTarget);
        getBinding().edtTarget.setOnClickListener(v1 -> selectTargetTime());
        getBinding().swWorkDay.setOnCheckedChangeListener((buttonView, isChecked) ->
                getViewModel().setIsWorkingDay(isChecked));

        getViewModel().getIsChanged().observe(this, isChanged -> {
            if (getActivity() != null)
                getActivity().invalidateOptionsMenu();
        });

        return v;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_date, menu);
        MenuItem item = menu.findItem(R.id.menu_save);
        if (item != null)
            item.setEnabled(
                    getViewModel().getIsChanged().getValue() != null &&
                    getViewModel().getIsChanged().getValue());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                getViewModel().save();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected EditDateViewModel onCreateViewModel() {
        return ViewModelProviders.of(this, viewModelFactory).get(EditDateViewModel.class);
    }

    protected @IdRes
    int getModelVariableId() {
        return BR.view_model;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_edit_date;
    }

    @Override
    public void onDialogResult(@NonNull DialogResultData data) {
        switch (data.requestCode) {
            case DLG_REQUEST_TARGET_TIME: {
                TimePickerFragment.TimePickerDialogResultData d = (TimePickerFragment.TimePickerDialogResultData) data;
                getViewModel().setTargetTime(d.hourOfDay * 60 + d.minute);
                break;
            }
        }
    }

    @Override
    protected boolean shouldRegisterToEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDayDescriptionSavedEvent(DayDescriptionSavedEvent e) {
        if (e.wasSaved)
            Toast.makeText(getContext(), R.string.msg_day_description_saved, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), R.string.msg_day_description_unable_save, Toast.LENGTH_SHORT).show();
    }


    public void setDate(long date) {
        getViewModel().setDate(date);
    }

    private void selectTargetTime() {
        long unixTime = getViewModel().getDayDescription().getTargetDuration() * 60; // getCurTargetMin() * 60;
        new TimePickerFragment.Builder(DLG_REQUEST_TARGET_TIME)
                .withUnixTime(unixTime)
                .setForce24Hour(true)
                .show(this, "EditDateTargetTimePicker");
    }
}
