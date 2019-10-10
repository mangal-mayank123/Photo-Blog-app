package com.example.photoblogger;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
public class Home extends Fragment {
    private FirebaseFirestore firebaseFirestore;
     private RecyclerView recyclerView;
     private List<Blogpost> list;
     private Recycleradapter recycleradapter;
    private DocumentSnapshot lastvisible;
    private  boolean isfirstpage =true;

    public Home() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseFirestore =FirebaseFirestore.getInstance();
        View view=inflater.inflate(R.layout.fragment_home, container, false);
          recyclerView=view.findViewById(R.id.recycler1);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
          list=new ArrayList<>();
          recycleradapter=new Recycleradapter(list);
          recyclerView.setAdapter(recycleradapter);

        firebaseFirestore.collection("Posts").orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){
                    if(doc.getType()==DocumentChange.Type.ADDED){
                        String postid=doc.getDocument().getId();
                            list.add((Blogpost) doc.getDocument().toObject(Blogpost.class).withid(postid));

                        recycleradapter.notifyDataSetChanged();
                    }
                }

            }
        });

        return view;
    }
}
