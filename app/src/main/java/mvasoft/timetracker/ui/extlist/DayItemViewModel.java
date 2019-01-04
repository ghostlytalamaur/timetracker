package mvasoft.timetracker.ui.extlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayGroup;
import mvasoft.timetracker.vo.Session;

public class DayItemViewModel extends BaseItemViewModel {

    private final DateTimeFormatters mFormatter;
    private final DayGroup mDayGroup;
    private final AppPreferences mPreferences;
    private final MutableLiveData<String> mDurationLiveData;
    private final MutableLiveData<String> mTargetTimeDiffData;

    DayItemViewModel(DateTimeFormatters formatter, DayGroup dayGroup, AppPreferences preferences) {
        mFormatter = formatter;
        mDayGroup = dayGroup;
        mPreferences = preferences;
        mDurationLiveData = new MutableLiveData<>();
        mTargetTimeDiffData = new MutableLiveData<>();
        updateDuration();
    }

    public String getDayText() {
        if (mDayGroup != null)
            return mFormatter.formatDate(mDayGroup.getStartTime());
        else
            return "Start date";
    }


    public LiveData<String> getDuration() {
        return mDurationLiveData;
    }

    public LiveData<String> getTargetTimeDiff() {
        return mTargetTimeDiffData;
    }

    @Override
    void updateDuration() {
        if (mDayGroup == null)
            return;

        mDurationLiveData.postValue(mFormatter.formatDuration(
                mDayGroup.getDuration()));
        mTargetTimeDiffData.postValue(mFormatter.formatDuration(
                mDayGroup.getTargetTimeDiff(mPreferences)));
    }

    public boolean getIsTargetAchieved() {
        return mDayGroup != null && mDayGroup.getTargetTimeDiff(mPreferences) >= 0;
    }

    @Override
    public boolean getIsRunning() {
        return (mDayGroup != null) && mDayGroup.isRunning();
    }


    @Override
    public long getId() {
        if (mDayGroup != null)
            return mDayGroup.getId();
        else
            return 0;
    }

    @Override
    public String getClipboardString(@Nullable DateTimeFormatters formatter) {
        if (formatter == null)
            formatter = mFormatter;
        return String.format("%s: %s\n",
                formatter.formatDate(mDayGroup.getDay()),
                formatter.formatDuration(DateTimeHelper.roundDateTime(mDayGroup.getDuration(),
                        mPreferences.roundDurationToMin())));
    }

    @Override
    void appendSessionIds(@NonNull List<Long> destList) {
        if (mDayGroup != null && mDayGroup.getSessions() != null)
            for (Session s : mDayGroup.getSessions())
                destList.add(s.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DayItemViewModel))
            return false;
        DayItemViewModel to = (DayItemViewModel) obj;
        return getIsSelected() == to.getIsSelected() &&
                (mDayGroup != null && mDayGroup.equals(to.mDayGroup));
    }
}
