package mvasoft.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.format.DateFormat;

import java.util.Calendar;

public class TimePickerFragment extends BaseDialogFragment {

    private static final String STATE_TAG = "TimePickerFragment_DialogState";

    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private DialogConfig mState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            mState = savedInstanceState.getParcelable(STATE_TAG);
        else if (getArguments() != null)
            mState = getArguments().getParcelable(STATE_TAG);
        else
            mState = new DialogConfig(0, 0, 0, false);

        mTimeSetListener = (view, hourOfDay, minute) ->
                sendResult(new TimePickerDialogResultData(mState.requestCode, hourOfDay, minute));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null)
            return super.onCreateDialog(savedInstanceState);

        boolean is24Hour = mState.force24Hour || DateFormat.is24HourFormat(getContext());
        //noinspection UnnecessaryLocalVariable
        TimePickerDialog dlg = new TimePickerDialog(getActivity(), mTimeSetListener,
                mState.initDayOfHour, mState.initMinute, is24Hour);
        return dlg;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_TAG, mState);
    }

    private static class DialogConfig implements Parcelable {
        int requestCode;
        int initDayOfHour;
        int initMinute;
        boolean force24Hour;

        DialogConfig(int requestCode, int initDayOfHour, int initMinute, boolean force24Hour) {
            this.requestCode = requestCode;
            this.initDayOfHour = initDayOfHour;
            this.initMinute = initMinute;
            this.force24Hour = force24Hour;
        }

        DialogConfig(Parcel in) {
            requestCode = in.readInt();
            initDayOfHour = in.readInt();
            initMinute = in.readInt();
            force24Hour = in.readByte() == 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(requestCode);
            dest.writeInt(initDayOfHour);
            dest.writeInt(initMinute);
            dest.writeByte((byte) (force24Hour ? 1 : 0));
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

    public static class TimePickerDialogResultData extends DialogResultData {
        public final int hourOfDay;
        public final int minute;

        TimePickerDialogResultData(int requestCode, int hourOfDay, int minute) {
            super(requestCode);
            this.hourOfDay = hourOfDay;
            this.minute = minute;
        }
    }

    public static class Builder extends BaseDialogFragment.Builder {

        private long unixTime;
        private boolean force24Hour;

        public Builder(int requestCode) {
            super(requestCode);
        }

        @Override
        BaseDialogFragment newInstance() {
            return new TimePickerFragment();
        }

        @Override
        Bundle makeArgs() {
            Bundle b = new Bundle();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(unixTime * 1000);

            DialogConfig config = new DialogConfig(requestCode,
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    force24Hour);

            b.putParcelable(STATE_TAG, config);

            return b;
        }

        public Builder withUnixTime(long unixTime) {
            this.unixTime = unixTime;
            return this;
        }

        public Builder setForce24Hour(boolean isForce) {
            force24Hour = isForce;
            return this;
        }
    }
}
