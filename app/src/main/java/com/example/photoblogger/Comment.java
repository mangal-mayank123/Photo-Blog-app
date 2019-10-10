package com.example.photoblogger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class Comment extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    private List<Comment_model> list;
    private RecyclerView recyclerView;
    private String postid;
    private Button comment;
    private EditText msg;
    private FirebaseUser firebaseUser;
    private String uid;
    private CommentRecycler commentRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        list=new ArrayList<>();
        Intent i =getIntent();
        postid=i.getStringExtra("postid");
        recyclerView =findViewById(R.id.commentrecycler);
        commentRecycler=new CommentRecycler(list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(commentRecycler);
        comment=findViewById(R.id.postcomment);
        msg=findViewById(R.id.writecomment);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        uid=firebaseUser.getUid();

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message=msg.getText().toString();
                if(!TextUtils.isEmpty(message)&&!TextUtils.isEmpty(uid)){
                    Map<String,Object> m=new HashMap<>();
                    m.put("message",message);
                    m.put("username",uid);
                    m.put("time",FieldValue.serverTimestamp());
                    firebaseFirestore.collection("Posts/"+postid+"/Comment").document(uid).set(m).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Comment.this, "Commented Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                                overridePendingTransition( 0, 0);
                                startActivity(getIntent());
                                overridePendingTransition( 0, 0);
                            }
                        }
                    });
                }
            }
        });

        firebaseFirestore.collection("Posts/" +postid +"/Comment").orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    for(DocumentChange documentChangeme:queryDocumentSnapshots.getDocumentChanges()){
                        if(documentChangeme.getType()==DocumentChange.Type.ADDED){
                            list.add(documentChangeme.getDocument().toObject(Comment_model.class));
                            commentRecycler.notifyDataSetChanged();

                        }
                    }
                }
            }
        });

    }

}
class CommentRecycler extends RecyclerView.Adapter<CommentRecycler.ViewHolder> {

    private Context context;
    private FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    private List<Comment_model> list;
    String name;


    public CommentRecycler(List<Comment_model> list) {
        this.list=list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.commentview,parent,false);
        firebaseAuth=FirebaseAuth.getInstance();
        context=parent.getContext();
        firebaseFirestore =FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final String comm,uid;
            uid=list.get(position).getUsername();
            comm=list.get(position).getMessage();
        firebaseFirestore.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    name=task.getResult().getString("name");

                    holder.setdata(name,comm);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public   View root;
        public TextView username,comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            root=itemView;
        }

        public void setdata(String name, String comm) {
            username=root.findViewById(R.id.commenter);
            comment=root.findViewById(R.id.showcomment);
            username.setText(name);
            comment.setText(comm);
        }
    }
}