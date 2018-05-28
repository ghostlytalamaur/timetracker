package mvasoft.timetracker.ui.editdate.model;

import android.util.Log;

import java.util.Objects;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.preferences.AppPreferences;
import mvasoft.timetracker.vo.DayDescription;

public class EditDateModel {

    public static final String LOGT = "mvasoft.log";

    private final AppPreferences mPreferences;
    private final DataRepository mRepository;
    private BehaviorSubject<DayDescription> mDayDescriptionSubj;
    private Flowable<DayDescription> mDayDescriptionFlowable;
    private BehaviorSubject<Long> mDateSubject;
    private BehaviorSubject<Long> mTargetMin;
    private BehaviorSubject<Boolean> mIsWorkingDay;
    private BehaviorSubject<Boolean> mIsChanged;


    public EditDateModel(DataRepository repository, AppPreferences preferences) {
        mRepository = repository;
        mPreferences = preferences;

        mDateSubject = BehaviorSubject.createDefault(System.currentTimeMillis() / 1000);
        mIsWorkingDay = BehaviorSubject.createDefault(false);
        mTargetMin = BehaviorSubject.createDefault(0L);
        mIsChanged = BehaviorSubject.createDefault(false);

        mDayDescriptionSubj = BehaviorSubject.createDefault(
                new DayDescription(0, 0, 0, false));

        mDayDescriptionFlowable = mDateSubject
                .skip(1)
                .switchMap(date -> mRepository.getDayDescriptionRx(date).toObservable())
                .filter(dayDescription -> !Objects.equals(dayDescription, mDayDescriptionSubj.getValue()))
                .doOnNext(dayDescription -> {
                    Log.d(LOGT, "mDayDescriptionFlowable.doOnNext() " + dayDescription.getDate());
                    mIsChanged.onNext(false);
                })
                .toFlowable(BackpressureStrategy.LATEST);

        Log.d(LOGT, "subscribe(mDayDescriptionSubj)");
        mDayDescriptionFlowable
                .toObservable()
                .subscribe(mDayDescriptionSubj);

        Log.d(LOGT, "subscribe(mTargetMin)");
        mDayDescriptionFlowable
                .filter(dayDescription -> !getIsChanged())
                .map(DayDescription::getTargetDuration)
                .toObservable()
                .subscribe(mTargetMin);

        Log.d(LOGT, "subscribe(mIsWorkingDay)");
        mDayDescriptionFlowable
                .filter(dayDescription -> !getIsChanged())
                .map(DayDescription::isWorkingDay)
                .toObservable()
                .subscribe(mIsWorkingDay);
    }

    public Observable<Long> getId() {
        Log.d(LOGT, "query observable: getId()");
        return mDayDescriptionSubj.map(DayDescription::getId);
    }

    public Observable<Boolean> getIsChangedObservable() {
        Log.d(LOGT, "query observable: getIsChangedObservable()");
        return mIsChanged;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean getIsChanged() {
        return mIsChanged.getValue();
    }

    public Observable<Long> getTargetMin() {
        Log.d(LOGT, "query observable: getTargetMin()");
        return mTargetMin;
    }

    public Observable<Boolean> getIsWorkingDay() {
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
        if (mDateSubject.getValue() != date) {
            Log.d(LOGT,"setDate()");
            mDateSubject.onNext(date);
        }
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
}
