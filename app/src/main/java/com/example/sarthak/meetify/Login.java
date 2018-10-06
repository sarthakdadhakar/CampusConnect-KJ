package com.example.sarthak.meetify;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    int RC_SIGN_IN = 1;
    FirebaseFirestore db;
    ScrollView sv;
    ProgressBar progressBar;
   private GoogleSignInClient mGoogleSignInClient;
    private Button signInButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d("here", "reached 1");
        db = FirebaseFirestore.getInstance();
        signInButton = findViewById(R.id.sign_in_button);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        sv=findViewById(R.id.scrollView);
        progressBar=findViewById(R.id.progBar);

        try
        {
            mGoogleSignInClient = GoogleSignIn.getClient(Login.this, gso);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            //Toast.makeText(this, e, Toast.LENGTH_SHORT).show();
        }

        mAuth = FirebaseAuth.getInstance();


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    private void updateUI(final FirebaseUser currentUser) {
        if(currentUser!=null)
        {
            progressBar.setVisibility(View.VISIBLE);
            sv.setVisibility(View.GONE);
            signInButton.setEnabled(false);



            DocumentReference docRef = db.collection("users").document(currentUser.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
//                            Toast.makeText(Login.this, "Document Exists", Toast.LENGTH_SHORT).show();
                            boolean ifcomplete = document.getBoolean("ifComplete");

                            if (!ifcomplete)
                            {
//                                Toast.makeText(Login.this, "NOT COMPLETE", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this,SignupComplete.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            }
                            else
                            {
//                                Toast.makeText(Login.this, "COMPLETE", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this,Homepage.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                            Log.d("doc", "DocumentSnapshot data: " + document.getData());
                        } else {
//                            Toast.makeText(Login.this, "Document NOT", Toast.LENGTH_SHORT).show();
                            Map<String, Object> data = new HashMap<>();
                            data.put("userId", currentUser.getUid());
                            data.put("ifComplete",false);
                            data.put("name",currentUser.getDisplayName());
                            db.collection("users").document(currentUser.getUid())
                                    .set(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent intent = new Intent(Login.this,SignupComplete.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                            Log.d("success", "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("fail", "Error writing document", e);
                                        }
                                    });
                        }
                    } else {
                        Log.d("error", "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("2", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("3", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            mGoogleSignInClient.revokeAccess();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("4", "signInWithCredential:failure", task.getException());
//                            Toast.makeText(Login.this, "Not Signed In!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
