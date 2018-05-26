package mvasoft.timetracker.core;


/**
 * Helper class that holds value, calculated by {@link ValueCalculator}.
 * Can invalidate their value by call {@link #invalidate()}
 * @param <T> type of containing value
 */
public class CalculatedValue<T> {

    private final ValueCalculator<T> mCalculator;
    private boolean mIsValid;
    private T mValue;

    public CalculatedValue(ValueCalculator<T> calculator) {
        mCalculator = calculator;
    }

    /**
     * Return value, calculated by {@link ValueCalculator#calculate()}
     * @return a calculated value
     */
    public T getValue() {
        if (!mIsValid) {
            mValue = mCalculator.calculate();
            mIsValid = true;
        }
        return mValue;
    }

    /**
     * Invalidate internal value.
     * On next call {@link #getValue()} value will be recalculated.
     */
    public void invalidate() {
        mIsValid = false;
    }

}
