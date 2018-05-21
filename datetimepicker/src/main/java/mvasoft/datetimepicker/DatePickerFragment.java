package mvasoft.datetimepicker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import mvasoft.datetimepicker.event.DatePickerDateSelectedEvent;

public class DatePickerFragment extends DialogFragment {

    private static final String STATE_TAG = "TimePickerFragment_DialogState";

    private DatePickerDialog.OnDateSetListener mDateListener;
    private DialogConfig mState;


    public static DatePickerFragment newInstante(String eventTag, long unixTime) {
        DatePickerFragment f = new DatePickerFragment();
        f.setArguments(makeArgs(eventTag, unixTime));
        return f;
    }


    private static Bundle makeArgs(String eventTag, long initUnixTime) {
        Bundle bundle = new Bundle();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(initUnixTime * 1000);
        bundle.putParcelable(STATE_TAG,
                new DialogConfig(eventTag, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)));
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
            mState = new DialogConfig("", 0, 0, 0);

        mDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                EventBus.getDefault().post(createEvent(mState.eventTag, year, month + 1, dayOfMonth));
            }
        };
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_TAG, mState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null)
            return super.onCreateDialog(savedInstanceState);

        //noinspection UnnecessaryLocalVariable
        DatePickerDialog dlg = new DatePickerDialog(getActivity(), mDateListener,
                mState.initYear, mState.initMonth, mState.initDayOfMonth);
        return dlg;
    }

    protected DatePickerDateSelectedEvent createEvent(String eventTag,
                                                      int year, int month, int dayOfMonth) {
        return new DatePickerDateSelectedEvent(eventTag, year, month, dayOfMonth);
    }


    private static class DialogConfig implements Parcelable {
        String eventTag;
        int initYear;
        int initMonth;
        int initDayOfMonth;

        DialogConfig(String eventTag, int year, int month, int dayOfMonth) {
            this.eventTag = eventTag;
            this.initYear = year;
            this.initMonth = month;
            this.initDayOfMonth = dayOfMonth;
        }

        DialogConfig(Parcel in) {
            eventTag = in.readString();
            initYear = in.readInt();
            initMonth = in.readInt();
            initDayOfMonth = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(eventTag);
            dest.writeInt(initYear);
            dest.writeInt(initMonth);
            dest.writeInt(initDayOfMonth);
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
