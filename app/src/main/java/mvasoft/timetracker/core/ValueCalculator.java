package mvasoft.timetracker.core;

@FunctionalInterface
public interface ValueCalculator<T> {
    T calculate();
}
