package mvasoft.datetimepicker;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import mvasoft.datetimepicker.event.TimePickerTimeSelectedEvent;

public class TimePickerFragment extends DialogFragment {

    private static final String STATE_TAG = "TimePickerFragment_DialogState";

    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private DialogConfig mState;


    public static TimePickerFragment newInstante(String eventTag, long unixTime) {
        TimePickerFragment f = new TimePickerFragment();
        f.setArguments(makeArgs(eventTag, unixTime));
        return f;
    }


    private static Bundle makeArgs(String eventTag, long initUnixTime) {
        Bundle bundle = new Bundle();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(initUnixTime * 1000);
        bundle.putParcelable(STATE_TAG,
                new DialogConfig(eventTag, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            mState = savedInstanceState.getParcelable(STATE_TAG);
        else if (getArguments() != null)
            mState = getArguments().getParcelable(STATE_TAG);
        else
            mState = new DialogConfig("", 0, 0);

        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                EventBus.getDefault().post(createEvent(mState.eventTag, hourOfDay, minute));
            }
        };
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null)
            return super.onCreateDialog(savedInstanceState);

        //noinspection UnnecessaryLocalVariable
        TimePickerDialog dlg = new TimePickerDialog(getActivity(), mTimeSetListener,
                mState.initDayOfHour, mState.initMinute, DateFormat.is24HourFormat(getContext()));
        return dlg;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_TAG, mState);
    }

    protected TimePickerTimeSelectedEvent createEvent(String eventTag, int hourOfDay, int minute) {
        return new TimePickerTimeSelectedEvent(eventTag, hourOfDay, minute);
    }


    private static class DialogConfig implements Parcelable {
        String eventTag;
        int initDayOfHour;
        int initMinute;

        DialogConfig(String eventTag, int initDayOfHour, int initMinute) {
            this.eventTag = eventTag;
            this.initDayOfHour = initDayOfHour;
            this.initMinute = initMinute;
        }

        DialogConfig(Parcel in) {
            eventTag = in.readString();
            initDayOfHour = in.readInt();
            initMinute = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(eventTag);
            dest.writeInt(initDayOfHour);
            dest.writeInt(initMinute);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DialogConfig> CREATOR = new Creator<DialogConfig>() {
            @Override
            public DialogConfig createFromParcel(Parcel in) {
                return new DialogConfig(in);
            }

            @Override
            public DialogConfig[] newArray(int size) {
                return new DialogConfig[size];
            }
        };
    }
}
