package website.bsagor.gallery;

import android.graphics.Bitmap;

/**
 * Created by medisys-java on 20-Nov-16.
 */
public class GridViewItem {
    private String path;
    private boolean isDirectory;
    private Bitmap image;


    public GridViewItem(String path, boolean isDirectory, Bitmap image) {
        this.path = path;
        this.isDirectory = isDirectory;
        this.image = image;
    }


    public String getPath() {
        return path;
    }


    public boolean isDirectory() {
        return isDirectory;
    }


    public Bitmap getImage() {
        return image;
    }
}
