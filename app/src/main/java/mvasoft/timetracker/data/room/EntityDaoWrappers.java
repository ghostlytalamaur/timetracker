package mvasoft.timetracker.data.room;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.data.room.entity.SessionEntity;

abstract class EntityDaoWrappers {

    static LiveData<GroupsList> wrapEntityList(final LiveData<List<SessionEntity>> entityLiveData) {
        return new GroupsListLiveData(entityLiveData);
    }

    private static GroupsList toGroupList(List<SessionEntity> sessionGroupEntities) {
        GroupsList res = new GroupsList();
        if (sessionGroupEntities != null)
            for (SessionEntity entity : sessionGroupEntities)
                res.add(toGroup(entity));
        return res;
    }

    private static GroupsList.SessionGroup toGroup(SessionEntity entity) {
        return new GroupsList.SessionGroup(entity.id, entity.startTime, entity.endTime, 0, 0);
    }

    private static class GroupsListLiveData extends MutableLiveData<GroupsList> {


        private final LiveData<List<SessionEntity>> mData;
        private final Observer<List<SessionEntity>> mObserver;

        GroupsListLiveData(LiveData<List<SessionEntity>> entityLiveData) {
            mData = entityLiveData;
            mObserver = new Observer<List<SessionEntity>>() {
                @Override
                public void onChanged(@Nullable List<SessionEntity> sessionGroupEntities) {
                    setValue(toGroupList(sessionGroupEntities));
                }
            };
            mData.observeForever(mObserver);
        }
    }
}
