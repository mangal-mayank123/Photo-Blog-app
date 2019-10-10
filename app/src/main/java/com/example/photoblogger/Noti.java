package com.example.photoblogger;



import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class Noti extends Fragment {
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth;
    private NotificationAdapter notificationAdapter;
    public List<Notification> list;
    public Noti() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_noti, container, false);
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        recyclerView=view.findViewById(R.id.notirecycler);
        list=new ArrayList<>();
        notificationAdapter=new NotificationAdapter(list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(notificationAdapter);
        String cid=firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("users/"+cid+"/Notifications").orderBy("time", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                      if(!queryDocumentSnapshots.isEmpty()){
                          for(DocumentChange documentChangeme:queryDocumentSnapshots.getDocumentChanges()){
                              if(documentChangeme.getType()==DocumentChange.Type.ADDED){
                                  list.add(documentChangeme.getDocument().toObject(Notification.class));
                                  notificationAdapter.notifyDataSetChanged();
                              }
                          }
                      }
            }
        });
        return view;
    }

}
class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    public List<Notification> list;
    private Context context;
    private String cid;

    public NotificationAdapter(List<Notification> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        View view=LayoutInflater.from(context).inflate(R.layout.notiview,parent,false);

        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        cid=firebaseAuth.getCurrentUser().getUid();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

           firebaseFirestore.collection("users").document(cid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
               @Override
               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                   if(task.isSuccessful()){
                       final String name1=task.getResult().getString("name");
                       String cid2=list.get(position).getUid();
                       if(!TextUtils.isEmpty(cid2)){
                       firebaseFirestore.collection("users").document(cid2).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                               if(task.isSuccessful()){
                                   String name2=task.getResult().getString("name");
                                   String desc=list.get(position).getDesc();
                                   holder.setnoti(name1,name2,desc);
                               }
                           }
                       });
                   }}
               }
           });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
        }

        public void setnoti(String name1, String name2, String desc) {
        textView=view.findViewById(R.id.Notification);
        textView.setText(name2+" likes your "+"'"+desc+"' photo");
        }
    }
}