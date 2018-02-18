package custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.popvuk.vchat.R;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by MiljanaMilena on 12/21/2017.
 */

public class FileHandler {

    private SharedPreferences my_prefs;
    private Context context;

    public FileHandler(Context ctx)
    {
        this.context = ctx;
    }

    public File saveImageToExternalStorage(String path, String fileName, Bitmap finalBitmap)
    {
        File myDir = new File(path);
        myDir.mkdirs();
        String filePath = path+"/"+fileName;
        File file = new File(filePath);
        if (file.exists())
            file.delete();

        try
        {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            Toast.makeText(context, "File not added! Try again", Toast.LENGTH_LONG).show();
        }
        // Tell the media scanner about the new file so that it is immediately available to the user.
        MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null,null);

        return file;
    }

    public void uploadPhotoToFirebase(File file)
    {
        Firebase.setAndroidContext(this.context);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("images");
        InputStream stream = null;
        try
        {
            stream = new FileInputStream(file);
        }
        catch (FileNotFoundException ex)
        {
            Toast.makeText(context, "Cannot find file to upload!", Toast.LENGTH_LONG).show();
        }

        my_prefs = this.context.getSharedPreferences(this.context.getString(R.string.my_prefs), Context.MODE_PRIVATE);
        final String myId = my_prefs.getString(context.getString(R.string.my_prefs_id), null);
        StorageReference mountainsRef = imagesRef.child(myId+".jpg");

        UploadTask uploadTask = mountainsRef.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Toast.makeText(context, "File is not uploaded"+ exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Firebase my_firebase = new Firebase(context.getString(R.string.firebase_users)+"/"+myId);
                Map<String, Object> image = new HashMap<>();
                image.put("photo", myId+".jpg");
                my_firebase.updateChildren(image);

                Toast.makeText(context, "File is uploaded", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void downloadPhotoFromFirebase(final String fileName, final String path)
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference().child("images/"+fileName);
        try
        {
            final File localFile = File.createTempFile("images", "jpg");
            imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("DownloadING...");
                    Bitmap bmp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    saveImageToExternalStorage(path, fileName, bmp);
                }
            });
        }
        catch (IOException ex)
        {
            Toast.makeText(context, "Error downloading file!", Toast.LENGTH_LONG).show();
        }


    }

}
