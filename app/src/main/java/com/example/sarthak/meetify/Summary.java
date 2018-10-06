package com.example.sarthak.meetify;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Summary extends AppCompatActivity {
    FirebaseFirestore db;
    private JSONObject j;
    ArrayList<Dialogue> final_results = new ArrayList<>();
    StringBuffer for_summ=new StringBuffer();
    ArrayList<String> convo = new ArrayList<String>();
    private String agenda;
    TextView tvAgenda,tvTranscript,tvConversation,tvKeywords,tvLabel;
    TextView  tvToDo;
    Button btnProceed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        j=new JSONObject();
        tvAgenda=findViewById(R.id.tvAgenda);
        tvTranscript=findViewById(R.id.tvTranscript);
        tvConversation=findViewById(R.id.tvConversation);
        tvKeywords=findViewById(R.id.tvKeywords);
        tvLabel=findViewById(R.id.tvLabel);
        btnProceed=findViewById(R.id.btnProceed);
        db = FirebaseFirestore.getInstance();
        /*ArrayList<String> convo = getIntent().getStringArrayListExtra("conversation");
        String for_summ = getIntent().getStringExtra("transcript");
        ArrayList<String> string_speech = getIntent().getStringArrayListExtra("memberText");*/
        final_results = getIntent().getParcelableArrayListExtra("final_results");
        final String meetingId = getIntent().getStringExtra("meetingId");
        agenda = getIntent().getStringExtra("agenda");
        tvAgenda.setText(agenda);
        Log.d("test", "final result_size: " + final_results.size());
        //    Log.d("string_trans",for_summ);

        TreeMap<Integer, String> all_membertext = new TreeMap<>();
        for (Dialogue dg : final_results) {
            Log.d("test", "speakr: " + dg.getSpeakerId() + "msg: " + dg.getTranscript());
            if (all_membertext.containsKey(dg.getSpeakerId())) {
                String text = all_membertext.get(dg.getSpeakerId());
                StringBuffer res = new StringBuffer(text);
                res.append(dg.getTranscript());
                all_membertext.put(dg.getSpeakerId(), res.toString());
            } else {
                all_membertext.put(dg.getSpeakerId(), dg.getTranscript());
            }
            //speech.get(dg.getSpeakerId()).append(dg.getTranscript());
            for_summ.append(dg.getTranscript() + " ");
            convo.add("Speaker "+(dg.getSpeakerId()+1) + " : " + dg.getTranscript());
            Log.d("test", ".");
        }


        getData(for_summ.toString());
        Log.d("test", "trans: " + for_summ);

        ArrayList<String> string_speech = new ArrayList<String>(all_membertext.values());


        Log.d("test", "Sending to FB");
        Map<String, Object> data = new HashMap<>();
        data.put("conversation", convo);
        data.put("transcript", for_summ.toString());
        data.put("memberText", string_speech);
        db.collection("meeting").document(meetingId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Summary.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("test", "Fire failed");
                        e.printStackTrace();
                        Toast.makeText(Summary.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });

        StringBuffer sb=new StringBuffer();
        for (String s : convo) {
            sb.append(s);
            sb.append('\n');
        }
        tvConversation.setText(sb.toString());

        tvTranscript.setText(for_summ.toString());

        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Summary.this,AddTask.class);
                intent.putExtra("meetingId",meetingId);
                startActivity(intent);
            }
        });

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
