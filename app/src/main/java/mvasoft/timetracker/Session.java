package mvasoft.timetracker;

import mvasoft.timetracker.data.room.entity.SessionEntity;

public class Session extends SessionEntity {

    public Session(long id, long startTime, long endTime) {
        super(id, startTime, endTime);
    }
}
