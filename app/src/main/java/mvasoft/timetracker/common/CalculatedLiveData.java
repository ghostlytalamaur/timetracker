package mvasoft.timetracker.common;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

public class CalculatedLiveData<I, O> extends LiveData<O> {

    private final Function<I, O> mCalcFunc;
    private final LiveData<I> mDataSource;
    private final Observer<I> mDataSourceObserver;


    public CalculatedLiveData(LiveData<I> dataSource, Function<I, O> calcFunc) {
        mCalcFunc = calcFunc;
        mDataSourceObserver = new Observer<I>() {
            @Override
            public void onChanged(@Nullable I i) {
                invalidateValue();
            }
        };
        mDataSource = dataSource;
    }

    public void invalidateValue() {
        if (hasActiveObservers())
            postValue(mCalcFunc.apply(mDataSource.getValue()));
    }

    @Override
    protected void onActive() {
        super.onActive();
        invalidateValue();
        mDataSource.observeForever(mDataSourceObserver);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        invalidateValue();
        mDataSource.removeObserver(mDataSourceObserver);
    }

}
