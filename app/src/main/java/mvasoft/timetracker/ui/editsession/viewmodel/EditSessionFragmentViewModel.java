package mvasoft.timetracker.ui.editsession.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import mvasoft.timetracker.BR;
import mvasoft.timetracker.data.DataRepository;
import mvasoft.timetracker.ui.common.BaseViewModel;
import mvasoft.timetracker.ui.editsession.model.SessionEditModel;
import mvasoft.timetracker.utils.DateTimeFormatters;


public class EditSessionFragmentViewModel extends BaseViewModel {

    private final DateTimeFormatters mFormatter;
    private final SessionEditModel mData;
    private MutableLiveData<Boolean> mIsChangedLiveData;

    private final DataRepository mRepository;

    @Inject
    EditSessionFragmentViewModel(@NonNull Application application, DataRepository repository) {
        super(application);
        mRepository = repository;

        mFormatter = new DateTimeFormatters();
        mData = new SessionEditModel(mRepository);
        mData.addDataChangedListener(new SessionDataChangedListener());
    }

    public SessionEditModel getModel() {
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
        return mFormatter.formatDuration(mData.getDuration());
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
        if (getModel().getSession() != null)
            return mRepository.updateSession(getModel().getSessionForUpdate());
        else {
            MutableLiveData<Boolean> res = new MutableLiveData<>();
            res.setValue(false);
            return res;
        }
    }

    private class SessionDataChangedListener implements SessionEditModel.ISessionDataChangedListener {

        @Override
        public void dataChanged(SessionEditModel.SessionDataType dataType) {
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
