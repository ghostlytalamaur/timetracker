package mvasoft.timetracker;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import mvasoft.timetracker.data.DatabaseDescription;


class GroupsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int GROUPS_LOADER_ID   = 1;
    static final int TODAY_LOADER_ID    = 2;
    static final int WEEK_LOADER_ID     = 3;

    private final Context mContext;
    private final GroupInfoProvider mGroupInfoProvider;
    private GroupsList mCurrentGroups;
    private GroupsList mTodayGroup;
    private GroupsList mWeekGroup;

    GroupsLoaderCallbacks(@NonNull Context context, @NonNull GroupInfoProvider groupInfoProvider,
                          GroupsList currentGroups,
                          GroupsList todayGroup,
                          GroupsList weekGroup) {
        mContext = context;
        mGroupInfoProvider = groupInfoProvider;
        mCurrentGroups = currentGroups;
        mTodayGroup = todayGroup;
        mWeekGroup = weekGroup;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GROUPS_LOADER_ID:
                return new CursorLoader(mContext,
                        mGroupInfoProvider.getCurrentGroupsUri(),
                        null, null, null,
                        DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
            case TODAY_LOADER_ID:
                return new CursorLoader(mContext,
                        DatabaseDescription.GroupsDescription.GROUP_DAY_URI,
                        null,
                        String.format("date(%1$s, 'unixepoch') = date('now')",
                                DatabaseDescription.SessionDescription.COLUMN_START),
                        null,
                        DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
            case WEEK_LOADER_ID:
                return new CursorLoader(mContext,
                        DatabaseDescription.GroupsDescription.GROUP_WEEK_URI,
                        null,
                        String.format(
                                "date(%1$s, 'unixepoch', 'weekday 0', '-7 days') " +
                                        " = date('now', 'weekday 0', '-7 days')",
                                DatabaseDescription.SessionDescription.COLUMN_START),
                        null,
                        DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swapCursor(loader.getId(), data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swapCursor(loader.getId(), null);
    }

    private void swapCursor(int id, Cursor cursor) {
        switch (id) {
            case GROUPS_LOADER_ID: {
                swapCurrentGroupCursor(cursor);
                break;
            }
            case TODAY_LOADER_ID: {
                swapTodayCursor(cursor);
                break;
            }
            case WEEK_LOADER_ID: {
                swapWeekCursor(cursor);
                break;
            }
        }
    }

    private void swapCurrentGroupCursor(Cursor cursor) {
        mCurrentGroups.swapCursor(cursor);
    }

    private void swapTodayCursor(Cursor cursor) {
        mTodayGroup.swapCursor(cursor);
    }

    private void swapWeekCursor(Cursor cursor) {
        mWeekGroup.swapCursor(cursor);
    }

}
