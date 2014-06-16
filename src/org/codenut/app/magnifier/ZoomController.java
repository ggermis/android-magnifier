package org.codenut.app.magnifier;

public class ZoomController {
    private int mMaxZoom;
    private int mCurrentZoom;

    public ZoomController(final int maxZoom) {
        mMaxZoom = maxZoom;
        setPercentage(getDefaultZoomLevel());
    }

    public int getCurrentZoom() {
        return mCurrentZoom;
    }

    public int setPercentage(final int percentage) {
        mCurrentZoom = mMaxZoom * percentage / 100;
        return mCurrentZoom;
    }

    private int getDefaultZoomLevel() {
        return 75;
    }
}
