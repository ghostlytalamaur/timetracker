package mvasoft.timetracker.data.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Observable;

@Entity(tableName = "sessions")
public class SessionGroupEntity extends Observable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public final long id;

    @ColumnInfo(name = "StartTime")
    public long startTime;

    @ColumnInfo(name = "EndTime")
    public long endTime;

    public SessionGroupEntity(long id, long startTime, long endTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
