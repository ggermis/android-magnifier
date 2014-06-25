package org.codenut.app.magnifier;

import android.hardware.Camera;

import java.util.Arrays;

public class CameraCapabilities {

    public static boolean isZoomSupported(final Camera.Parameters params) {
        return params.isZoomSupported();
    }

    public static boolean isNegativeEffectSupported(final Camera.Parameters params) {
        return params.getSupportedColorEffects().contains(Camera.Parameters.EFFECT_NEGATIVE);
    }

    public static boolean isFocusSupported(final Camera.Parameters params) {
        return params.getSupportedFocusModes().containsAll(Arrays.asList(Camera.Parameters.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_MACRO));
    }

    public static boolean isFlashSupported(final Camera.Parameters params) {
        return params.getSupportedFlashModes() != null && params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH);
    }

}
