package mvasoft.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;


public class DatePickerFragment extends BaseDialogFragment {

    private static final String STATE_TAG = "TimePickerFragment_DialogState";

    private DatePickerDialog.OnDateSetListener mDateListener;
    private DialogConfig mState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            mState = savedInstanceState.getParcelable(STATE_TAG);
        else if (getArguments() != null)
            mState = getArguments().getParcelable(STATE_TAG);
        else
            mState = new DialogConfig(0, 0, 0, 0);

        mDateListener = (view, year, month, dayOfMonth) ->
                sendResult(new DatePickerDialogResultData(mState.requestCode,
                                year, month + 1, dayOfMonth));
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

    private static class DialogConfig implements Parcelable {
        int requestCode;
        int initYear;
        int initMonth;
        int initDayOfMonth;

        DialogConfig(int requestCode, int year, int month, int dayOfMonth) {
            this.requestCode = requestCode;
            this.initYear = year;
            this.initMonth = month;
            this.initDayOfMonth = dayOfMonth;
        }

        DialogConfig(Parcel in) {
            requestCode = in.readInt();
            initYear = in.readInt();
            initMonth = in.readInt();
            initDayOfMonth = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(requestCode);
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

    public static class Builder extends BaseDialogFragment.Builder {

        private long unixTime;

        public Builder(int requestCode) {
            super(requestCode);
        }

        @Override
        BaseDialogFragment newInstance() {
            return new DatePickerFragment();
        }

        @Override
        Bundle makeArgs() {
            Bundle b = new Bundle();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(unixTime * 1000);
            b.putParcelable(STATE_TAG,
                    new DialogConfig(requestCode, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                            c.get(Calendar.DAY_OF_MONTH)));
            return b;
        }

        public Builder withUnixTime(long unixTime) {
            this.unixTime = unixTime;
            return this;
        }
    }

    public static class DatePickerDialogResultData extends DialogResultData {
        public final int year;
        public final int month;
        public final int dayOfMonth;

        DatePickerDialogResultData(int requestCode, int year, int month, int dayOfMonth) {
            super(requestCode);
            this.year = year;
            this.month = month;
            this.dayOfMonth = dayOfMonth;
        }
    }
}
