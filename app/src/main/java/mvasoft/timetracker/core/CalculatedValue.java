package mvasoft.timetracker.core;

public class CalculatedValue<T> {

    private final ValueCalculator<T> mCalculator;
    private boolean mIsValid;
    private T mValue;

    public CalculatedValue(ValueCalculator<T> calculator) {
        mCalculator = calculator;
    }

    public T getValue() {
        if (!mIsValid) {
            mValue = mCalculator.calculate();
            mIsValid = true;
        }
        return mValue;
    }

    public void invalidate() {
        mIsValid = false;
    }

    public interface ValueCalculator<T> {
        T calculate();
    }
}
