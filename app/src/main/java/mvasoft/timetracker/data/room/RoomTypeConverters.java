package mvasoft.timetracker.data.room;

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
//    static String listOfLongToSt
}
