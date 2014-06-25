package org.codenut.app.magnifier;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


public class MagnifierFragment extends Fragment {
    private static final String TAG = "MagnifierFragment";
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private PreviewImage mPreviewImage;
    private ImageView mCapturedImageContainer;
    private boolean mFrozen = false;
    private GestureDetector mGestureDetector;
    private Slider mZoomSlider;
    private VerticalSeekBar mSeekBar;
    private ToggleButton mFlashToggleButton;
    private ToggleButton mFocusToggleButton;
    private ToggleButton mNegativeToggleButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera_fragment, parent, false);

        final SurfaceView surfaceView = (SurfaceView) v.findViewById(R.id.camera_surfaceView);
        SurfaceHolder holder = surfaceView.getHolder();
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

                Camera.Size s = getOptimalPreviewSize(mParameters.getSupportedPreviewSizes(), w, h);
                mParameters.setPreviewSize(s.width, s.height);
                if (CameraCapabilities.isZoomSupported(mParameters)) {
                    mParameters.setZoom(mZoomSlider.getCurrentValue());
                }
                mCamera.setParameters(mParameters);

                if (!CameraCapabilities.isFocusSupported(mParameters)) {
                    mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                }
                if (!CameraCapabilities.isZoomSupported(mParameters)) {
                    mSeekBar.setVisibility(View.GONE);
                }
                if (!CameraCapabilities.isFlashSupported(mParameters)) {
                    mFlashToggleButton.setVisibility(View.GONE);
                }
                if (!CameraCapabilities.isNegativeEffectSupported(mParameters)) {
                    mNegativeToggleButton.setVisibility(View.GONE);
                }
                if (!CameraCapabilities.isFocusSupported(mParameters)) {
                    mFocusToggleButton.setVisibility(View.GONE);
                }

                startCameraPreview();
            }
        });
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        surfaceView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        mCapturedImageContainer = (ImageView) v.findViewById(R.id.capture);
        mCapturedImageContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        mCapturedImageContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        mSeekBar = (VerticalSeekBar) v.findViewById(R.id.zoom_control);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (CameraCapabilities.isZoomSupported(mParameters)) {
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

        mFlashToggleButton = (ToggleButton) v.findViewById(R.id.toggleFlashButton);
        mFlashToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mFrozen) {
                            startCameraPreview();
                        }
                        Toggle<String> flashToggle = new Toggle<String>(Camera.Parameters.FLASH_MODE_TORCH, Camera.Parameters.FLASH_MODE_OFF);
                        mParameters.setFlashMode(flashToggle.toggle(isChecked));
                        mCamera.setParameters(mParameters);
                    }
                });
            }
        });

        mNegativeToggleButton = (ToggleButton) v.findViewById(R.id.toggleNegativeButton);
        mNegativeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mFrozen) {
                            startCameraPreview();
                        }
                        Toggle<String> negativeToggle = new Toggle<String>(Camera.Parameters.EFFECT_NEGATIVE, Camera.Parameters.EFFECT_NONE);
                        mParameters.setColorEffect(negativeToggle.toggle(isChecked));
                        mCamera.setParameters(mParameters);
                    }
                });
            }
        });

        mFocusToggleButton = (ToggleButton) v.findViewById(R.id.toggleFocusButton);
        mFocusToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mFrozen) {
                            startCameraPreview();
                        }
                        if (CameraCapabilities.isFocusSupported(mParameters)) {
                            Toggle<String> focusModeToggle = new Toggle<String>(Camera.Parameters.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_MACRO);
                            mParameters.setFocusMode(focusModeToggle.toggle(isChecked));
                            mCamera.setParameters(mParameters);
                        }
                    }
                });
            }
        });

        final ImageView galleryButton = (ImageView) v.findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCameraPreview();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        Fragment fragment = new GridViewFragment();
                        fm.beginTransaction()
                                .setCustomAnimations(R.animator.load_gallery, R.animator.unload_gallery, R.animator.load_gallery, R.animator.unload_gallery)
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commitAllowingStateLoss();
                    }
                });
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
        mParameters = mCamera.getParameters();
        mParameters.setPreviewFormat(ImageFormat.NV21);
        Camera.Size size = getWorkablePictureSize();
        mParameters.setPictureSize(size.width, size.height);
        mZoomSlider = new Slider(mParameters.getMaxZoom());
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Size getWorkablePictureSize() {
        Camera.Size size = null;
        for (Camera.Size s : mParameters.getSupportedPictureSizes()) {
            size = s;
            if (s.width < 1300) {
                break;
            }
        }
        return size;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
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

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mFrozen && mPreviewImage.hasHighResolutionImage()) {
                mPreviewImage.saveHighResolutionImage();
                Toast toast = Toast.makeText(getActivity(), getString(R.string.gallery_saved), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment fragment = ImagePreviewFragment.newInstance(mPreviewImage.getFullPath());
                fm.beginTransaction()
                        .setCustomAnimations(R.animator.load_gallery, R.animator.unload_gallery, R.animator.load_gallery, R.animator.unload_gallery)
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss();
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mFrozen) {
                startCameraPreview();
            } else {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (!CameraCapabilities.isFocusSupported(mParameters) || success) {
                            mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
                                @Override
                                public void onPreviewFrame(byte[] data, Camera camera) {
                                    final Camera.Size size = mParameters.getPreviewSize();
                                    stopCameraPreview();
                                    mPreviewImage = new PreviewImage(getActivity().getFilesDir());
                                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                                        @Override
                                        public void onPictureTaken(byte[] data, Camera camera) {
                                            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
                                            bos.write(data, 0, data.length);

                                            mPreviewImage.setHighResolutionImage(bos);
                                            mCapturedImageContainer.setImageBitmap(BitmapUtil.decodeSampledBitmapFromBitmap(data, size.width, size.height));

                                            try {
                                                bos.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
            return true;
        }
    }
}