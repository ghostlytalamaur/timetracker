package mvasoft.timetracker.ui;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.SessionEditData;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.base.BaseViewModel;


public class SessionEditViewModel extends BaseViewModel {

    private DateTimeFormatters mFormatter;
    private SessionEditData mData;
    private MutableLiveData<Boolean> mIsChangedLiveData;

    private DataRepository mRepository;

    @Inject
    SessionEditViewModel(@NonNull Application application, DataRepository repository) {
        super(application);
        mRepository = repository;

        mFormatter = new DateTimeFormatters();
        mData = new SessionEditData(mRepository);
        mData.addDataChangedListener(new SessionDataChangedListener());
    }

//    public void setModel(SessionEditData model) {
//        mData = model;
//        mData.addDataChangedListener(new SessionDataChangedListener());
//    }


    public SessionEditData getModel() {
        return mData;
    }

    @Bindable
    public String getStartTime() {
        return mFormatter.formatDate(mData.getStartTime()) + " " + mFormatter.formatTime(mData.getStartTime());
    }

    @Bindable
    public String getEndTime() {
        return mFormatter.formatDate(mData.getEndTime()) + " " + mFormatter.formatTime(mData.getEndTime());
    }

    @Bindable
    public String getDuration() {
        return mFormatter.formatPeriod(mData.getDuration());
    }

    @Bindable
    public boolean getIsChanged() {
        return mData.isChanged();
    }

    @Bindable
    public boolean getIsClosed() {
        return mData.isClosed();
    }

    public void setIsClosed(boolean isClosed) {
        mData.setIsClosed(isClosed);
    }

    public LiveData<Boolean> getIsChangedLiveData() {
        if (mIsChangedLiveData == null) {
            mIsChangedLiveData = new MutableLiveData<>();
            mIsChangedLiveData.setValue(getIsChanged());
        }
        return mIsChangedLiveData;
    }

    public LiveData<Boolean> saveSession() {
        return mRepository.updateSession(getModel().getSession());
    }

    private class SessionDataChangedListener implements SessionEditData.ISessionDataChangedListener {

        @Override
        public void dataChanged(SessionEditData.SessionDataType dataType) {
            switch (dataType) {
                case sdtAll:
                    notifyPropertyChanged(BR._all);
                    break;
                case sdtStartTime:
                    notifyPropertyChanged(BR.startTime);
                    break;
                case sdtEndTime:
                    notifyPropertyChanged(BR.endTime);
                    break;
                case sdtClosed:
                    notifyPropertyChanged(BR.isClosed);
                    ((MutableLiveData<Boolean>) getIsChangedLiveData()).setValue(getIsChanged());
                    break;
            }

            notifyPropertyChanged(BR.isChanged);
            if (mIsChangedLiveData != null)
                mIsChangedLiveData.setValue(getIsChanged());
        }
    }
}
