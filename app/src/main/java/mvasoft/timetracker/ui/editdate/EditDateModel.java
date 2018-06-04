package mvasoft.timetracker.ui.editdate;

import android.util.Log;

import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.processors.BehaviorProcessor;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.DayDescription;

public class EditDateModel {

    private static final String LOGT = "mvasoft.log";

    private final AppPreferences mPreferences;
    private final DataRepository mRepository;

    private final BehaviorProcessor<Long> mDateSubject;

    private final BehaviorProcessor<DayDescription> mDayDescriptionSubj;
    private final BehaviorProcessor<Boolean> mIsChanged;

    private final BehaviorProcessor<Long> mIdSubject;
    private final BehaviorProcessor<Long> mTargetMin;
    private final BehaviorProcessor<Boolean> mIsWorkingDay;
    private Disposable mDisposable;

    @Inject
    EditDateModel(DataRepository repository, AppPreferences preferences) {
        mRepository = repository;
        mPreferences = preferences;

        long today = System.currentTimeMillis() / 1000;
        mDateSubject = BehaviorProcessor.createDefault(today);
        mIdSubject = BehaviorProcessor.createDefault(0L);
        mIsWorkingDay = BehaviorProcessor.createDefault(
                mPreferences.isWorkingDay(DateTimeHelper.dayOfWeek(today)));
        mTargetMin = BehaviorProcessor.createDefault(
                mPreferences.getTargetTimeInMin());
        mIsChanged = BehaviorProcessor.createDefault(false);

        mDayDescriptionSubj = BehaviorProcessor.createDefault(
                new DayDescription(0, 0, 0, false));

        ConnectableFlowable<DayDescription> dayConnectable = mDateSubject
                .skip(1)
                .distinctUntilChanged()
                .switchMap(mRepository::getDayDescriptionRx)
                .doOnNext(dd -> Log.d(LOGT, "New data received"))
                .distinctUntilChanged()
                .doOnNext(dd -> mIsChanged.onNext(false))
                .replay(1);

        dayConnectable
                .map(DayDescription::getTargetDuration)
                .subscribe(mTargetMin);
        dayConnectable
                .map(DayDescription::isWorkingDay)
                .subscribe(mIsWorkingDay);
        dayConnectable
                .map(DayDescription::getId)
                .subscribe(mIdSubject);
        dayConnectable
                .subscribe(mDayDescriptionSubj);
        mDisposable = dayConnectable.connect();
    }

    public Flowable<Long> getId() {
        Log.d(LOGT, "query observable: getId()");
        return mIdSubject;
    }

    public Flowable<Boolean> getIsChangedObservable() {
        Log.d(LOGT, "query observable: getIsChangedObservable()");
        return mIsChanged;
    }

    public Flowable<Long> getTargetMin() {
        Log.d(LOGT, "query observable: getTargetMin()");
        return mTargetMin;
    }

    public Flowable<Boolean> getIsWorkingDay() {
        Log.d(LOGT, "query observable: getIsWorkingDay()");
        return mIsWorkingDay;
    }

    public long getCurTargetMin() {
        return mTargetMin.getValue();
    }

    public void setIsWorkingDay(boolean isWorkingDay) {
        if (mIsWorkingDay.getValue() == isWorkingDay)
            return;

        mIsWorkingDay.onNext(isWorkingDay);
        updateIsChanged();
    }

    public void setTargetMinutes(long minutes) {
        if (mTargetMin.getValue() == minutes)
            return;

        mTargetMin.onNext(minutes);
        updateIsChanged();
    }

    private void updateIsChanged() {
        mIsChanged.onNext(!Objects.equals(mDayDescriptionSubj.getValue(), buildDayDescription()));
    }

    public void setDate(long date) {
        Log.d(LOGT,"setDate()");
        mDateSubject.onNext(date);
    }


    private DayDescription buildDayDescription() {
        return new DayDescription(mDayDescriptionSubj.getValue().getId(),
                mDateSubject.getValue(), mTargetMin.getValue(), mIsWorkingDay.getValue());
    }

    public void save() {
        if (!mIsChanged.getValue())
            return;

        Log.d(LOGT,"save()");
        mRepository.updateDayDescription(buildDayDescription());
    }

    public void clear() {
        if (mDisposable != null) {
            mDisposable.dispose();
            Log.d(LOGT, "EditDateModel.clear()");
        }
        mDisposable = null;
    }
}
