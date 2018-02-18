package com.example.popvuk.chatapp;

import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popvuk.vchat.R;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import custom.FileHandler;
import roundedimageview.RoundCornersImageView;


/**
 * Created by POPVUK on 8/25/2017.
 */

public class UserActvity extends AppCompatActivity {


    private String path;
    private WeakReference<RoundCornersImageView> img;
    private SharedPreferences my_prefs;
    private FileHandler fh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/ChatApp";

        RoundCornersImageView rciv = (RoundCornersImageView) findViewById(R.id.roundImg);
        img = new WeakReference<>(rciv);

        setProfilePhoto(path+"/"+getString(R.string.my_photo));

        final Button editUsername = (Button) findViewById(R.id.editUsername);
        Button editPhoto = (Button) findViewById(R.id.editPhoto);
        TextView username = (TextView) findViewById(R.id.textViewUsername);
        TextView phone = (TextView) findViewById(R.id.textViewPhone);

        my_prefs = getSharedPreferences(getString(R.string.my_prefs), Context.MODE_PRIVATE);
        username.setText(my_prefs.getString(getString(R.string.my_prefs_username),null));
        phone.setText(my_prefs.getString(getString(R.string.my_prefs_phone), null));

        editUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                EditUsernameDialog dialog = new EditUsernameDialog();
                dialog.show(fm, "Dialog");
            }
        });

        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
                {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                }
                else
                {
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();

            Intent crop = new Intent("com.android.camera.action.CROP");
            crop.setData(imageUri);
            crop.putExtra("crop", "true");
            crop.putExtra("aspectX", 1);
            crop.putExtra("aspectY", 1);
            crop.putExtra("outputX", 200);
            crop.putExtra("outputY", 200);
            crop.putExtra("noFaceDetection", true);
            crop.putExtra("return-data", true);

            try
            {
                startActivityForResult(crop, 2);
            }
            catch (ActivityNotFoundException ex)
            {
                Toast.makeText(this, "Your phone does not support croping",Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == 2 && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap bmp = extras.getParcelable("data");
            saveImage(path, bmp, getString(R.string.my_photo));
            setProfilePhoto(path+"/"+getString(R.string.my_photo));
        }
    }

    private void saveImage(String filePath, Bitmap finalBitmap, String fileName) {

        fh = new FileHandler(UserActvity.this);
        File file = fh.saveImageToExternalStorage(filePath, fileName, finalBitmap);
        fh.uploadPhotoToFirebase(file);
        SharedPreferences.Editor editor = my_prefs.edit();
        editor.putString(getString(R.string.my_prefs_photo), fileName);
        editor.commit();
    }

    private void setProfilePhoto(String filePath)
    {
        File imgFile = new  File(filePath);
        if ((imgFile.exists()))
        {
            Bitmap thumbBmp = BitmapFactory.decodeFile(filePath);
            RoundCornersImageView roundImage = img.get();
            roundImage.setImageBitmap(thumbBmp);
        }
    }
}
