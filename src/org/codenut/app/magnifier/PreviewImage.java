package org.codenut.app.magnifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PreviewImage {
    private File mDirectory;
    private String mName;
    private ByteArrayOutputStream mHighResolutionImage;

    public PreviewImage(final File directory) {
        this(directory, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-S", Locale.getDefault()).format(new Date()) + ".jpg");
    }

    public PreviewImage(final File directory, final String name) {
        mDirectory = directory;
        mName = name;
    }

    public static String getDatePart(final File file) {
        return file.getName().substring(0, 10);
    }

    public static String getTimePart(final File file) {
        return file.getName().substring(11, 19).replace('-', ':');
    }

    public void setHighResolutionImage(final ByteArrayOutputStream out) {
        mHighResolutionImage = out;
    }

    public void saveHighResolutionImage() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(getFullPath());
            mHighResolutionImage.writeTo(fos);
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

    public boolean hasHighResolutionImage() {
        return mHighResolutionImage != null;
    }

    private String getName() {
        return mName;
    }

    public File getFullPath() {
        return new File(mDirectory, getName());
    }
}
