package mvasoft.timetracker.ui.preferences;

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import mvasoft.timetracker.R;
import timber.log.Timber;

/**
 * Preference with seekBar widget. Based on SeekBarPreference from v7 support
 * library with some enhancements. Attributes defined in attrs.xml <p>
 * attr seekBarIncrement" format="integer" <p>
 * attr name="adjustable" format="boolean" <p>
 * attr name="showSeekBarValue" format="boolean" <p>
 * attr name="minValue" format="integer" <p>
 * attr name="maxValue" format="integer" <p>
 * attr name="stepValue" format="integer" <p>
 * attr name="segmented" format="boolean" <p>
 * attr name="autoUpdateSeekBarValue" format="boolean" <p>
 */
public class SliderPreference extends Preference {

    private final boolean mAutoUpdateSeekBarValue;
    private int mPrefValue;
    private int mMin;
    private int mMax;
    private int mStep;
    private int mSeekBarIncrement;
    private boolean mAdjustable; // whether the seekbar should respond to the left/right keys
    private final boolean mShowSeekBarValue; // whether to show the seekbar value TextView next to the bar

    private boolean mTrackingTouch;
    private SeekBar mSeekBar;
    private TextView mSeekBarValueTextView;


    /**
     * Listener reacting to the SeekBar changing value by the user
     */
    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && (mAutoUpdateSeekBarValue || !mTrackingTouch)) {
                syncValueInternal(seekBar);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = false;
            if (toPrefValue(seekBar.getProgress()) != mPrefValue) {
                syncValueInternal(seekBar);
            }
        }
    };

    /**
     * Listener reacting to the user pressing DPAD left/right keys if {@code
     * adjustable} attribute is set to true; it transfers the key presses to the SeekBar
     * to be handled accordingly.
     */
    private final View.OnKeyListener mSeekBarKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (!mAdjustable && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                // Right or left keys are pressed when in non-adjustable mode; Skip the keys.
                return false;
            }

            // We don't want to propagate the click keys down to the seekbar view since it will
            // create the ripple effect for the thumb.
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                return false;
            }

            if (mSeekBar == null) {
                Timber.e("SeekBar view is null and hence cannot be adjusted.");
                return false;
            }
            return mSeekBar.onKeyDown(keyCode, event);
        }
    };

    public SliderPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.SliderPreference, defStyleAttr, defStyleRes);

        /**
         * The ordering of these two statements are important. If we want to set mMax first, we need
         * to perform the same steps by changing mMin/mMax to mMax/mMin as following:
         * mMax = a.getInt(...) and setMin(...).
         */
        mMin = a.getInt(R.styleable.SliderPreference_minValue, 0);
        setMax(a.getInt(R.styleable.SliderPreference_maxValue, 100));
        setStep(a.getInt(R.styleable.SliderPreference_stepValue, 1));
        setSeekBarIncrement(a.getInt(R.styleable.SliderPreference_seekBarIncrement, 0));
        mAutoUpdateSeekBarValue = a.getBoolean(R.styleable.SliderPreference_autoUpdateSeekBarValue, true);
        mAdjustable = a.getBoolean(R.styleable.SliderPreference_adjustable, true);
        mShowSeekBarValue = a.getBoolean(R.styleable.SliderPreference_showSeekBarValue, true);
        a.recycle();
    }

    public SliderPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SliderPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.preference.R.attr.seekBarPreferenceStyle);
    }

    public SliderPreference(Context context) {
        this(context, null);
    }

    private int getSeekBarMax() {
        return (mMax - mMin) / mStep;
    }

    private int getSeekBarProgress() {
        return (mPrefValue - mMin) / mStep;
    }

    private int toPrefValue(int seekBarProgress) {
        return  (mMin / mStep + seekBarProgress) * mStep;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setOnKeyListener(mSeekBarKeyListener);
        mSeekBar = (SeekBar) view.findViewById(android.support.v7.preference.R.id.seekbar);
        mSeekBarValueTextView = (TextView) view.findViewById(android.support.v7.preference.R.id.seekbar_value);
        if (mShowSeekBarValue) {
            mSeekBarValueTextView.setVisibility(View.VISIBLE);
        } else {
            mSeekBarValueTextView.setVisibility(View.GONE);
            mSeekBarValueTextView = null;
        }

        if (mSeekBar == null) {
            Timber.e("SeekBar view is null in onBindViewHolder.");
            return;
        }
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mSeekBar.setMax(getSeekBarMax());
        // If the increment is not zero, use that. Otherwise, use the default mKeyProgressIncrement
        // in AbsSeekBar when it's zero. This default increment value is set by AbsSeekBar
        // after calling setMax. That's why it's important to call setKeyProgressIncrement after
        // calling setMax() since setMax() can change the increment value.
        if (mSeekBarIncrement != 0) {
            mSeekBar.setKeyProgressIncrement(mSeekBarIncrement);
        } else {
            mSeekBarIncrement = mSeekBar.getKeyProgressIncrement();
        }


        mSeekBar.setProgress(getSeekBarProgress());
        if (mSeekBarValueTextView != null) {
            mSeekBarValueTextView.setText(String.valueOf(mPrefValue));
        }
        mSeekBar.setEnabled(isEnabled());
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(mPrefValue)
                : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    public void setMin(int min) {
        if (min > mMax) {
            min = mMax;
        }
        if (min != mMin) {
            mMin = min;
            notifyChanged();
        }
    }

    public int getMin() {
        return mMin;
    }

    public final void setMax(int max) {
        if (max < mMin) {
            max = mMin;
        }
        if (max != mMax) {
            mMax = max;
            notifyChanged();
        }
    }

    private void setStep(int step) {
        step = Math.max(1, step);
        if (step > mMax) {
            step = mMax;
        }
        if (step != mStep) {
            mStep = step;
            notifyChanged();
        }
    }

    /**
     * Returns the amount of increment change via each arrow key click. This value is derived from
     * user's specified increment value if it's not zero. Otherwise, the default value is picked
     * from the default mKeyProgressIncrement value in {@link android.widget.AbsSeekBar}.
     * @return The amount of increment on the SeekBar performed after each user's arrow key press.
     */
    public final int getSeekBarIncrement() {
        return mSeekBarIncrement;
    }

    /**
     * Sets the increment amount on the SeekBar for each arrow key press.
     * @param seekBarIncrement The amount to increment or decrement when the user presses an
     *                         arrow key.
     */
    public final void setSeekBarIncrement(int seekBarIncrement) {
        if (seekBarIncrement != mSeekBarIncrement) {
            mSeekBarIncrement =  Math.min(mMax - mMin, Math.abs(seekBarIncrement));
            notifyChanged();
        }
    }

    public int getMax() {
        return mMax;
    }

    public void setAdjustable(boolean adjustable) {
        mAdjustable = adjustable;
    }

    public boolean isAdjustable() {
        return mAdjustable;
    }

    public void setValue(int seekBarValue) {
        setValueInternal(seekBarValue, true);
    }

    private void setValueInternal(int seekBarValue, boolean notifyChanged) {
        if (seekBarValue < mMin) {
            seekBarValue = mMin;
        }
        if (seekBarValue > mMax) {
            seekBarValue = mMax;
        }

        if (seekBarValue != mPrefValue) {
            mPrefValue = seekBarValue;
            if (mSeekBarValueTextView != null) {
                mSeekBarValueTextView.setText(String.valueOf(mPrefValue));
            }
            persistInt(seekBarValue);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    public int getValue() {
        return mPrefValue;
    }

    /**
     * Persist the seekBar's seekbar value if callChangeListener
     * returns true, otherwise set the seekBar's value to the stored value
     */
    private void syncValueInternal(SeekBar seekBar) {
        int prefValue = toPrefValue(seekBar.getProgress());
        if (prefValue != mPrefValue) {
            if (callChangeListener(prefValue)) {
                setValueInternal(prefValue, false);
            } else {
                seekBar.setProgress(getSeekBarProgress());
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.mPrefValue = mPrefValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mPrefValue = myState.mPrefValue;

        notifyChanged();
    }



    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        int mPrefValue;

        public SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            mPrefValue = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeInt(mPrefValue);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SliderPreference.SavedState createFromParcel(Parcel in) {
                        return new SliderPreference.SavedState(in);
                    }

                    @Override
                    public SliderPreference.SavedState[] newArray(int size) {
                        return new SliderPreference.SavedState[size];
                    }
                };
    }
}
