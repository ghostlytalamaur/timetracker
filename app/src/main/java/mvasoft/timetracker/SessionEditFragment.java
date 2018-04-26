package mvasoft.timetracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mvasoft.datetimepicker.DatePickerFragment;
import mvasoft.timetracker.data.DatabaseDescription.SessionDescription;
import mvasoft.timetracker.databinding.FragmentSessionEditBinding;
import mvasoft.timetracker.ui.SessionEditViewModel;
import mvasoft.timetracker.ui.base.BindingSupportFragment;

import static mvasoft.timetracker.Consts.LOADER_ID_SESSION;


public class SessionEditFragment extends BindingSupportFragment<FragmentSessionEditBinding,
        SessionEditViewModel> {

    private static final String ARGS_SESSION_ID = "session_id";
    private static final int REQUEST_START_TIME = 1;
    private static final int REQUEST_END_TIME   = 2;
    private SessionEditData mData;
    private Cursor mCursor;

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
                            mData.getStartTime() / 1000) / 1000;
                    if (newDateTime != mData.getStartTime()) {
                        mData.setStartTime(newDateTime);
                    }
                    break;
                case REQUEST_END_TIME:
                    newDateTime = data.getLongExtra(DatePickerFragment.ARGS_DATE,
                            mData.getEndTime() / 1000) / 1000;
                    if (newDateTime != mData.getEndTime()) {
                        mData.setEndTime(newDateTime);
                    }
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mData = new SessionEditData(-1, -1, -1);
        if (savedInstanceState != null)
            mData.restoreState(savedInstanceState);
        else {
            mData.setId(getArguments().getLong(ARGS_SESSION_ID));
        }

        SessionLoaderCallbacks loaderCallbacks = new SessionLoaderCallbacks();
        getActivity().getSupportLoaderManager().initLoader(LOADER_ID_SESSION, null, loaderCallbacks);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        getBinding().fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSession();
            }
        });

        getBinding().tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDateTime(mData.getStartTime(), REQUEST_START_TIME);
            }
        });

        getBinding().tvEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDateTime(mData.getEndTime(), REQUEST_END_TIME);
            }
        });

        return v;
    }

    @Override
    protected SessionEditViewModel onCreateViewModel() {
        return new SessionEditViewModel(mData);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_session_edit;
    }

    @Override
    protected int getModelVariableId() {
        return BR.view_model;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mData.saveState(outState);
    }

    @Override
    public void onDestroy() {
        swapCursor(null);
        getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID_SESSION);
        super.onDestroy();
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
        mData.setOriginalStartTime(
                mCursor.getLong(mCursor.getColumnIndex(SessionDescription.COLUMN_START)));
        mData.setOriginalEndTime(
                mCursor.getLong(mCursor.getColumnIndex(SessionDescription.COLUMN_END)));
    }

    private void saveSession() {
        SessionHelper helper = new SessionHelper(getContext());
        boolean isSaved = helper.updateSession(mData.getId(), mData.getStartTime(),
                mData.isClosed() ? mData.getEndTime() : 0);
        if (isSaved)
            Snackbar.make(getBinding().fab, R.string.session_saved, Snackbar.LENGTH_LONG);
        else
            Snackbar.make(getBinding().fab, R.string.session_unable_save, Snackbar.LENGTH_LONG);
    }

    private void editDateTime(long dateTime, int requestCode) {
        if (dateTime == 0)
            dateTime = System.currentTimeMillis() / 1000L;
        DatePickerFragment dlg = DatePickerFragment.newInstance(dateTime * 1000, "");
        dlg.setTargetFragment(this, requestCode);
        dlg.show(getFragmentManager(), "dialog_date");
    }

    private class SessionLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case LOADER_ID_SESSION:
                    return new CursorLoader(getContext(),
                            SessionDescription.buildSessionUri(mData.getId()),
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
