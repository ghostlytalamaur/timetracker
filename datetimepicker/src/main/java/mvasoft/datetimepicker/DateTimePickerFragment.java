package mvasoft.datetimepicker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimePickerFragment extends DialogFragment {

    public static final String ARGS_DATE  = "extra_date";
    private static final String ARGS_TITLE = "extra_title";

    private long mDate;

    private int mYear, mMonth, mDay, mHour, mMin;

    public static DateTimePickerFragment newInstance(long date, String title) {
        Bundle args = new Bundle();
        args.putLong(ARGS_DATE, date);
        args.putString(ARGS_TITLE, title);

        DateTimePickerFragment f = new DateTimePickerFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long date = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMin).getTimeInMillis();
        outState.putLong(ARGS_DATE, date);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            mDate = savedInstanceState.getLong(ARGS_DATE);
        else
            mDate = getArguments().getLong(ARGS_DATE);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate);

        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);
        mHour = cal.get(Calendar.HOUR_OF_DAY);
        mMin = cal.get(Calendar.MINUTE);

        View v = getActivity().getLayoutInflater().inflate(R.layout.fragment_date_dialog, null);

        TimePicker timePicker = v.findViewById(R.id.dialog_date_timePicker);

        timePicker.setSaveFromParentEnabled(false);
        timePicker.setSaveEnabled(true);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(mHour);
        timePicker.setCurrentMinute(mMin);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMin = minute;
            }
        });

        DatePicker datePicker = v.findViewById(R.id.dialog_date_datePicker);
        datePicker.setSaveFromParentEnabled(false);
        datePicker.setSaveEnabled(true);
        datePicker.init(mYear, mMonth, mDay, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
            }
        });

        DialogInterface.OnClickListener onOkClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(AppCompatActivity.RESULT_OK);
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(getArguments().getString(ARGS_TITLE));
        builder.setPositiveButton(android.R.string.ok, onOkClickListener);
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private void sendResult(int resCode) {
        if (getTargetFragment() == null)
            return;

        mDate = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMin).getTimeInMillis();
        Intent i = new Intent();
        i.putExtra(ARGS_DATE, mDate);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resCode, i);
    }
}
