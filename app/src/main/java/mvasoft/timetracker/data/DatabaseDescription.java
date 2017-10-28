package mvasoft.timetracker.data;

import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by mihal on 13.10.2017.
 */

public class DatabaseDescription {
    public static final String DATABASE_NAME = "timetracker.db";
    public static final String AUTHORITY = "com.mvasoft.timetracker.data.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class GroupsDescription {
        public static final String GROUPS_PATH = "groups";

        public static final Uri CONTENT_URI =
                SessionDescription.CONTENT_URI.buildUpon().appendPath(GROUPS_PATH).build();


        public static final String GROUP_NONE = "none";
        public static final String GROUP_DAY = "day";
        public static final String GROUP_WEEK = "week";
        public static final String GROUP_MONTH = "month";
        public static final String GROUP_YEAR = "year";

        public static final String COLUMN_START = "StartTime";
        public static final String COLUMN_END  = "EndTime";
        public static final String COLUMN_DURATION  = "Duration";
        public static final String COLUMN_UNCOMPLETED_COUNT = "UncompletedCount";

        public static final Uri GROUP_NONE_URI =
                CONTENT_URI.buildUpon().appendEncodedPath(GROUP_NONE).build();
        public static final Uri GROUP_DAY_URI =
                CONTENT_URI.buildUpon().appendEncodedPath(GROUP_DAY).build();
        public static final Uri GROUP_WEEK_URI =
                CONTENT_URI.buildUpon().appendEncodedPath(GROUP_WEEK).build();
        public static final Uri GROUP_MONTH_URI =
                CONTENT_URI.buildUpon().appendEncodedPath(GROUP_MONTH).build();
        public static final Uri GROUP_YEAR_URI =
                CONTENT_URI.buildUpon().appendEncodedPath(GROUP_YEAR).build();
    }

    public static final class SessionDescription {
        public static final String TABLE_NAME = "sessions";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String _ID = "_id";
        public static final String COLUMN_START = "StartTime";
        public static final String COLUMN_END = "EndTime";

        public static Uri buildSessionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
