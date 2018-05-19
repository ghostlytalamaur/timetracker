package mvasoft.datetimepicker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.GregorianCalendar;

import mvasoft.datetimepicker.event.DatePickerDateSelectedEvent;

public abstract class DatePickerFragment<EventType extends DatePickerDateSelectedEvent>
        extends DialogFragment {

    private static final String ARGS_INIT_DATE = "DatePickerFragment_INIT_DATE";

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private long mInitDate;


    public static Bundle makeArgs(long initUnixTime) {
        Bundle bundle = new Bundle();
        bundle.putLong(ARGS_INIT_DATE, initUnixTime);
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = new GregorianCalendar(year, month, dayOfMonth, 0, 0);
                EventBus.getDefault().post(createEvent(calendar.getTimeInMillis() / 1000));
            }
        };

        Bundle args = getArguments();
        if (args != null)
            mInitDate = args.getLong(ARGS_INIT_DATE, System.currentTimeMillis() / 1000);
        else
            mInitDate = -1;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null)
            return super.onCreateDialog(savedInstanceState);

        int year = 0;
        int month = 0;
        int dayOfMonth = 0;
        if (mInitDate >= 0) {
            Calendar c = new GregorianCalendar();
            c.setTimeInMillis(mInitDate * 1000);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        }
        //noinspection UnnecessaryLocalVariable
        DatePickerDialog dlg = new DatePickerDialog(getActivity(), mDateSetListener,
                year, month, dayOfMonth);
        return dlg;
    }

    abstract protected EventType createEvent(long unixTime);

}
