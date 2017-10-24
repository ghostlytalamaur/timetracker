package mvasoft.timetracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import mvasoft.datetimepicker.DatePickerFragment;
import mvasoft.timetracker.data.DatabaseDescription.SessionDescription;

import static mvasoft.timetracker.Consts.LOADER_ID_SESSION;


public class SessionEditFragment extends Fragment {

    private static final String ARGS_SESSION_ID = "session_id";
    private static final int REQUEST_START_TIME = 1;
    private static final int REQUEST_END_TIME   = 2;
    private static final String STATE_ID         = "session_id";
    private static final String STATE_START_TIME = "start_time";
    private static final String STATE_END_TIME   = "end_time";
    private static final String STATE_ORIGINAL_START_TIME = "original_start";
    private static final String STATE_ORIGINAL_END_TIME = "original_end";
    private long mSessionId;
    private long mStartTime;
    private long mEndTime;
    private TextView mTvStart;
    private TextView mTvEnd;
    private PeriodFormatter mPeriodFormatter;
    private DateTimeFormatter mDateTimeFormatter;
    private TextView mTvDuration;
    private SessionLoaderCallbacks mLoaderCallbacks;
    private Cursor mCursor;
    private FloatingActionButton mFab;
    private long mOriginalStartTime;
    private long mOriginalEndTime;

    static public SessionEditFragment newInstance(long sessionId) {
        
        Bundle args = new Bundle();
        args.putLong(ARGS_SESSION_ID, sessionId);
        
        SessionEditFragment fragment = new SessionEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            long newDateTime;
            switch (requestCode) {
                case REQUEST_START_TIME:
                    newDateTime = data.getLongExtra(DatePickerFragment.ARGS_DATE,
                            getDisplayStartTime() / 1000) / 1000;
                    if (newDateTime != getDisplayStartTime()) {
                        mStartTime = newDateTime;
                        setFabVisibility(true);
                    }
                    break;
                case REQUEST_END_TIME:
                    newDateTime = data.getLongExtra(DatePickerFragment.ARGS_DATE,
                            getDisplayEndTime() / 1000) / 1000;
                    if (newDateTime != getDisplayEndTime()) {
                        mEndTime = newDateTime;
                        setFabVisibility(true);
                    }
                    break;
            }
            updateUI();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPeriodFormatter = new PeriodFormatterBuilder().
                printZeroAlways().
                minimumPrintedDigits(2).
                appendHours().
                appendSeparator(":").
                printZeroAlways().
                minimumPrintedDigits(2).
                appendMinutes().
                appendSeparator(":").
                printZeroAlways().
                minimumPrintedDigits(2).
                appendSeconds().
                toFormatter();

        mDateTimeFormatter = new DateTimeFormatterBuilder().
                appendDayOfWeekText().
                appendLiteral(", ").
                appendDayOfMonth(2).
                appendLiteral(" ").
                appendMonthOfYearText().
                appendLiteral(" ").
                appendYear(4, 4).
                appendLiteral(" ").
                appendHourOfDay(2).
                appendLiteral(":").
                appendMinuteOfHour(2).
                toFormatter();

        mStartTime = -1;
        mEndTime = -1;

        if (savedInstanceState != null)
            restoreState(savedInstanceState);
        else {
            Bundle args = getArguments();
            mSessionId = args.getLong(ARGS_SESSION_ID);
        }

        mLoaderCallbacks = new SessionLoaderCallbacks();
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID_SESSION, null, mLoaderCallbacks);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_session_edit, container, false);

        mFab = fragmentView.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSession();
            }
        });
        setFabVisibility(false);

        mTvStart = fragmentView.findViewById(R.id.tvStart);
        mTvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDateTime(getDisplayStartTime(), REQUEST_START_TIME);
            }
        });

        mTvEnd = fragmentView.findViewById(R.id.tvEnd);
        mTvEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDateTime(getDisplayEndTime(), REQUEST_END_TIME);
            }
        });

        mTvDuration = fragmentView.findViewById(R.id.tvDuration);
        updateUI();

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_ID, mSessionId);
        outState.putLong(STATE_ORIGINAL_START_TIME, mOriginalStartTime);
        outState.putLong(STATE_ORIGINAL_END_TIME, mOriginalEndTime);
        outState.putLong(STATE_START_TIME, mStartTime);
        outState.putLong(STATE_END_TIME, mEndTime);
    }


    private void restoreState(Bundle state) {
        mSessionId = state.getLong(STATE_ID);
        mOriginalStartTime = state.getLong(STATE_ORIGINAL_START_TIME);
        mOriginalEndTime = state.getLong(STATE_ORIGINAL_END_TIME);
        mStartTime = state.getLong(STATE_START_TIME);
        mEndTime = state.getLong(STATE_END_TIME);
    }


    private void saveSession() {
        SessionHelper helper = new SessionHelper(getContext());
        boolean isSaved = helper.updateSession(mSessionId, getDisplayStartTime(), getDisplayEndTime());
        setFabVisibility(!isSaved);
        if (isSaved)
            Snackbar.make(mFab, R.string.session_saved, Snackbar.LENGTH_LONG);
        else
            Snackbar.make(mFab, R.string.session_unable_save, Snackbar.LENGTH_LONG);
    }

    private void setFabVisibility(boolean isVisible) {
        if (isVisible)
            mFab.setVisibility(View.VISIBLE);
        else
            mFab.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        swapCursor(null);
        getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID_SESSION);
        super.onDestroy();
    }

    private void editDateTime(long dateTime, int requestCode) {
        DatePickerFragment dlg = DatePickerFragment.newInstance(dateTime * 1000, "");
        dlg.setTargetFragment(this, requestCode);
        dlg.show(getFragmentManager(), "dialog_date");
    }

    private void updateUI() {
        if (mTvStart != null)
            mTvStart.setText(mDateTimeFormatter.print(new DateTime(getDisplayStartTime() * 1000L)));
        if (mTvEnd != null)
            mTvEnd.setText(mDateTimeFormatter.print(new DateTime(getDisplayEndTime() * 1000L)));
        if (mTvDuration != null)
            mTvDuration.setText(mPeriodFormatter.print(
                    new Period((getDisplayEndTime() - getDisplayStartTime()) * 1000L )));

        if (mFab != null)
            setFabVisibility((mOriginalStartTime != getDisplayStartTime()) ||
                    (mOriginalEndTime != getDisplayEndTime()));
    }

    private long getDisplayStartTime() {
        if (mStartTime > 0)
            return mStartTime;
        else
            return mOriginalStartTime;
    }

    private long getDisplayEndTime() {
        if (mEndTime > 0)
            return mEndTime;
        else
            return mOriginalEndTime;
    }

    private void swapCursor(Cursor cursor) {
        if (mCursor != null)
            mCursor.close();
        mCursor = cursor;
        if (mCursor != null)
            fillSession();
    }

    private void fillSession() {
        if ((mCursor == null) || (mCursor.getCount() <= 0))
            return;

        mCursor.moveToFirst();
        mOriginalStartTime = mCursor.getLong(mCursor.getColumnIndex(SessionDescription.COLUMN_START));
        mOriginalEndTime = mCursor.getLong(mCursor.getColumnIndex(SessionDescription.COLUMN_END));

        updateUI();
    }

    private class SessionLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case LOADER_ID_SESSION:
                    return new CursorLoader(getContext(),
                            SessionDescription.buildSessionUri(mSessionId),
                            null, null, null, null);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            swapCursor(null);
        }
    }
}
