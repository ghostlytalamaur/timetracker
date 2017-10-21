package mvasoft.timetracker.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.StringJoiner;

import mvasoft.timetracker.R;
import mvasoft.timetracker.data.DatabaseDescription.GroupsDescription;
import mvasoft.timetracker.data.DatabaseDescription.SessionDescription;

public class SessionsContentProvider extends ContentProvider {

    private SessionsDatabaseHelper mDBHelper;
    private static final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ONE_SESSION        = 1;
    private static final int SESSIONS           = 2;
    private static final int GROUP_NONE         = 3;
    private static final int GROUP_DAY          = 4;
    private static final int GROUP_WEEK         = 5;
    private static final int GROUP_MONTH        = 6;
    private static final int GROUP_YEAR         = 7;
    private static final int ONE_GROUP_NONE     = 8;
    private static final int ONE_GROUP_DAY      = 9;
    private static final int ONE_GROUP_WEEK     = 11;
    private static final int ONE_GROUP_MONTH    = 12;
    private static final int ONE_GROUP_YEAR     = 13;



    static {
        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/#", ONE_SESSION);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME, SESSIONS);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_NONE, GROUP_NONE);
//                 GroupsDescription.GROUPS_PATH + "/" + GroupsDescription.GROUP_NONE, GROUP_NONE);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_DAY, GROUP_DAY);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_WEEK, GROUP_WEEK);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_MONTH, GROUP_MONTH);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_YEAR, GROUP_YEAR);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_NONE + "/#", ONE_GROUP_NONE);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_DAY + "/#", ONE_GROUP_DAY);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_WEEK + "/#", ONE_GROUP_WEEK);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_MONTH + "/#", ONE_GROUP_MONTH);

        mMatcher.addURI(DatabaseDescription.AUTHORITY,
                SessionDescription.TABLE_NAME + "/" + GroupsDescription.GROUPS_PATH + "/" +
                        GroupsDescription.GROUP_YEAR + "/#", ONE_GROUP_YEAR);

    }

