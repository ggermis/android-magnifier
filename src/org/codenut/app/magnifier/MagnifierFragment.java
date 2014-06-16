package org.codenut.app.magnifier;

import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class MagnifierFragment extends Fragment {
    private static final String TAG = "org.codenut.app.magnifier";
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private SurfaceView mSurfaceView;
    private ImageView mPreview;
    private SeekBar mZoomSeeker;
    private Switch mLightButton;
    private Zoomer mZoomer;
    private boolean mFrozen = false;
    private Flasher mFlasher;
    private GestureDetector mGestureDetector;
    private YuvImage mFrozenImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // inflate the camera fragment
        View v = inflater.inflate(R.layout.camera_fragment, parent, false);

        // configure surface view
        mSurfaceView = (SurfaceView) v.findViewById(R.id.camera_surfaceView);
        final SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                // Tell the camera to use this surface as its preview area
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (IOException exception) {
                    Log.e(TAG, "Error setting up preview display", exception);
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                stopCameraPreview();
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                if (mCamera == null) return;
                // The surface has changed size; update the camera preview size
                Camera.Size s = getBestSupportedSize(mParameters.getSupportedPreviewSizes(), w, h);
                mParameters.setPreviewSize(s.width, s.height);
                if (mParameters.isZoomSupported()) {
                    mParameters.setZoom(mZoomer.getCurrentZoom());
                }
                mCamera.setParameters(mParameters);
                try {
                    startCameraPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }
        });

        mPreview = (ImageView) v.findViewById(R.id.preview);
        mPreview.setImageBitmap(loadImage());

        // configure zoom buttons
        mZoomSeeker = (SeekBar) v.findViewById(R.id.zoom_control);
        mZoomSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mParameters.isZoomSupported()) {
                    mParameters.setZoom(mZoomer.setPercentage(progress));
                }
                mCamera.setParameters(mParameters);
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

        mLightButton = (Switch) v.findViewById(R.id.button_light);
        mLightButton.setEnabled(true);
        mLightButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mParameters.setFlashMode(mFlasher.toggle());
                mCamera.setParameters(mParameters);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
        mParameters = mCamera.getParameters();
        mParameters.setPreviewFormat(ImageFormat.JPEG);
        mZoomer = new Zoomer(mParameters.getMaxZoom());
        mFlasher = new Flasher();
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
     * A simple algorithm to get the largest size available. For a more
     * robust version, see CameraPreview.java in the ApiDemos
     * sample app from Android.
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

    public void updateImagePreview() {
        imagePreviewFadeIn(loadImage());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                imagePreviewFadeOut();
            }
        }, 2000);
    }

    public Bitmap loadImage() {
        File imgFile = new File(getActivity().getFilesDir(), "test.jpg");
        return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    }

    public void imagePreviewFadeIn(Bitmap bitmap) {
        mPreview.setImageBitmap(bitmap);

        Animation fadeIn = new AlphaAnimation(0.00f, 1.00f);
        fadeIn.setDuration(2000);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
            }
        });
        mPreview.startAnimation(fadeIn);
    }

    public void imagePreviewFadeOut() {
        Animation fadeOut = new AlphaAnimation(1.00f, 0.00f);
        fadeOut.setDuration(2000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                mPreview.setVisibility(View.GONE);
            }
        });
        mPreview.startAnimation(fadeOut);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mFrozen) {
                captureScreen();
                updateImagePreview();
                startCameraPreview();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mFrozen) {
                startCameraPreview();
            } else {
                if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
                    mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                    mParameters.setPreviewFormat(ImageFormat.JPEG);
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
                } else {
                    stopCameraPreview();
                }
            }
            return false;
        }

        private void captureScreen() {
            String path = getActivity().getFilesDir() + "/test.jpg";
            try {
                Camera.Size size = mParameters.getPreviewSize();
                Rect rectangle = new Rect();
                rectangle.bottom = size.height;
                rectangle.top = 0;
                rectangle.left = 0;
                rectangle.right = size.width;

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                mFrozenImage.compressToJpeg(rectangle, 100, out);

                FileOutputStream fos = new FileOutputStream(new File(path));
                out.writeTo(fos);
                out.close();
                fos.close();
                mFrozenImage = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}