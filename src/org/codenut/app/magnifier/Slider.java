package org.codenut.app.magnifier;

public class Slider {
    private static final int DEFAULT_LEVEL = 75;
    private final int mMaxValue;
    private int mCurrentValue;

    public Slider(final int maxValue) {
        mMaxValue = maxValue;
        setLevel(DEFAULT_LEVEL);
    }

    public int getCurrentValue() {
        return mCurrentValue;
    }

    public int setLevel(final int percentage) {
        mCurrentValue = mMaxValue * percentage / 100;
        return mCurrentValue;
    }
}
