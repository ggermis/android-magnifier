package org.codenut.app.magnifier;

public class Toggle<T> {
    private T mOnValue;
    private T mOffValue;

    public Toggle(final T onValue, final T offValue) {
        mOnValue = onValue;
        mOffValue = offValue;
    }

    public T toggle(final boolean off) {
        return off ? mOnValue : mOffValue;
    }
}
