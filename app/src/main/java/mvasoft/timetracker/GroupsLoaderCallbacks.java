package mvasoft.timetracker;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import mvasoft.timetracker.data.DatabaseDescription;

import static mvasoft.timetracker.Consts.LOADER_ID_GROUPS_CURRENT;
import static mvasoft.timetracker.Consts.LOADER_ID_GROUPS_TODAY;
import static mvasoft.timetracker.Consts.LOADER_ID_GROUPS_WEEK;


class GroupsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    private final Context mContext;
    private final GroupInfoProvider mGroupInfoProvider;
    private final GroupsList mCurrentGroups;
    private final GroupsList mTodayGroup;
    private final GroupsList mWeekGroup;

    GroupsLoaderCallbacks(@NonNull Context context, @NonNull GroupInfoProvider groupInfoProvider,
                          GroupsList currentGroups,
                          GroupsList todayGroup,
                          GroupsList weekGroup) {
        mContext = context.getApplicationContext();
        mGroupInfoProvider = groupInfoProvider;
        mCurrentGroups = currentGroups;
        mTodayGroup = todayGroup;
        mWeekGroup = weekGroup;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID_GROUPS_CURRENT:
                return new CursorLoader(mContext,
                        mGroupInfoProvider.getCurrentGroupsUri(),
                        null, null, null,
                        DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
            case LOADER_ID_GROUPS_TODAY:
                return new CursorLoader(mContext,
                        DatabaseDescription.GroupsDescription.GROUP_DAY_URI,
                        null,
                        String.format("date(%1$s, 'unixepoch') = date('now')",
                                DatabaseDescription.SessionDescription.COLUMN_START),
                        null,
                        DatabaseDescription.SessionDescription.COLUMN_START + " DESC");
            case LOADER_ID_GROUPS_WEEK:
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
            case LOADER_ID_GROUPS_CURRENT: {
                swapCurrentGroupCursor(cursor);
                break;
            }
            case LOADER_ID_GROUPS_TODAY: {
                swapTodayCursor(cursor);
                break;
            }
            case LOADER_ID_GROUPS_WEEK: {
                swapWeekCursor(cursor);
                break;
            }
        }
    }

    private void swapCurrentGroupCursor(Cursor cursor) {
        mCurrentGroups.updateData(cursor);
    }

    private void swapTodayCursor(Cursor cursor) {
        mTodayGroup.updateData(cursor);
    }

    private void swapWeekCursor(Cursor cursor) {
        mWeekGroup.updateData(cursor);
    }

}
