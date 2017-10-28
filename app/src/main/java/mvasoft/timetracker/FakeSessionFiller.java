package mvasoft.timetracker;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Random;

import mvasoft.timetracker.data.DatabaseDescription;

/**
 * Created by mihal on 21.10.2017.
 */

class FakeSessionFiller {

    public static void fill(Context context) {
        int COUNT = 1000;



        Random rnd = new Random(System.currentTimeMillis());
        ArrayList<ContentProviderOperation> opList = new ArrayList<>(COUNT);
        for (int weeks = 0; weeks < 36; weeks++) {
            DateTime dt = new DateTime(2017, 1, 1, 8, 0);
            dt = dt.plusWeeks(weeks);

            for (int i = 0; i < 15; i++) {

                long startTime = dt.getMillis() / 1000L;
                long duration = 3600 + rnd.nextInt(3600);
                long endTime = startTime + duration;

                ContentValues values = new ContentValues();

                values.put(DatabaseDescription.SessionDescription.COLUMN_START,
                        startTime);
                values.put(DatabaseDescription.SessionDescription.COLUMN_END,
                        endTime);

                opList.add(ContentProviderOperation.newInsert(
                        DatabaseDescription.SessionDescription.CONTENT_URI).withValues(values).build());

                dt = dt.plus(duration * 1000L + 300 * rnd.nextInt(4));
                if (i % 3 == 0)
                    dt = dt.plusDays(1);
            }
        }

        try {
            context.getContentResolver().applyBatch(DatabaseDescription.AUTHORITY, opList);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
