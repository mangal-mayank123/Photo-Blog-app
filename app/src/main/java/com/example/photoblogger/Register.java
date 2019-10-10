package com.example.photoblogger;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {
    private EditText user;
    private  EditText pwd,cp;
    private Button login,register;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth=FirebaseAuth.getInstance();
        user=findViewById(R.id.name);
        pwd=findViewById(R.id.pwd);
        cp=findViewById(R.id.cpass);
        register=findViewById(R.id.signup);
        login=findViewById(R.id.sign);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name=user.getText().toString();
                String pass=pwd.getText().toString();
                String cpwd=cp.getText().toString();
                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pass)&&!TextUtils.isEmpty(cpwd)&&cpwd.equals(pass)){
                    firebaseAuth.createUserWithEmailAndPassword(name,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent i=new Intent(Register.this,Setup.class);
                                startActivity(i);
                                finish();
                            }
                            else  Toast.makeText(Register.this,task.getException().getMessage().toString(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else Toast.makeText(Register.this,"Password does not match",Toast.LENGTH_SHORT).show();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this,Login.class));
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            Intent i=new Intent(Register.this,MainActivity.class);
            startActivity(i);
            finish();
        }

    }
}
