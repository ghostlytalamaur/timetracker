package mvasoft.timetracker.ui.editdate;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.support.annotation.NonNull;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayDescription;
import timber.log.Timber;

public class DatesViewModel extends BaseViewModel {

    private final DataRepository mRepository;
    private final AppPreferences mPreferences;
    private LiveData<Set<CalendarDay>> mWorkingDays;
    private BehaviorProcessor<CalendarDay> mLastQueryMonth;

    @Inject
    public DatesViewModel(@NonNull Application application, DataRepository repository, AppPreferences preferences) {
        super(application);
        mRepository = repository;
        mPreferences = preferences;

        mLastQueryMonth = BehaviorProcessor.createDefault(CalendarDay.today());
        Flowable<Set<CalendarDay>> workingDays = mLastQueryMonth
                .flatMap(month -> {
                    long unixTime = DateTimeHelper.getUnixTime(month.getYear(), month.getMonth(), month.getDay());
                    long start = DateTimeHelper.startOfMonth(unixTime);
                    long end = DateTimeHelper.endOfMonth(unixTime);

                    return mRepository.getDayDescriptionsRx(start, end)
                            .map(list -> buildWorkingDaysSet(list, month));
                });
        mWorkingDays = LiveDataReactiveStreams.fromPublisher(workingDays);
    }

    LiveData<Set<CalendarDay>> getWorkingDays() {
        return mWorkingDays;
    }

    void updateWorkingDays(CalendarDay month) {
        mLastQueryMonth.onNext(month);
    }

    private Set<CalendarDay> buildWorkingDaysSet(List<DayDescription> list, CalendarDay calendarDay) {
        HashSet<CalendarDay> days = new HashSet<>(31);
        HashSet<CalendarDay> notWorkingDays = new HashSet<>(31);
        if (list != null) {
            for (DayDescription descr : list) {
                DateTime dt = new DateTime(descr.getDate() * 1000);
                CalendarDay day = CalendarDay.from(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
                if (descr.isWorkingDay())
                    days.add(day);
                else
                    notWorkingDays.add(day);
            }
        }


        DateTime dt = new DateTime(calendarDay.getYear(), calendarDay.getMonth(), 1, 0, 0);
        for (int nday = 1; nday <= dt.dayOfMonth().withMaximumValue().getDayOfMonth(); nday++) {
            CalendarDay day = CalendarDay.from(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());

            if (!notWorkingDays.contains(day) && mPreferences.isWorkingDay(dt.getDayOfWeek())) {
                days.add(day);
            }
            dt = dt.plusDays(1);
        }

        Timber.d("Total %d working; %d in database", days.size(), list != null ? list.size() : 0);

        return days;
    }
}
