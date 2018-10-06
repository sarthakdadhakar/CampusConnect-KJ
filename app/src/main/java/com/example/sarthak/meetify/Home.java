package com.example.sarthak.meetify;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {
    Button btnLogout;
    TextView tv1,tv2;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        tv1=findViewById(R.id.tvName);
        tv2=findViewById(R.id.tvEmail);
        String name = user.getDisplayName();
        tv1.setText(name);
        String email = user.getEmail();
        tv2.setText(email);
        btnLogout = (Button) findViewById(R.id.btnLogut);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(Home.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
