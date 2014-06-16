package org.codenut.app.magnifier;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


public class PreviewImage {
    private static final int ANIMATION_DURATION = 2000;

    private File mDirectory;
    private ImageView mContainer;
    private String mName;

    public PreviewImage(final ImageView container, final File directory) {
        this(container, directory, UUID.randomUUID().toString());
    }

    public PreviewImage(final ImageView container, final File directory, final String name) {
        mContainer = container;
        mDirectory = directory;
        mName = name;
    }

    public void capture(final YuvImage image, final Camera.Size size) {
        ByteArrayOutputStream bos = null;
        FileOutputStream fos = null;
        try {

            Rect rectangle = new Rect();
            rectangle.bottom = size.height;
            rectangle.top = 0;
            rectangle.left = 0;
            rectangle.right = size.width;

            bos = new ByteArrayOutputStream();
            image.compressToJpeg(rectangle, 100, bos);

            fos = new FileOutputStream(getFullPath());
            bos.writeTo(fos);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    public void preview() {
        fadeIn(asBitmap());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOut();
            }
        }, ANIMATION_DURATION);
    }


    private String getName() {
        return mName;
    }

    private File getFullPath() {
        return new File(mDirectory, getName());
    }

    private Bitmap asBitmap() {
        File imgFile = new File(mDirectory, getName());
        return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    }

    private void fadeIn(final Bitmap bitmap) {
        Animation fadeIn = new AlphaAnimation(0.00f, 1.00f);
        fadeIn.setDuration(ANIMATION_DURATION);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        mContainer.setImageBitmap(bitmap);
        mContainer.startAnimation(fadeIn);
    }

    private void fadeOut() {
        Animation fadeOut = new AlphaAnimation(1.00f, 0.00f);
        fadeOut.setDuration(ANIMATION_DURATION);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mContainer.setVisibility(View.GONE);
            }
        });
        mContainer.startAnimation(fadeOut);
    }
}
