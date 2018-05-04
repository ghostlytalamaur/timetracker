package mvasoft.timetracker.data.room;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import mvasoft.timetracker.GroupsList;
import mvasoft.timetracker.vo.Session;

abstract class EntityDaoWrappers {

    static LiveData<GroupsList> wrapEntityList(final LiveData<List<Session>> entityLiveData) {
        return new GroupsListLiveData(entityLiveData);
    }

    private static GroupsList toGroupList(List<Session> sessionGroupEntities) {
        GroupsList res = new GroupsList();
        if (sessionGroupEntities != null)
            for (Session entity : sessionGroupEntities)
                res.add(toGroup(entity));
        return res;
    }

    private static GroupsList.SessionGroup toGroup(Session entity) {
//        return new GroupsList.SessionGroup(entity.id, entity.startTime, entity.endTime, 0, 0);
        return new GroupsList.SessionGroup(entity.getId(), entity.getStartTime(), entity.getEndTime(), 0, 0);
    }

    private static class GroupsListLiveData extends MutableLiveData<GroupsList> {


        private final LiveData<List<Session>> mData;
        private final Observer<List<Session>> mObserver;

        GroupsListLiveData(LiveData<List<Session>> entityLiveData) {
            mData = entityLiveData;
            mObserver = new Observer<List<Session>>() {
                @Override
                public void onChanged(@Nullable List<Session> sessionGroupEntities) {
                    setValue(toGroupList(sessionGroupEntities));
                }
            };
            mData.observeForever(mObserver);
        }
    }
}
