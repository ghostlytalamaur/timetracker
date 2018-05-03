package mvasoft.timetracker;

import mvasoft.timetracker.data.room.entity.SessionGroupEntity;

public class Session extends SessionGroupEntity {

    public Session(long id, long startTime, long endTime) {
        super(id, startTime, endTime);
    }
}
