package org.codenut.app.magnifier;

import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PreviewImage {
    private File mDirectory;
    private String mName;
    private ByteArrayOutputStream mCapture;

    public PreviewImage(final File directory) {
        this(directory, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-S").format(new Date()) + ".jpg");
    }

    public PreviewImage(final File directory, final String name) {
        mDirectory = directory;
        mName = name;
    }

    public ByteArrayOutputStream capture(final YuvImage image, final int width, final int height) {
        Rect rectangle = new Rect();
        rectangle.bottom = height;
        rectangle.top = 0;
        rectangle.left = 0;
        rectangle.right = width;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compressToJpeg(rectangle, 100, bos);

        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCapture = bos;
        return bos;
    }

    public void save() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(getFullPath());
            mCapture.writeTo(fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    private String getName() {
        return mName;
    }

    private File getFullPath() {
        return new File(mDirectory, getName());
    }
}
