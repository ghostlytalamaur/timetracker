package mvasoft.timetracker.data.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;

import java.util.List;

@Entity
public class SessionGroupEntity {

    @ColumnInfo(name = "groupId")
    public long groupId;

    @ColumnInfo(name = "groupStartTime")
    public long groupStartTime;

    @ColumnInfo(name = "groupEndTime")
    public long groupEndTime;

    @ColumnInfo(name = "sessionIds")
    public List<Long> sessionIds;
}
