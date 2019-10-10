package com.example.photoblogger;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class Post extends AppCompatActivity {
   private Toolbar toolbar;
   private ImageView img=null;
    private ProgressBar progressBar;
   private EditText info;
   private Button upload;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private Uri posturi;
    private Bitmap compressor;
     Uri u ;
     String uurl ;
    private String uid;
    private String name;
    private static int MAX_LENGTH=200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        toolbar=findViewById(R.id.posttool);
        setSupportActionBar(toolbar);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        final FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        upload=findViewById(R.id.post);
        uid=firebaseUser.getUid();
        img=findViewById(R.id.postimg);
        progressBar=findViewById(R.id.progressBar2);
        info=findViewById(R.id.desc);
        progressBar.setVisibility(ProgressBar.INVISIBLE);



        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(Post.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Post.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {

                        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(Post.this);
                    }
                }
                else{
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(Post.this);
                }
            }
        });





        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc=info.getText().toString();
                if(!TextUtils.isEmpty(desc)&&img!=null){
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                     name= UUID.randomUUID().toString();
                    final StorageReference srf=storageReference.child("post-images").child(name + "jpg");
                    UploadTask uploadTask=srf.putFile(posturi);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return srf.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                try {
                                    compressor=new Compressor(Post.this)
                                            .setMaxWidth(200)
                                            .setMaxHeight(150)
                                            .setQuality(5)
                                            .compressToBitmap(new File(posturi.getPath()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressor.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();
                                name=UUID.randomUUID().toString();
                                final StorageReference thumb=storageReference.child("post-images/thumbs").child(name+"jpg");
                                UploadTask up=thumb.putBytes(data);

                                Task<Uri> task1=up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }
                                        return thumb.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isSuccessful()){
                                            u =task.getResult();
                                            uurl =u.toString();
                                        }
                                        else {
                                            Toast.makeText(Post.this, task.getException().getMessage()+"11111111", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                Uri downloadUri = task.getResult();
                                String downloadURL = downloadUri.toString();
                                Map<String,Object> m=new HashMap<>();
                                m.put("desc",desc);
                                m.put("imageurl",downloadURL);
                                m.put("uid",uid);
                                m.put("time",FieldValue.serverTimestamp());
                                m.put("thumb",uurl);
                                firebaseFirestore.collection("Posts").add(m).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                           if(task.isSuccessful()){
                                               Toast.makeText(Post.this, "Blog has been posted", Toast.LENGTH_SHORT).show();
                                               startActivity(new Intent(Post.this,MainActivity.class));
                                               finish();
                                           }
                                           else{
                                               Toast.makeText(Post.this, task.getException().getMessage()+"222222222", Toast.LENGTH_SHORT).show();
                                           }
                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                    }
                                });

                            } else {
                                Toast.makeText(Post.this, task.getException().getMessage()+"33333333", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                posturi = result.getUri();
                img.setImageURI(posturi);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
