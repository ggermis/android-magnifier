package org.codenut.app.magnifier;

public class Toggle<T> {
    private boolean mOn = false;
    private T mOnValue;
    private T mOffValue;

    public Toggle(T onValue, T offValue) {
        mOnValue = onValue;
        mOffValue = offValue;
    }

    public T toggle() {
        T result;
        if (mOn) {
            result = mOffValue;
            mOn = false;
        } else {
            result = mOnValue;
            mOn = true;
        }
        return result;
    }
}
