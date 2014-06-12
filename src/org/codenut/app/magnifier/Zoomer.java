package org.codenut.app.magnifier;

public class Zoomer {
    private int mMaxZoom;
    private int mCurrentZoom;

    private static final int INCREMENT = 3;

    public Zoomer(final  int maxZoom) {
        mMaxZoom = maxZoom;
        mCurrentZoom = maxZoom;
    }

    public int getCurrentZoom() {
        return mCurrentZoom;
    }

    public int zoomIn() {
        mCurrentZoom += INCREMENT;
        if (mCurrentZoom > mMaxZoom) {
            mCurrentZoom -= INCREMENT;
        }
        return mCurrentZoom;
    }

    public int zoomOut() {
       mCurrentZoom -= INCREMENT;
        if (mCurrentZoom < 0) {
            mCurrentZoom += INCREMENT;
        }
        return mCurrentZoom;
    }
}
