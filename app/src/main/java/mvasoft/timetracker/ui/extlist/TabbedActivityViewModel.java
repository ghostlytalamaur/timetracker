package mvasoft.timetracker.ui.extlist;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import javax.inject.Inject;

import dagger.Lazy;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.utils.DateTimeFormatters;
import mvasoft.timetracker.utils.DateTimeHelper;
import mvasoft.timetracker.vo.Session;

public class TabbedActivityViewModel extends BaseViewModel {


    private final Lazy<DataRepository> mRepository;
    private final DateTimeFormatters mFormatter;
    private LiveData<Long> mOpenedSessionId;
    private final MutableLiveData<Long> mDate;
    private final LiveData<String> mTitleData;

    @Inject
    TabbedActivityViewModel(@NonNull Application application, Lazy<DataRepository> repository) {
        super(application);
        mRepository = repository;
        mFormatter = new DateTimeFormatters();
        mDate = new MutableLiveData<>();
        mDate.setValue(System.currentTimeMillis() / 1000);
        mTitleData = Transformations.map(mDate, date ->
                mFormatter.formatDate(Objects.requireNonNull(date)));
    }

    public void toggleSession() {
        mRepository.get().toggleSession();
    }

    public LiveData<Long> getOpenedSessionsId() {
        if (mOpenedSessionId == null)
            mOpenedSessionId = LiveDataReactiveStreams.fromPublisher(
                    mRepository.get().getOpenedSessionIdRx());

        return mOpenedSessionId;
    }

    public LiveData<String> getTitleData() {
        return mTitleData;
    }

    public LiveData<Long> getDate() {
        return mDate;
    }

    public void setDate(int year, int month, int dayOfMonth) {
        mDate.setValue(DateTimeHelper.getUnixTime(year, month, dayOfMonth));
    }

    public void restoreState(Bundle state) {
        SavedState data = Objects.requireNonNull(state.getParcelable("TabbedActivityViewModelState"));
        data.restore(this);
    }

    public void saveState(Bundle outState) {
        outState.putParcelable("TabbedActivityViewModelState", new SavedState(this));
    }

    public void fillFakeSessions() {
        DateTime day = new DateTime(System.currentTimeMillis())
                .minusYears(1)
                .monthOfYear()
                .withMinimumValue()
                .withTime(8, 0, 0, 0);

        ArrayList<Session> list = new ArrayList<>();
        Random rnd = new Random();
        while (day.getMillis() < System.currentTimeMillis()) {
            long start = day.withTime(8, 0, 0, 0).getMillis() / 1000;
            long end = day.withTime(16 + rnd.nextInt(1),
                    rnd.nextInt(60), 0, 0).getMillis() / 1000;

            list.add(new Session(0, start, end));
            day = day.plusDays(1);
            if (day.getDayOfWeek() == 6)
                    day = day.plusDays(2);
        }
        mRepository.get().appendAll(list);
    }

    static class SavedState implements Parcelable {
        long date;

        SavedState(Parcel in) {
            date = in.readLong();
        }

        SavedState(TabbedActivityViewModel data) {
            date = Objects.requireNonNull(data.getDate().getValue());
        }

        void restore(TabbedActivityViewModel dest) {
            if (Objects.requireNonNull(dest.mDate.getValue()) != date)
                dest.mDate.setValue(date);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(date);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
