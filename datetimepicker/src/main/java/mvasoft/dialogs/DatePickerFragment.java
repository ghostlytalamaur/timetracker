package mvasoft.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
            mState = new DialogConfig(0, SELECTION_MODE_SINGLE, null);

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
        if (mState.selectedDays != null) {
            mCalendarView.clearSelection();
            for (CalendarDay day : mState.selectedDays)
                mCalendarView.setDateSelected(day, true);
        }

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
        int selectionMode;
        List<CalendarDay> selectedDays;

        DialogConfig(int requestCode, int selectionMode, List<CalendarDay> selectedDays) {
            this.requestCode = requestCode;
            this.selectionMode = selectionMode;
            this.selectedDays = selectedDays;
        }

        DialogConfig(Parcel in) {
            requestCode = in.readInt();
            selectionMode = in.readInt();
            if (in.readInt() != -1) {
                selectedDays = new ArrayList<>();
                in.readTypedList(selectedDays, CalendarDay.CREATOR);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(requestCode);
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

        public static Parcelable from(Builder builder) {
            DateTime start = new DateTime(builder.startDate * 1000);
            DateTime end = new DateTime(builder.endDate * 1000);
            int daysCount = Days.daysBetween(start, end).getDays() + 1;
            ArrayList<CalendarDay> selDays = new ArrayList<>(daysCount);
            for (int i = 0; i < daysCount; i++) {
                selDays.add(CalendarDay.from(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
                start = start.plusDays(1);
            }

            return new DialogConfig(builder.requestCode, builder.selectionMode, selDays);
        }
    }

    public static class Builder extends BaseDialogFragment.Builder {

        private long startDate;
        private long endDate;
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
            b.putParcelable(STATE_TAG, DialogConfig.from(this));
            return b;
        }

        public Builder withUnixTime(long unixTime) {
            this.startDate = unixTime;
            return this;
        }

        public Builder setStartDate(long unixTime) {
            this.startDate = unixTime;
            return this;
        }

        public Builder setEndDate(long unixTime) {
            this.endDate = unixTime;
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
