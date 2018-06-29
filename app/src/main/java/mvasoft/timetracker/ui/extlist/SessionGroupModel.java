package mvasoft.timetracker.ui.extlist;


import android.util.Pair;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayDescription;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;

public class SessionGroupModel {

    private final DataRepository mRepository;
    private final BehaviorProcessor<List<Long>> mDays;
    private final Flowable<List<DayGroup>> mGroups;

    SessionGroupModel(DataRepository repository) {
        mRepository = repository;
        mDays = BehaviorProcessor.createDefault(Collections.singletonList(0L));
        mGroups = mDays
                .skip(1) // skip initial value
                .switchMap(mRepository::getDayGroupsRx);
    }

    public Flowable<List<DayGroup>> getGroups() {
        return mGroups;
    }

    public void setDate(long startUnixSec, long endUnixSec) {
        List<Long> days = DateTimeHelper.daysList(startUnixSec, endUnixSec);
        if (days == null)
            days = Collections.singletonList(0L);

        mDays.onNext(days);
    }
}
