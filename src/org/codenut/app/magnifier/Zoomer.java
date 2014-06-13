package org.codenut.app.magnifier;

public class Zoomer {
    private static final int INCREMENT = 3;
    private int mMaxZoom;
    private int mCurrentZoom;

    public Zoomer(final int maxZoom) {
        mMaxZoom = maxZoom;
        setPercentage(getDefaultPercentage());
    }

    public int getCurrentZoom() {
        return mCurrentZoom;
    }

    public int setPercentage(final int percentage) {
        mCurrentZoom = mMaxZoom * percentage / 100;
        return mCurrentZoom;
    }

    public int getDefaultPercentage() {
        return 75;
    }
}
