package com.example.camera;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.camera.EditScreen.cropThenRotateBitmap;
import static com.example.camera.EditScreen.croppedBitmap;
import static com.example.camera.EditScreen.rotateBitmap;
import static com.example.camera.EditScreen.rotateThenCropBitmap;

public class Home extends AppCompatActivity {
    public static final int REQUEST_CODE = 105;
    ImageView Selfie;
    ImageView imageView;
    private Button Take_Selfie;
    private Button Gallery;
    File cameraImage;
    static Bitmap bitmap;
    static String imageFileName;
    static Uri uri;
    static Uri uri1;


    @RequiresApi(api = Build.VERSION_CODES.Q)


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            }
        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        System.out.println(currentPhotoPath);
        return image;
    }


    private void getPhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

                System.out.println(ex.getMessage());
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.camera.filesprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(takePictureIntent, 1);
            }
        }

    }


    public void makeBitmapNull() {
        croppedBitmap = null;
        rotateBitmap = null;
        cropThenRotateBitmap = null;
        rotateThenCropBitmap = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        imageView = findViewById(R.id.Selfie);
        Gallery = findViewById(R.id.btn2);
        Take_Selfie = findViewById(R.id.btn1);

        if (cropThenRotateBitmap != null) {
            imageView.setImageBitmap(cropThenRotateBitmap);

            makeBitmapNull();

        } else if (rotateThenCropBitmap != null) {
            imageView.setImageBitmap(rotateThenCropBitmap);

            makeBitmapNull();

        } else if (rotateBitmap != null) {
            imageView.setImageBitmap(rotateBitmap);

            makeBitmapNull();
        } else if (croppedBitmap != null) {
            imageView.setImageBitmap(croppedBitmap);

            makeBitmapNull();
        } else if (bitmap != null) {
            imageView.setImageBitmap(bitmap);

            makeBitmapNull();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)

    public void TakeSelfie(View view)  {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);

        } else {
            getPhoto();
        }
    }

    public static final int PICK_IMAGE = 2;

    public void Gallery(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 1 && resultCode == RESULT_OK) {

            try {
                bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                System.out.println(bitmap);


                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                ContentResolver resolver = getContentResolver();
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                OutputStream imageOutStream = null;

                try {
                    if (uri == null) {
                        throw new IOException("Failed to insert MediaStore row");
                    }

                    imageOutStream = resolver.openOutputStream(uri);

                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream)) {
                        throw new IOException("Failed to compress bitmap");
                    }


                    Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show();

                } finally {
                    if (imageOutStream != null) {
                        imageOutStream.close();


                    }
                    Intent intent = new Intent(this, EditScreen.class);

                    startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                assert data != null;
                uri1 = data.getData();
                System.out.println(uri1.toString());
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri1);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }


            Intent intent = new Intent(this, EditScreen.class);
            startActivity(intent);


        }

    }
}
