package website.bsagor.gallery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.takeimage.R;

public class GalleryActivity extends Activity implements AdapterView.OnItemClickListener {

	private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
	private Button btnSelect;
	private ImageView ivImage;
	private String userChoosenTask;

    List<GridViewItem> gridItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_activity);

        File file = new File(Environment.getExternalStorageDirectory() + "/BDTOURMATE/");
        if(file.exists()){
            setGridAdapter(file.getPath());
        }


		btnSelect = (Button) findViewById(R.id.btnSelectPhoto);
		btnSelect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                Log.d("Called", "btnSelect");
				selectImage();

			}
		});
		ivImage = (ImageView) findViewById(R.id.ivImage);
	}

    private void setGridAdapter(String path) {
        // Create a new grid adapter
        gridItems = createGridItems(path);
        ImageGridAdapter adapter = new ImageGridAdapter(this, gridItems);

        // Set the grid adapter
        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(adapter);

        // Set the onClickListener
        gridView.setOnItemClickListener(this);
    }

    private List<GridViewItem> createGridItems(String directoryPath) {
        List<GridViewItem> items = new ArrayList<GridViewItem>();

        // List all the items within the folder.
        File[] files = new File(directoryPath).listFiles(new ImageFileFilter());
        for (File file : files) {

            // Add the directories containing images or sub-directories
            if (file.isDirectory()
                    && file.listFiles(new ImageFileFilter()).length > 0) {

                items.add(new GridViewItem(file.getAbsolutePath(), true, null));
            }
            // Add the images
            else {
                Bitmap image = BitmapHelper.decodeBitmapFromFile(file.getAbsolutePath(),
                        50,
                        50);
                items.add(new GridViewItem(file.getAbsolutePath(), false, image));
            }
        }

        return items;
    }

    private boolean isImageFile(String filePath) {
        if (filePath.endsWith(".jpg") || filePath.endsWith(".png"))
        // Add other formats as desired
        {
            return true;
        }
        return false;
    }

    @Override
    public void
    onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("gridItems", gridItems.get(position).getPath());

        shareImage(gridItems.get(position).getPath());

        if (gridItems.get(position).isDirectory()) {
            setGridAdapter(gridItems.get(position).getPath());
        }
        else {
            // Display the image
        }

    }

    @Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					if(userChoosenTask.equals("Take Photo"))
						cameraIntent();
					else if(userChoosenTask.equals("Choose from Library"))
//						galleryIntent();
                    openFolder();
				} else {
					//code for deny
				}
				break;
		}
	}

	private void selectImage() {
		final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };

		AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
		builder.setTitle("Add Photo!");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				boolean result=Utility.checkPermission(GalleryActivity.this);

				if (items[item].equals("Take Photo")) {
					userChoosenTask ="Take Photo";
					if(result)
						cameraIntent();

				} else if (items[item].equals("Choose from Library")) {
					userChoosenTask ="Choose from Library";
					if(result)
                        Log.d("Called", "userChoosenTask");
//						galleryIntent();
                    openFolder();

				} else if (items[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	/*private void galleryIntent()
	{
		Intent intent = new Intent();
		intent.setType("image*//*");
		intent.setAction(Intent.ACTION_GET_CONTENT);//
		startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
	}*/

	private void cameraIntent()
	{
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, REQUEST_CAMERA);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SELECT_FILE) {
                Log.d("Called", "SELECT_FILE");
                onSelectFromGalleryResult(data);
            }
			else if (requestCode == REQUEST_CAMERA)
				onCaptureImageResult(data);
		}
	}

	private void onCaptureImageResult(Intent data) {
		Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

		File file = new File(Environment.getExternalStorageDirectory() + "/BDTOURMATE");
        File destination = new File(file + "/", System.currentTimeMillis() + ".jpg");

		if(!file.exists()) {
            file.mkdir();
            Log.d("Called", "file::" + destination.getAbsolutePath());
        }

		FileOutputStream fo;
        if(!destination.exists()){
            try {
                destination.createNewFile();
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

		ivImage.setImageBitmap(thumbnail);
        setGridAdapter(file.getPath());
	}

	@SuppressWarnings("deprecation")
	private void onSelectFromGalleryResult(Intent data) {
        Log.d("Called", "onSelectFromGalleryResult");
		Bitmap bm=null;
		if (data != null) {
			try {
				bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ivImage.setImageBitmap(bm);
	}


    public void openFolder() {
        Log.d("Called", "openFolder");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/BDTOURMATE/");
        intent.setDataAndType(uri, "image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }


    private class ImageFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            else if (isImageFile(file.getAbsolutePath())) {
                return true;
            }
            return false;
        }
    }

    private void shareImage(String imgPath) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        File imageFileToShare = new File(imgPath);
        Uri uri = Uri.fromFile(imageFileToShare);
        share.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(share, "Share Image!"));
    }

}
