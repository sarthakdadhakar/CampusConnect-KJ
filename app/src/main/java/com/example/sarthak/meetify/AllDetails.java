package com.example.sarthak.meetify;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
//import com.anychart.sample.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class AllDetails extends AppCompatActivity {
    private JSONObject j;
    private String meetingId;
    FirebaseFirestore db;
    DocumentSnapshot doc;
    TextView tvAgenda,tvTranscript,tvConversation,tvKeywords,tvLabel,tvTodo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_details);
        j=new JSONObject();
        db=FirebaseFirestore.getInstance();
        meetingId=getIntent().getStringExtra("meetingId");

        tvAgenda=findViewById(R.id.tvAgenda);
        tvTranscript=findViewById(R.id.tvTranscript);
        tvConversation=findViewById(R.id.tvConversation);
        tvKeywords=findViewById(R.id.tvKeywords);
        tvLabel=findViewById(R.id.tvLabel);
        tvTodo=findViewById(R.id.tvTodo);



    final DocumentReference docRef = db.collection("meeting").document(meetingId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        doc = document;
                        getData(doc.getString("transcript"));

                        Log.d("doc yes", "DocumentSnapshot data: " + document.getData());

                        tvAgenda.setText(doc.getString("name"));
                        tvTranscript.setText(doc.getString("transcript"));
                        StringBuffer sb = new StringBuffer();
                        ArrayList<String> conversations = (ArrayList<String>) doc.get("conversation");
                        for (String s : conversations) {
                            sb.append(s);
                            sb.append('\n');
                        }
                        tvConversation.setText(sb.toString());

                        db.collection("meeting").document(meetingId).collection("toDo")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                        StringBuffer bs = new StringBuffer();

                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot fDoc : task.getResult()) {
                                                Log.d("success", fDoc.getId() + " => " + fDoc.getData());
                                                bs.append(fDoc.getString("task")+"\t"+fDoc.getString("assigned")+"\n");

                                            }
                                            tvTodo.setText(bs.toString());
                                        } else {
                                            Log.d("failure", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });

                    }
                }
                        else {
                    Log.d("failure", "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alldetails_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                sendData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void sendData()
    {
//CALL ON ONCLICK
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.setPackage("com.google.android.gm");
        String shareBody = tvTranscript.getText().toString();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, tvAgenda.getText().toString());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(sharingIntent);
    }

    void getData(final String trans)
    {

        try
        {
            j.put("sentiment", new JSONObject());
            j.put("keywords", new JSONObject());
        }
        catch (Exception e)
        {
            Log.e("error",e.toString());
        }

        String url ="https://gateway.watsonplatform.net/natural-language-understanding/api/v1/analyze?version=2018-03-19";
        final String uname="9c550e9e-62c9-4b7f-95ac-ad5c41cd3a43";
        final String pwd="KdXgo2605JUn";
        StringRequest sr = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject result = null;

                try {
                    result = new JSONObject(response);
                    JSONObject senti = result.getJSONObject("sentiment").getJSONObject("document");
                    String score= String.valueOf(senti.get("score"));
                    String label= senti.getString("label");

                    JSONArray tokenList = result.getJSONArray("keywords");
                    StringBuffer sb = new StringBuffer();
                    for(int i=0;i<tokenList.length();i++)
                    {
                        JSONObject obj = tokenList.getJSONObject(i);
                        sb.append(obj.getString("text")+"\n");
                    }

                    tvKeywords.setText(sb.toString());
                    tvLabel.setText(label + " "+score);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error", String.valueOf(error));
            }
        })
        {

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("text", trans);
                    jsonObject.put("features", come());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject.toString().getBytes();
            }

            private JSONObject come() throws JSONException {
                JSONObject params = new JSONObject();
                params.put( "keywords", new JSONObject());
                params.put("sentiment", new JSONObject());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                String fauth=uname+":"+pwd;
                String nauth="Basic "+ Base64.encodeToString(fauth.getBytes(),Base64.NO_WRAP);
                params.put("Authorization",nauth);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(sr);

    }
}

