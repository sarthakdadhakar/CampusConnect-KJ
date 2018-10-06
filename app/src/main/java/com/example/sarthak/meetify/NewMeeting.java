package com.example.sarthak.meetify;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewMeeting extends AppCompatActivity {
    FirebaseFirestore db;
    String myid;
    List<User> userList=new ArrayList<>();
    RecyclerView rList;
    ChatAdapter chatAdapter;
    FloatingActionButton fab;
    EditText etName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting);
        db=FirebaseFirestore.getInstance();
        myid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        rList = findViewById(R.id.rList);
        rList.setLayoutManager(new LinearLayoutManager(NewMeeting.this));
        rList.setItemAnimator(new DefaultItemAnimator());
        fab=findViewById(R.id.fab);
        etName=findViewById(R.id.meetingName);
        db.collection("users")
                .whereEqualTo("ifComplete", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                               String user_id=document.getString("userId");
                               if(user_id!=myid);
                                {
                                    User user = new User();
                                    user.setName(document.getString("name"));
                                    user.setUserId(document.getString("userId"));
                                    userList.add(user);
                                }
                            }
                            chatAdapter = new ChatAdapter(NewMeeting.this, userList);
                            rList.setAdapter(chatAdapter);
                            Log.d("list size",userList.size()+"");
                        } else {
                            Log.d("failure", "Error getting documents: ", task.getException());
                        }
                    }
                });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> members = new ArrayList<>();
                for(User user: userList)
                {
                    if(user.getSelected())
                    {
                        Log.d("member",user.getName());
                        members.add(user.getUserId());
                    }

                }
                Toast.makeText(NewMeeting.this, "Members: "+members.size(), Toast.LENGTH_SHORT).show();
                Map<String, Object> data = new HashMap<>();
                data.put("members", members);
                data.put("name",etName.getText().toString());
                Date c = Calendar.getInstance().getTime();
                System.out.println("Current time => " + c);

                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                String formattedDate = df.format(c);
                data.put("date",new Date(formattedDate).toString());
                db.collection("meeting")
                        .add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Intent intent = new Intent(NewMeeting.this,Meeting.class);
                                intent.putExtra("agenda",etName.getText().toString());
                                intent.putExtra("meetingId",documentReference.getId());
                                Toast.makeText(NewMeeting.this, "Done", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                                Toast.makeText(NewMeeting.this, "meetingId "+documentReference.getId(), Toast.LENGTH_SHORT).show();
                                Log.d("success", "DocumentSnapshot written with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("failure", "Error adding document", e);
                            }
                        });

            }
        });
    }

}
