package com.example.photoblogger;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private  FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FloatingActionButton fab;
    String uid;
    private BottomNavigationView bottomNavigationView;
    private Home home;
    private Noti noti;
    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView=findViewById(R.id.bottom);
        home=new Home();
        noti=new Noti();
        account=new Account();
        replace(home);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        fab=findViewById(R.id.floatingActionButton);
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            Intent intent1=new Intent(MainActivity.this,Login.class);
            startActivity(intent1);
            finish();
        }
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  startActivity(new Intent(MainActivity.this,Post.class));
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.home:
                        replace(home);
                        return true;
                    case R.id.account:
                        replace(account);
                        return true;
                    case R.id.noti:
                        replace(noti);
                        return true;

                }
                return true;
            }
        });
    }

    private void replace(Fragment f) {
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mylayout,f);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(MainActivity.this,Login.class));
        }
        else{
            uid=firebaseUser.getUid();
            firebaseFirestore.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                     if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            startActivity(new Intent(MainActivity.this,Setup.class));
                        }
                     }
                     else{

                     }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.logout){
            firebaseAuth.signOut();
            Intent intent1=new Intent(MainActivity.this,Login.class);
            startActivity(intent1);

        }
        if(item.getItemId()==R.id.account){
            startActivity(new Intent(MainActivity.this,Setup.class));

        }
        return true;
    }
}

