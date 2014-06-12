package org.codenut.app.magnifier;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ZoomControls;

import java.io.IOException;
import java.util.List;


public class MagnifierFragment extends Fragment {
    private static final String TAG = "X";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private ZoomControls mZoom;
    private Button mLightButton;
    private Button mFreezeButton;
    private Zoomer mZoomer;
    private Flasher mFlasher;

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
                // We can no longer display on this surface, so stop the preview.
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                if (mCamera == null) return;
                // The surface has changed size; update the camera preview size
                Camera.Parameters p = mCamera.getParameters();
                Camera.Size s = getBestSupportedSize(p.getSupportedPreviewSizes(), w, h);
                p.setPreviewSize(s.width, s.height);
                if (p.isZoomSupported()) {
                    p.setZoom(mZoomer.getCurrentZoom());
                }
                mCamera.setParameters(p);
                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }
        });

        // configure zoom buttons
        mZoom = (ZoomControls) v.findViewById(R.id.zoom_control);
        mZoom.setOnZoomInClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Camera.Parameters p = mCamera.getParameters();
                if (p.isZoomSupported()) {
                    p.setZoom(mZoomer.zoomIn());
                }
                mCamera.setParameters(p);
                mCamera.startPreview();
            }
        });
        mZoom.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters p = mCamera.getParameters();
                if (p.isZoomSupported()) {
                    p.setZoom(mZoomer.zoomOut());
                }
                mCamera.setParameters(p);
                mCamera.startPreview();
            }
        });

        mFreezeButton = (Button)v.findViewById(R.id.button_freeze);
        mFreezeButton.setOnClickListener(new View.OnClickListener() {
            private boolean mFrozen = false;
            @Override
            public void onClick(View v) {
                if (mFrozen) {
                    mCamera.startPreview();
                    mFrozen = false;
                } else {
                    mCamera.stopPreview();
                    mFrozen = true;
                }
            }
        });
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            mLightButton = (Button) v.findViewById(R.id.button_light);
            mLightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Camera.Parameters p = mCamera.getParameters();
                    p.setFlashMode(mFlasher.toggle());
                    mCamera.setParameters(p);
                    mCamera.startPreview();
                }
            });
        }

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
        mZoomer = new Zoomer(mCamera.getParameters().getMaxZoom());
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
}
