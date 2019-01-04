package mvasoft.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mvasoft.datetimepicker.R;


public class DatePickerFragment extends BaseDialogFragment {

    public static final int SELECTION_MODE_SINGLE = MaterialCalendarView.SELECTION_MODE_SINGLE;
    public static final int SELECTION_MODE_RANGE = MaterialCalendarView.SELECTION_MODE_RANGE;

    private static final String STATE_TAG = "TimePickerFragment_DialogState";

    private AlertDialog.OnClickListener mDialogOkListener;
    private DialogConfig mState;
    private MaterialCalendarView mCalendarView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            mState = savedInstanceState.getParcelable(STATE_TAG);
        else if (getArguments() != null)
            mState = getArguments().getParcelable(STATE_TAG);
        else
            mState = new DialogConfig(0, 0, 1, 0, SELECTION_MODE_SINGLE, null);

        mDialogOkListener = (dialog, which) -> {
            DatePickerDialogResultData data = getResultData();
            sendResult(data);
            dialog.dismiss();
        };
    }

    private DatePickerDialogResultData getResultData() {
        return new DatePickerDialogResultData(mState.requestCode, mCalendarView.getSelectedDates());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mState.selectedDays = mCalendarView.getSelectedDates();
        outState.putParcelable(STATE_TAG, mState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (mCalendarView != null)
            mCalendarView.setSelectionMode(mState.selectionMode);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null)
            return super.onCreateDialog(savedInstanceState);


        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_datepicker, null);
        Dialog dlg = new AlertDialog.Builder(getContext())
                .setView(view)
                .setPositiveButton(android.R.string.ok, mDialogOkListener)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .create();

        mCalendarView = view.findViewById(R.id.calendarView);
        mCalendarView.setSelectionMode(mState.selectionMode);
        mCalendarView.setShowOtherDates(MaterialCalendarView.SHOW_OTHER_MONTHS);
        mCalendarView.setSelectedDate(CalendarDay.from(mState.initYear, mState.initMonth, mState.initDayOfMonth));

        return dlg;
    }

    @Override
    public void onDestroyView() {
        mCalendarView = null;
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mState != null && mState.selectedDays != null) {
            mCalendarView.clearSelection();
            for (CalendarDay day : mState.selectedDays)
                mCalendarView.setDateSelected(day, true);
        }

    }

    private static class DialogConfig implements Parcelable {
        int requestCode;
        int initYear;
        int initMonth;
        int initDayOfMonth;
        int selectionMode;
        List<CalendarDay> selectedDays;

        DialogConfig(int requestCode, int year, int month, int dayOfMonth, int selectionMode, List<CalendarDay> selectedDays) {
            this.requestCode = requestCode;
            this.initYear = year;
            this.initMonth = month;
            this.initDayOfMonth = dayOfMonth;
            this.selectionMode = selectionMode;
            this.selectedDays = selectedDays;
        }

        DialogConfig(Parcel in) {
            requestCode = in.readInt();
            initYear = in.readInt();
            initMonth = in.readInt();
            initDayOfMonth = in.readInt();
            selectionMode = in.readInt();
            if (in.readInt() != -1) {
                selectedDays = new ArrayList<>();
                in.readTypedList(selectedDays, CalendarDay.CREATOR);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(requestCode);
            dest.writeInt(initYear);
            dest.writeInt(initMonth);
            dest.writeInt(initDayOfMonth);
            dest.writeInt(selectionMode);
            if (selectedDays != null) {
                dest.writeInt(1);
                dest.writeTypedList(selectedDays);
            }
            else
                dest.writeInt(-1);
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
        private int selectionMode;

        public Builder(int requestCode) {
            super(requestCode);
            selectionMode = SELECTION_MODE_SINGLE;
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
                    new DialogConfig(requestCode, c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
                            c.get(Calendar.DAY_OF_MONTH), selectionMode, null));
            return b;
        }

        public Builder withUnixTime(long unixTime) {
            this.unixTime = unixTime;
            return this;
        }

        public Builder setSelectionMode(int mode) {
            selectionMode = mode;
            return this;
        }
    }

    public static class DatePickerDialogResultData extends DialogResultData {

        private final List<CalendarDay> mDays;

        DatePickerDialogResultData(int requestCode, List<CalendarDay> days) {
            super(requestCode);
            mDays = days;
        }

        public CalendarDay getDay() {
            if (mDays != null && !mDays.isEmpty())
                return mDays.get(0);
            else
                return null;
        }

        public List<CalendarDay> getDays() {
            return mDays;
        }

    }
}
