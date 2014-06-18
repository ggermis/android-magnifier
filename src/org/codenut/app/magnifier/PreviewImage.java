package org.codenut.app.magnifier;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PreviewImage {
    private static final int ANIMATION_DURATION = 2000;
    private File mDirectory;
    private String mName;

    public PreviewImage(final File directory) {
        this(directory, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-S").format(new Date()) + ".jpg");
    }

    public PreviewImage(final File directory, final String name) {
        mDirectory = directory;
        mName = name;
    }

    public void capture(final YuvImage image, final int width, final int height) {
        ByteArrayOutputStream bos = null;
        FileOutputStream fos = null;
        try {

            Rect rectangle = new Rect();
            rectangle.bottom = height;
            rectangle.top = 0;
            rectangle.left = 0;
            rectangle.right = width;

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

    public void preview(final ImageView container) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return asBitmap();
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                applyFadeEffect(container, bitmap);
            }
        }.execute();
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

    private void applyFadeEffect(final ImageView container, final Bitmap bitmap) {
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
                fadeOut(container);
            }
        });
        container.setImageBitmap(bitmap);
        container.startAnimation(fadeIn);
    }

    private void fadeOut(final ImageView container) {
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
                container.setVisibility(View.GONE);
            }
        });
        container.startAnimation(fadeOut);
    }
}
