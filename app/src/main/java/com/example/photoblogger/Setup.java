package com.example.photoblogger;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Setup extends AppCompatActivity {
   private Toolbar toolbar;
   private CircleImageView circleImageView;
   private Button button;
   private EditText name;
   private Uri imguri=null;
   private StorageReference storageReference;
   private FirebaseAuth firebaseAuth;
   private ProgressBar progressBar;
   private String uid;
   private boolean ischange=false;
   private FirebaseFirestore db;
   private String myname;
    private Bitmap compressor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        uid=firebaseAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        name=findViewById(R.id.name);
        progressBar=findViewById(R.id.progressBar);
        circleImageView = findViewById(R.id.circleImageView);
        db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                       if(task.isSuccessful()){
                           if(task.getResult().exists()){
                               String nm= task.getResult().getString("name");
                               String img=task.getResult().getString("image");
                               name.setText(nm);
                               imguri=Uri.parse(img);
                               Glide.with(Setup.this).load(img).into(circleImageView);

                           }
                       }
            }
        });
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(Setup.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(Setup.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(Setup.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {

                        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(Setup.this);
                    }
                }
                else{
                    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(Setup.this);
                }
            }
        });
        button=findViewById(R.id.save);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  myname=name.getText().toString();
                  if(ischange){
                      if (!TextUtils.isEmpty(myname) && imguri != null) {
                          progressBar.setVisibility(ProgressBar.VISIBLE);
                          final StorageReference srf = storageReference.child("Profile-image").child(uid + ".jpg");
                          byte[] da=null;
                          try {
                              Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver() , imguri);
                              ByteArrayOutputStream baos = new ByteArrayOutputStream();
                              bitmap.compress(Bitmap.CompressFormat.JPEG, 15, baos);
                               da = baos.toByteArray();

                          } catch (IOException e) {
                              e.printStackTrace();
                          }
                          UploadTask uploadTask=srf.putBytes(da);
                          Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                              @Override
                              public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                  if (!task.isSuccessful()) {
                                      throw task.getException();
                                  }

                                  // Continue with the task to get the download URL
                                  return srf.getDownloadUrl();
                              }
                          }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                              @Override
                              public void onComplete(@NonNull Task<Uri> task) {
                                  if (task.isSuccessful()) {
                                      storefirestore(task,uid);
                                  } else {
                                      // Handle failures
                                      progressBar.setVisibility(ProgressBar.INVISIBLE);
                                  }
                              }
                          });
                      }
                  }
                  else{
                      storefirestore(null, uid);
                  }
            }
        });
    }
    protected void storefirestore(Task<Uri> task,String uid){
        Uri download;
        if(task!=null){
            download=task.getResult();
        }
        else{
            download=imguri;
        }
        Map<String,String> m=new HashMap<>();
        m.put("name",myname);
        m.put("image",download.toString());
        db.collection("users").document(uid).set(m).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                      if(task.isSuccessful()){
                          Toast.makeText(Setup.this,"Details has been updated",Toast.LENGTH_SHORT).show();
                          Intent i=new Intent(Setup.this,MainActivity.class);
                          startActivity(i);
                          finish();
                      }
                      else{
                          Toast.makeText(Setup.this,task.getException().getMessage().toString(),Toast.LENGTH_SHORT).show();
                      }
                      progressBar.setVisibility(ProgressBar.INVISIBLE);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imguri = result.getUri();
                ischange=true;
                circleImageView.setImageURI(imguri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}