package org.codenut.app.magnifier;

import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;

import java.io.IOException;
import java.util.List;


public class MagnifierFragment extends Fragment {
    private static final String TAG = "org.codenut.app.magnifier";
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private ImageView mPreviewImageContainer;
    private PreviewImage mPreviewImage;
    private boolean mFrozen = false;
    private YuvImage mFrozenImage;
    private GestureDetector mGestureDetector;
    private Slider mZoomSlider;
    private Toggle<String> mFlashToggle;
    private Toggle<String> mNegativeToggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera_fragment, parent, false);

        final SurfaceView surfaceView = (SurfaceView) v.findViewById(R.id.camera_surfaceView);
        final SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                if (mCamera != null) {
                    try {
                        mCamera.setPreviewDisplay(holder);
                    } catch (IOException exception) {
                        Log.e(TAG, "Error setting up preview display", exception);
                    }
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                stopCameraPreview();
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                if (mCamera == null) return;

                Camera.Size s = getBestSupportedSize(mParameters.getSupportedPreviewSizes(), w, h);
                mParameters.setPreviewSize(s.width, s.height);
                if (mParameters.isZoomSupported()) {
                    mParameters.setZoom(mZoomSlider.getCurrentValue());
                }
                mCamera.setParameters(mParameters);
                startCameraPreview();
            }
        });

        mPreviewImageContainer = (ImageView) v.findViewById(R.id.preview);

        final SeekBar zoomSeeker = (SeekBar) v.findViewById(R.id.zoom_control);
        zoomSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mParameters.isZoomSupported()) {
                    mParameters.setZoom(mZoomSlider.setLevel(progress));
                    mCamera.setParameters(mParameters);
                }
                if (mFrozen) {
                    startCameraPreview();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector.onTouchEvent(event)) {
                    return false;
                }
                return true;
            }
        });

        final Switch lightButton = (Switch) v.findViewById(R.id.button_light);
        lightButton.setEnabled(true);
        lightButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mParameters.setFlashMode(mFlashToggle.toggle());
                mCamera.setParameters(mParameters);
            }
        });

        final Switch negativeButton = (Switch) v.findViewById(R.id.button_negative);
        negativeButton.setEnabled(true);
        negativeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mParameters.setColorEffect(mNegativeToggle.toggle());
                mCamera.setParameters(mParameters);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            mCamera = Camera.open();
        }
        mParameters = mCamera.getParameters();
        mParameters.setPreviewFormat(ImageFormat.NV21);
        mZoomSlider = new Slider(mParameters.getMaxZoom());
        mFlashToggle = new Toggle<String>(Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF);
        mNegativeToggle = new Toggle<String>(Camera.Parameters.EFFECT_NEGATIVE, Camera.Parameters.EFFECT_NONE);
        mPreviewImage = new PreviewImage(mPreviewImageContainer, getActivity().getFilesDir(), "test.jpg");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * A simple algorithm to get the largest size available.
     * For a more robust version, see CameraPreview.java in the ApiDemos sample app from Android.
     */
    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }

    private void stopCameraPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mFrozen = true;
        }
    }

    private void startCameraPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            mFrozen = false;
        }
    }

    private void captureScreen() {
        mPreviewImage.capture(mFrozenImage, mParameters.getPreviewSize());
    }

    private void showImagePreview() {
        mPreviewImage.preview();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mFrozen) {
                captureScreen();
                showImagePreview();
                startCameraPreview();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mFrozen) {
                startCameraPreview();
            } else {
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
                                @Override
                                public void onPreviewFrame(byte[] data, Camera camera) {
                                    Camera.Size size = mParameters.getPreviewSize();
                                    mFrozenImage = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                                    stopCameraPreview();
                                }
                            });
                        }
                    }
                });
            }
            return false;
        }
    }

}