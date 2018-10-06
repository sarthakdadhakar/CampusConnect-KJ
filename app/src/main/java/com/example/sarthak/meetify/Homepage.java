package com.example.sarthak.meetify;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Homepage extends AppCompatActivity {
    FirebaseFirestore db;
    RecyclerView rList;
    ArrayList<QueryDocumentSnapshot> nList = new ArrayList<>();
    MeetingAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db=FirebaseFirestore.getInstance();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        rList= findViewById(R.id.rList);
        rList.setLayoutManager(new LinearLayoutManager(Homepage.this));
        rList.setItemAnimator(new DefaultItemAnimator());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Homepage.this,NewMeeting.class);
               // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        db.collection("meeting")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                nList.add(document);
                                Log.d("success", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d("failure", "Error getting documents: ", task.getException());
                        }

                        mAdapter = new MeetingAdapter(Homepage.this, nList);
                        rList.setAdapter(mAdapter);
                        Log.d("list size",nList.size()+"");
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

}
