package org.codenut.app.magnifier;

import android.hardware.Camera;

public class Flasher {
    private boolean mEnabled = false;

    public String toggle() {
        final String result;
        if (mEnabled) {
            result = Camera.Parameters.FLASH_MODE_OFF;
            mEnabled = false;
        } else {
            result = Camera.Parameters.FLASH_MODE_TORCH;
            mEnabled = true;
        }
        return result;
    }
}
