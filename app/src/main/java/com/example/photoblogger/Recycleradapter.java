package com.example.photoblogger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class Recycleradapter extends RecyclerView.Adapter<Recycleradapter.ViewHolder> {
     public  List<Blogpost>list;
     private Context context;
    private FirebaseAuth firebaseAuth;
     FirebaseFirestore firebaseFirestore;
    public Recycleradapter(List<Blogpost> list) {
            this.list=list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.newpost,parent,false);
        context=parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
          String data=list.get(position).getDesc();
          holder.setdesc(data);
          String imgurl=list.get(position).getImageurl();
          final String postid=list.get(position).postid;
          holder.setimagepost(imgurl);
          Date time=list.get(position).getTime();
        if (time != null) {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
            String creationDate = dateFormat.format(time);
            holder.setdate(creationDate);
        }
        final String id=list.get(position).getUid();
        firebaseFirestore.collection("users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                     if(task.isSuccessful()){
                          String name =task.getResult().getString("name");
                         String pic=task.getResult().getString("image");
                         holder.setdata(name,pic);
                     }
            }
        });
         final String currentuser=firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Posts/"+ postid +"/Likes").document(currentuser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()) {
                    holder.like.setImageDrawable(context.getDrawable(R.mipmap.baseline_favorite_border_black_17dp));
                } else {
                    holder.like.setImageDrawable(context.getDrawable(R.mipmap.baseline_favorite_border_black_18dp));
                }
            }
        });
         holder.like.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             firebaseFirestore.collection("Posts/"+ postid +"/Likes").document(currentuser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                 @Override
                 public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                     if(!task.getResult().exists()){
                         Map<String,Object> m=new HashMap<>();
                         m.put("time", FieldValue.serverTimestamp());
                         firebaseFirestore.collection("Posts/"+ postid +"/Likes").document(currentuser).set(m);
                         firebaseFirestore.collection("Posts").document(postid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                             @Override
                             public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                 if(task.isSuccessful()){
                                     String uid=task.getResult().getString("uid");
                                     String desc=task.getResult().getString("desc");
                                     Map<String,Object> p=new HashMap<>();
                                     p.put("time",FieldValue.serverTimestamp());
                                     p.put("desc",desc);
                                     p.put("pid",postid);
                                     p.put("uid",currentuser);
                                     firebaseFirestore.collection("users/"+uid+"/Notifications").document(currentuser).set(p);
                                 }
                             }
                         });


                     }
                     else {
                         firebaseFirestore.collection("Posts/"+ postid +"/Likes").document(currentuser).delete();
                     }
                 }
             });

         }
     });


        firebaseFirestore.collection("Posts/"+ postid +"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                          if(!queryDocumentSnapshots.isEmpty()){
                              int count=queryDocumentSnapshots.size();
                              holder.setlikes(count);
                          }
                          else{
                              holder.setlikes(0);
                          }
            }
        });

       holder.comment.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent i=new Intent(context,Comment.class);
               i.putExtra("postid",postid);
               context.startActivity(i);
           }
       });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View root;
        public TextView desc,dat,username,number;
        public ImageView imageView,like,comment;
        private CircleImageView circleImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            root=itemView;
            like=root.findViewById(R.id.like);
            number=root.findViewById(R.id.number);
            comment=root.findViewById(R.id.comment);
        }
        public void setdesc(String de){
            desc=root.findViewById(R.id.desc2);
            desc.setText(de);
        }

        public void setimagepost(String imgurl) {
            imageView=root.findViewById(R.id.imageView);
            Glide.with(context).load(imgurl).into(imageView);
        }

        public void setdate(String date) {
            dat=root.findViewById(R.id.Date);
            dat.setText(date);
        }

        public void setdata(String name, String pic) {
              circleImageView=root.findViewById(R.id.circleImageView2);
              username=root.findViewById(R.id.username);
              username.setText(name);
              Glide.with(context).load(pic).into(circleImageView);
        }

        public void setlikes(int i) {
            number.setText(i+" Likes");
        }
    }

}
