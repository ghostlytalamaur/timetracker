package mvasoft.timetracker.db;

import android.arch.persistence.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public abstract class RoomTypeConverters {

    @TypeConverter
    public static List<Long> listOfLongFromString(String input) {
        ArrayList<Long> list = new ArrayList<>();
        for (String s : input.split(" "))
            list.add(Long.parseLong(s));
        return list;
    }

//    @TypeConverter
//    public static Session entityFromSession(Session session) {
//        return new Session(session.getId(), session.getStartTime(), session.getEndTime());
//    }

//    @TypeConverter
//    public static Session entityToSession(Session entity) {
//        return new Session(entity.id, entity.startTime, entity.endTime);
//    }
}
