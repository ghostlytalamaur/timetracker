package mvasoft.timetracker.ui.editdate;

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
import timber.log.Timber;

public class EditDateModel {

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

        final long today = System.currentTimeMillis() / 1000;
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
                .distinctUntilChanged()
                .flatMap(this::queryOrDefault)
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

    private Flowable<DayDescription> queryOrDefault(Long date) {
        DayDescription dd = new DayDescription(0, date,
                mPreferences.getTargetTimeInMin(),
                mPreferences.isWorkingDay(DateTimeHelper.dayOfWeek(date)));
        return Flowable.just(dd)
                .mergeWith(
                        mRepository.getDayDescriptionRx(date)
                                .doOnNext(descr -> Timber.d("Data received")));
    }

    public Flowable<Long> getId() {
        Timber.d("query observable: getId()");
        return mIdSubject;
    }

    public Flowable<Long> getDate() {
        return mDateSubject;
    }

    Flowable<Boolean> getIsChangedObservable() {
        Timber.d("query observable: getIsChangedObservable()");
        return mIsChanged;
    }

    Flowable<Long> getTargetMin() {
        Timber.d("query observable: getTargetMin()");
        return mTargetMin;
    }

    public Flowable<Boolean> getIsWorkingDay() {
        Timber.d("query observable: getIsWorkingDay()");
        return mIsWorkingDay;
    }

    long getCurTargetMin() {
        return mTargetMin.getValue();
    }

    public void setIsWorkingDay(boolean isWorkingDay) {
        if (mIsWorkingDay.getValue() == isWorkingDay)
            return;

        mIsWorkingDay.onNext(isWorkingDay);
        updateIsChanged();
    }

    void setTargetMinutes(long minutes) {
        if (mTargetMin.getValue() == minutes)
            return;

        mTargetMin.onNext(minutes);
        updateIsChanged();
    }

    private void updateIsChanged() {
        mIsChanged.onNext(!Objects.equals(mDayDescriptionSubj.getValue(), buildDayDescription()));
    }

    public void setDate(long date) {
        Timber.d("setDate()");
        mDateSubject.onNext(date);
    }


    private DayDescription buildDayDescription() {
        return new DayDescription(mDayDescriptionSubj.getValue().getId(),
                mDateSubject.getValue(), mTargetMin.getValue(), mIsWorkingDay.getValue());
    }

    void save() {
        if (!mIsChanged.getValue())
            return;

        Timber.d("save()");
        mRepository.updateDayDescription(buildDayDescription());
    }

    void clear() {
        if (mDisposable != null) {
            mDisposable.dispose();
            Timber.d("EditDateModel.clear()");
        }
        mDisposable = null;
    }
}
