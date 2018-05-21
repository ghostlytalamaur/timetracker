package mvasoft.datetimepicker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public class DateTimePickerFragment extends DialogFragment {

    public static final String ARGS_DATE  = "extra_date";
    private static final String STATE_TAG = "dialog_state";

    private DialogState mState;

    public static DateTimePickerFragment newInstance(long unixTime, String title) {
        Bundle args = new Bundle();
        args.putParcelable(STATE_TAG, new DialogState(unixTime, ""));
        DateTimePickerFragment f = new DateTimePickerFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            mState = savedInstanceState.getParcelable(STATE_TAG);
        else if (getArguments() != null)
            mState = getArguments().getParcelable(STATE_TAG);
        else
            mState = new DialogState(System.currentTimeMillis() / 1000, "Select date and time");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_TAG, mState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Context context = getActivity();
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_date_dialog, null);

        final ViewPager viewPager = v.findViewById(R.id.viewPager);

        viewPager.setAdapter(new PagerAdapter() {

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                return viewPager.getChildAt(position);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0: return "Date";
                    case 1: return "Time";
                }
                return super.getPageTitle(position);
            }

            @Override
            public int getCount() {
                return viewPager.getChildCount();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        });

        viewPager.setCurrentItem(0);

        TabLayout tabLayout = v.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        TimePicker timePicker = v.findViewById(R.id.dialog_date_timePicker);

        timePicker.setSaveFromParentEnabled(false);
        timePicker.setSaveEnabled(true);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(mState.mCalendar.get(Calendar.HOUR));
        timePicker.setCurrentMinute(mState.mCalendar.get(Calendar.MINUTE));
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mState.mCalendar.set(Calendar.HOUR, hourOfDay);
                mState.mCalendar.set(Calendar.MINUTE, minute);
            }
        });

        DatePicker datePicker = v.findViewById(R.id.dialog_date_datePicker);
        datePicker.setSaveFromParentEnabled(false);
        datePicker.setSaveEnabled(true);
        datePicker.init(mState.mCalendar.get(Calendar.YEAR),
                mState.mCalendar.get(Calendar.MONTH),
                mState.mCalendar.get(Calendar.DAY_OF_MONTH),

                new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mState.mCalendar.set(Calendar.YEAR, year);
                mState.mCalendar.set(Calendar.MONTH, monthOfYear);
                mState.mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        });

        DialogInterface.OnClickListener onOkClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(AppCompatActivity.RESULT_OK);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);
        builder.setTitle(mState.mTitle);
        builder.setPositiveButton(android.R.string.ok, onOkClickListener);
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private void sendResult(int resCode) {
        if (getTargetFragment() == null)
            return;

        Intent i = new Intent();
        i.putExtra(ARGS_DATE, mState.getUnixTime());
        getTargetFragment().onActivityResult(getTargetRequestCode(), resCode, i);
    }

    private static class DialogState implements Parcelable {
        final Calendar mCalendar;
        final String mTitle;

        private DialogState(long unixTime, String title) {
            mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(unixTime * 1000);
            mTitle = title;
        }

        DialogState(Parcel in) {
            long unixTime = in.readLong();
            mCalendar = Calendar.getInstance();
            mCalendar.setTimeInMillis(unixTime * 1000);
            mTitle = in.readString();

        }

        long getUnixTime() {
            return mCalendar.getTimeInMillis() / 1000;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(getUnixTime());
            dest.writeString(mTitle);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DialogState> CREATOR = new Creator<DialogState>() {
            @Override
            public DialogState createFromParcel(Parcel in) {
                return new DialogState(in);
            }

            @Override
            public DialogState[] newArray(int size) {
                return new DialogState[size];
            }
        };
    }

    private static class DialogAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Date";
                case 1: return "Time";
            }
            return super.getPageTitle(position);
        }
    }
}
