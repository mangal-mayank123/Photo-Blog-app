package com.example.photoblogger;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private EditText user;
    private  EditText pwd;
    private Button login,register;
    private FirebaseAuth firebaseAuth;
    private  ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth=FirebaseAuth.getInstance();
        user=findViewById(R.id.name);
        pwd=findViewById(R.id.pwd);
        progressBar=findViewById(R.id.progress);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.INVISIBLE);
        login=findViewById(R.id.sign);
        register=findViewById(R.id.register);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name=user.getText().toString();
                String pass=pwd.getText().toString();
                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass)){
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseAuth.signInWithEmailAndPassword(name,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                               if(task.isSuccessful()){
                                   startActivity(new Intent(Login.this,MainActivity.class));
                                   finish();
                               }
                               else{
                                   Toast.makeText(Login.this,task.getException().getMessage().toString(),Toast.LENGTH_SHORT).show();
                               }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,Register.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            Intent i=new Intent(Login.this,MainActivity.class);
            startActivity(i);
            finish();
        }

    }
}
