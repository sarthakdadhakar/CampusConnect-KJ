package com.example.sarthak.meetify;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddTask extends AppCompatActivity {

    FirebaseFirestore db;


    EditText etTask,etAssigned;
    Button btAdd,btCancel;
    FloatingActionButton fabAdd;
    TextView tvToDo;
    String meetingId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        db=FirebaseFirestore.getInstance();
        etTask = findViewById(R.id.etTask);
        etAssigned = findViewById(R.id.etAssigned);
        btAdd = findViewById(R.id.btAdd);
        btCancel = findViewById(R.id.btCancel);
        fabAdd = findViewById(R.id.fabAdd);
        tvToDo = findViewById(R.id.tvToDo);

        meetingId = getIntent().getStringExtra("meetingId");


        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String task = etTask.getText().toString();
                final String assigned = etAssigned.getText().toString();

                Map<String, Object> data = new HashMap<>();
                data.put("assigned", assigned);
                data.put("task",task);


                db.collection("meeting").document(meetingId).collection("toDo")
                        .add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                Toast.makeText(AddTask.this, "meetingId "+documentReference.getId(), Toast.LENGTH_SHORT).show();
                                Log.d("success", "DocumentSnapshot written with ID: " + documentReference.getId());

                                tvToDo.setText(tvToDo.getText()+"\n"+task+"\t"+assigned);
                                etTask.setText("");
                                etAssigned.setText("");
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

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTask.setText("");
                etAssigned.setText("");
            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(AddTask.this,Homepage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            }
        });


    }
}
