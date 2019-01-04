package mvasoft.timetracker.ui.extlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.Nullable;

import java.util.List;

import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.SessionsGroup;

public class GroupItemViewModel extends BaseItemViewModel {

    private final SessionsGroup mGroup;
    private final DateTimeFormatters mFormatter;
    private final AppPreferences mPreferences;
    private final MutableLiveData<String> mDurationData;

    public GroupItemViewModel(DateTimeFormatters formatter, AppPreferences preferences, SessionsGroup group) {
        mFormatter = formatter;
        mPreferences = preferences;
        mGroup = group;
        mDurationData = new MutableLiveData<>();
        mDurationData.postValue(mFormatter.formatDuration(mGroup.calculateDuration()));
    }

    @Override
    public long getId() {
        return mGroup.getId();
    }

    @Override
    public boolean getIsRunning() {
        return mGroup.hasOpenedSessions();
    }

    @Override
    void appendSessionIds(List<Long> destList) {
        mGroup.collectIds(destList);
    }

    @Override
    void updateDuration() {
        mDurationData.postValue(mFormatter.formatDuration(mGroup.calculateDuration()));
    }

    public String getStartTime() {
        long start = mGroup.getStart();
        return String.format("%s %s", mFormatter.formatDate(start), mFormatter.formatTime(start));
    }

    public String getEndTime() {
        long end = mGroup.getEnd();
        return String.format("%s %s", mFormatter.formatDate(end), mFormatter.formatTime(end));
    }

    public LiveData<String> getDuration() {
        return mDurationData;
    }

    public int sessionsCount() {
        return mGroup.sessionsCount();
    }


    @Override
    String getClipboardString(@Nullable DateTimeFormatters formatter) {
        if (formatter == null)
            formatter = mFormatter;
        return String.format("%s - %s: %s\n",
                formatter.formatDate(mGroup.getStart()), formatter.formatDate(mGroup.getEnd()),
                formatter.formatDuration(DateTimeHelper.roundDateTime(mGroup.calculateDuration(),
                        mPreferences.roundDurationToMin())));
    }
}