    public SessionsContentProvider() {
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new SessionsDatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        return null;
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(SessionDescription.TABLE_NAME);

        String groupBY = null;
        int uriID = mMatcher.match(uri);
        switch (uriID) {
            case ONE_SESSION:
                builder.appendWhere(SessionDescription._ID + "=" + uri.getLastPathSegment());
                break;
            case SESSIONS:
                break;

            case GROUP_NONE:
            case GROUP_DAY:
            case GROUP_WEEK:
            case GROUP_MONTH:
            case GROUP_YEAR:
//                final String PROJ_GROUP_NONE =
//                        "   min(%1$s) as '%3$s', " +
//                        "   max( " +
//                        "       case " +
//                        "           when %2$s is NULL then " +
//                        "               strftime('%%s', 'now') " +
//		                "       else " +
//                        "           %2$s " +
//                        "       end	" +
//	                    "   ) as '%4$s', " +
//                        "   sum(case " +
//                        "       when %2$s is NULL then " +
//                        "           strftime('%%s','now') - %1$s " +
//	                    "       else " +
//                        "           %2$s - %1$s " +
//                        "   end) as '%5$s', " +
//                        "   sum( " +
//                        "       case when %2$s is NULL then 1 else 0 end " +
//	                    "   ) as %6$s";
//
//                String proj = String.format(PROJ_GROUP_NONE,
//                        SessionDescription.COLUMN_START,
//                        SessionDescription.COLUMN_END,
//                        GroupsDescription.COLUMN_START,
//                        GroupsDescription.COLUMN_END,
//                        GroupsDescription.COLUMN_DURATION,
//                        GroupsDescription.COLUMN_UNCOMPLETED_COUNT
//                        );
//                projection = new String[] {proj};

                projection = new String[] {getProjectionByQuery(uriID)};
                groupBY = getGroupByQuery(uriID);
                break;

            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.query_invalid_uri) + uri);
        }


        Cursor cursor = builder.query(mDBHelper.getReadableDatabase(), projection,
                selection, selectionArgs, groupBY, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private String getProjectionByQuery(int uriID) {
        switch (uriID) {
            case GROUP_NONE:
                final String PROJ_GROUP_NONE =
                        " %7$s as '%7$s'," +
                        "   %1$s as '%3$s', " +
                                "       case " +
                                "           when %2$s is NULL then " +
                                "               strftime('%%s', 'now') " +
                                "       else " +
                                "           %2$s " +
                                "       end	" +
                                "   as '%4$s', " +
                                "   case " +
                                "       when %2$s is NULL then " +
                                "           strftime('%%s','now') - %1$s " +
                                "       else " +
                                "           %2$s - %1$s " +
                                "   end as '%5$s', " +
                                "       case when %2$s is NULL then 1 else 0 end as %6$s";
                return String.format(PROJ_GROUP_NONE,
                        SessionDescription.COLUMN_START,
                        SessionDescription.COLUMN_END,
                        GroupsDescription.COLUMN_START,
                        GroupsDescription.COLUMN_END,
                        GroupsDescription.COLUMN_DURATION,
                        GroupsDescription.COLUMN_UNCOMPLETED_COUNT,
                        SessionDescription._ID
                );

            case GROUP_DAY:
            case GROUP_WEEK:
            case GROUP_MONTH:
            case GROUP_YEAR:
                final String PROJ_GROUP =
                        " min(%7$s) as '%7$s'," +
                        "   min(%1$s) as '%3$s', " +
                                "   max( " +
                                "       case " +
                                "           when %2$s is NULL then " +
                                "               strftime('%%s', 'now') " +
                                "       else " +
                                "           %2$s " +
                                "       end	" +
                                "   ) as '%4$s', " +
                                "   sum(case " +
                                "       when %2$s is NULL then " +
                                "           strftime('%%s','now') - %1$s " +
                                "       else " +
                                "           %2$s - %1$s " +
                                "   end) as '%5$s', " +
                                "   sum( " +
                                "       case when %2$s is NULL then 1 else 0 end " +
                                "   ) as %6$s";
                return String.format(PROJ_GROUP,
                        SessionDescription.COLUMN_START,
                        SessionDescription.COLUMN_END,
                        GroupsDescription.COLUMN_START,
                        GroupsDescription.COLUMN_END,
                        GroupsDescription.COLUMN_DURATION,
                        GroupsDescription.COLUMN_UNCOMPLETED_COUNT,
                        SessionDescription._ID
                );
            default:
                return null;
        }
    }

    private String getGroupByQuery(int uriID) {
        switch (uriID) {
            case GROUP_DAY:
            case ONE_GROUP_DAY:
                return String.format("date(%1$s, 'unixepoch')", GroupsDescription.COLUMN_START);
            case GROUP_WEEK:
            case ONE_GROUP_WEEK:
                return String.format("date(%1$s, 'unixepoch', 'weekday 0', '-7 days')", GroupsDescription.COLUMN_START);
            case GROUP_MONTH:
            case ONE_GROUP_MONTH:
                return String.format("date(%1$s, 'unixepoch', 'start of month')", GroupsDescription.COLUMN_START);
            case GROUP_YEAR:
            case ONE_GROUP_YEAR:
                return String.format("date(%1$s, 'unixepoch', 'start of year')", GroupsDescription.COLUMN_START);
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newUri = null;

        switch (mMatcher.match(uri)) {
            case SESSIONS:
                long id = mDBHelper.getWritableDatabase().insert(SessionDescription.TABLE_NAME, null, values);
                if (id > 0) {
                    newUri = SessionDescription.buildSessionUri(id);
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                else
                    throw new SQLException(
                            getContext().getString(R.string.insert_failed) + uri);
                break;

            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.insert_invalid_uri) + uri);
        }

        return newUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int rowsCount;

        switch (mMatcher.match(uri)) {
            case ONE_SESSION:
                String id = uri.getLastPathSegment();
                rowsCount = mDBHelper.getWritableDatabase().update(SessionDescription.TABLE_NAME, values,
                        SessionDescription._ID + "=" + id, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.update_invalid_uri) + uri);
        }

        if (rowsCount != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_NONE_URI, null);
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_DAY_URI, null);
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_WEEK_URI, null);
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_MONTH_URI, null);
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_YEAR_URI, null);

        }

        return rowsCount;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int rowsCount;

        int uriId = mMatcher.match(uri);
        switch (uriId) {
            case ONE_SESSION:
            case ONE_GROUP_NONE:
                String id = uri.getLastPathSegment();
                rowsCount = mDBHelper.getWritableDatabase().delete(SessionDescription.TABLE_NAME,
                        SessionDescription._ID + "=" + id, selectionArgs);
                break;
            case ONE_GROUP_DAY:
            case ONE_GROUP_WEEK:
            case ONE_GROUP_MONTH:
            case ONE_GROUP_YEAR:
                rowsCount = deleteSessionsFromGroups(uriId, uri.getLastPathSegment());
                break;
            case GROUP_DAY:
            case GROUP_WEEK:
            case GROUP_MONTH:
            case GROUP_YEAR:

            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.delete_invalid_uri) + uri);
        }

        if (rowsCount != 0) {
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_NONE_URI, null);
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_DAY_URI, null);
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_WEEK_URI, null);
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_MONTH_URI, null);
            getContext().getContentResolver().notifyChange(GroupsDescription.GROUP_YEAR_URI, null);
        }

        return rowsCount;
    }

    private int deleteSessionsFromGroups(int groupType, String groupId) {
//                "DELETE " +
//                "FROM " +
//                "   %1$s WHERE " + // TableName
        final String SQL_WHERE = String.format(
                " %2$s in " + // ID
                "   (" +
                "       SELECT " +
                "           %2$s " +  // ID
                "       FROM " +
                "           %1$s " +  // TableName
                "       WHERE %3$s IN " + // GroupDate
                "           ( " +
                "               SELECT " +
                "                   GroupDate " +
                "               FROM " +
                "                   (" +
                "                       SELECT " +
                "                           %3$s as GroupDate, " + // GroupDate
                "                           MIN(%2$s) as GroupID " + // ID
                "                       FROM " +
                "                           %1$s " + // TableName
                "                       GROUP BY " +
                "                           %3$s " + // GroupDate
                "                   ) " +
                "               WHERE " +
                "                   GroupID IN (%4$s) " + // GroupID
                "           ) " +
                "   );",
                SessionDescription.TABLE_NAME,
                SessionDescription._ID,
                getGroupByQuery(groupType),
                groupId);
        return mDBHelper.getWritableDatabase().delete(SessionDescription.TABLE_NAME,
                SQL_WHERE, null);
    }


    private long[] getSessionIDsByGroup(int groupType, long groupId) {
        final String COLUMN_IDS = "ids";
        final String SQL_QUERY = String.format(
                "select" +
                "   %1$s" +
                "FROM" +
                "   (SELECT" +
                "       min(%2$s) as GroupID," +
                "       group_concat(%2$s, ', ') as %1$s" +
                "   FROM" +
                "       %3$s" +
                "   GROUP BY" +
                "       %4%s" +
                "   )" +
                "WHERE" +
                "   GroupID = %5%d" +
                ";",
                COLUMN_IDS,
                SessionDescription._ID,
                SessionDescription.TABLE_NAME,
                getGroupByQuery(groupType),
                groupId
                );
        Cursor c = mDBHelper.getReadableDatabase().rawQuery(SQL_QUERY, null);
        if (c == null)
            return null;
        else if (c.getCount() <= 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        String IdsCol = c.getString(c.getColumnIndex(COLUMN_IDS));
        String[] strIds = IdsCol.split(", ");
        long[] res = new long[strIds.length];
        for (int i = 0; i <= strIds.length; i++)
            res[i] = Long.parseLong(strIds[i]);
        return res;
    }


}
