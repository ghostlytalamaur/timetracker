package mvasoft.timetracker.ui.extlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.vo.TimeInfoProvider;

public class SessionItemViewModel extends BaseItemViewModel {

    private final DateTimeFormatters mFormatter;
    private final TimeInfoProvider mTimeInfo;
    private final MutableLiveData<String> mDurationLiveData;

    SessionItemViewModel(DateTimeFormatters formatter, TimeInfoProvider timeInfo) {
        mFormatter = formatter;
        mTimeInfo = timeInfo;
        mDurationLiveData = new MutableLiveData<>();
        updateDuration();
    }

    public String getStartTime() {
        if (mTimeInfo != null)
            return /*mFormatter.formatDate(mTimeInfo.getStartTime()) + " " +*/ mFormatter.formatTime(mTimeInfo.getStartTime());
        else
            return "Start date";
    }

    public String getEndTime() {
         if (mTimeInfo != null)
             return /*mFormatter.formatDate(mTimeInfo.getEndTime()) + " " +*/ mFormatter.formatTime(mTimeInfo.getEndTime());
        else
            return "End date";
    }

    public LiveData<String> getDuration() {
        return mDurationLiveData;
    }

    @Override
    void updateDuration() {
        if (mTimeInfo != null)
            mDurationLiveData.postValue(mFormatter.formatDuration(mTimeInfo.getDuration()));
    }

    @Override
    public boolean getIsRunning() {
        return (mTimeInfo != null) && mTimeInfo.isRunning();
    }

    @Override
    public long getId() {
        if (mTimeInfo != null)
            return mTimeInfo.getId();
        else
            return 0;
    }

    @Override
    public String getClipboardString(@Nullable DateTimeFormatters formatter) {
        if (formatter == null)
            formatter = mFormatter;
        return String.format("%s - %s: %s\n",
                formatter.formatDate(mTimeInfo.getStartTime()),
                formatter.formatDate(mTimeInfo.getEndTime()),
                formatter.formatDuration(mTimeInfo.getDuration()));
    }

    @Override
    void appendSessionIds(@NonNull List<Long> destList) {
        if (mTimeInfo != null)
            destList.add(mTimeInfo.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SessionItemViewModel))
            return false;

        SessionItemViewModel to = (SessionItemViewModel) obj;
        return Objects.equals(getIsSelected().getValue(), to.getIsSelected().getValue()) &&
                Objects.equals(mTimeInfo, to.mTimeInfo);
    }
}
