package com.example.sarthak.meetify;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechTimestamp;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;


public class Meeting extends AppCompatActivity {

    private String meetingId;

    private Button go;

    private static final String TAG = "A";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int RECORD_REQUEST_CODE = 101;
    private boolean permissionToRecordAccepted = false;
    Button btnRecord;
    EditText inputMessage;
    private boolean listening = false;
    private SpeechToText speechService;
    private MicrophoneInputStream capture;
    private SpeakerLabelsDiarization.RecoTokens recoTokens;
    private String STT_username="ac81e274-2d57-4665-a0a7-1b255087d94a",STT_password="qOvbzQIGIFcx";
    private  ArrayList dialoguelist;
    // GLOBAL DATA - STRUCTURES
    private ArrayList<MyTranscripts> all_timestamps = new ArrayList<>();
    private ArrayList<AllSpeakers> all_speakers = new ArrayList<>();
    private ArrayList < Dialogue > final_results = new ArrayList<>();
    private RecyclerView rview;
    private DialogueAdapter dAdapter;
    private String agenda;
    GifImageView gifImageView;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        db=FirebaseFirestore.getInstance();
        meetingId = getIntent().getStringExtra("meetingId");
        agenda = getIntent().getStringExtra("agenda");
        btnRecord=  findViewById(R.id.btn_record);
        gifImageView = findViewById(R.id.gif_img);
        inputMessage = (EditText) findViewById(R.id.message);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                recordMessage();
            }
        });
        rview = findViewById(R.id.rList);
        rview.setLayoutManager(new LinearLayoutManager(Meeting.this));
        rview.setItemAnimator(new DefaultItemAnimator());
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        }


        go=findViewById(R.id.go);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceed();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case RECORD_REQUEST_CODE: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user");
                } else {
                    Log.i(TAG, "Permission has been granted by user");
                }
                return;
            }
        }
        if (!permissionToRecordAccepted ) finish();
    }
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_REQUEST_CODE);
    }

    //Record a message via Watson Speech to Text
    private void recordMessage() {
        //mic.setEnabled(false);
        speechService = new SpeechToText();
        speechService.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");
        speechService.setUsernameAndPassword(STT_username, STT_password);
        if(listening != true) {
            btnRecord.setText("Stop Meeting");
            gifImageView.setVisibility(View.VISIBLE);
            btnRecord.setCompoundDrawablesWithIntrinsicBounds(R.drawable.stop,0,0,0);
            capture = new MicrophoneInputStream(true);
            new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        speechService.recognizeUsingWebSocket(capture, getRecognizeOptions(), new MicrophoneRecognizeDelegate());
                    } catch (Exception e) {
                        showError(e);
                    }
                }
            }).start();
            listening = true;
            Toast.makeText(Meeting.this,"Listening....Click to Stop", Toast.LENGTH_LONG).show();
        } else {
            try {
                capture.close();
                listening = false;
                gifImageView.setVisibility(View.GONE);
                Toast.makeText(Meeting.this,"Stopped Listening....Click to Start", Toast.LENGTH_LONG).show();

                Thread.sleep(8000);
                compute();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Private Methods - Speech to Text
    private RecognizeOptions getRecognizeOptions() {
        return new RecognizeOptions.Builder()
                .continuous(true)
                .contentType(ContentType.OPUS.toString())
//                .model("en-GB_NarrowbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .speakerLabels(true)
                .build();
    }
    //Watson Speech to Text Methods.
    private class MicrophoneRecognizeDelegate implements RecognizeCallback {
        @Override
        public void onTranscription(SpeechResults speechResults) {
            Double lasttime = 0.0;
            boolean dolast = false;
//            System.out.println("sop: "+speechResults);
//            recoTokens = new SpeakerLabelsDiarization.RecoTokens();
            if(speechResults.getSpeakerLabels() !=null)
            {
                //niche wala

                Log.i("SPEECHRESULTS", String.valueOf(speechResults.getSpeakerLabels().get(0).getSpeaker()));
                Log.i("SPEECHRESULTS", String.valueOf(speechResults.getSpeakerLabels().get(0).getTo()));
                Log.i("SPEECHRESULTS", String.valueOf(speechResults.getSpeakerLabels().get(0).getFrom()));
                Log.i("SPEECHRESULTS", String.valueOf(speechResults.getSpeakerLabels().get(0).isFinal()));

                int speakerId = speechResults.getSpeakerLabels().get(0).getSpeaker();
                Double to = speechResults.getSpeakerLabels().get(0).getTo();
                Double from = speechResults.getSpeakerLabels().get(0).getFrom();
                boolean isfinal = speechResults.getSpeakerLabels().get(0).isFinal();

                AllSpeakers speakers = new AllSpeakers();
                speakers.setEnd_time(to);
                speakers.setStart_time(from);
                speakers.setSpeakerId(speakerId);
                speakers.setFinal(isfinal);

                all_speakers.add(speakers);
                Log.d("test","as size: "+all_speakers.size());

            }
            if(speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                if(speechResults.getResults().get(0).isFinal())
                {
                    final String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                    Log.i("SPEECH", text);
                    showMicText(text);
                    List<SpeechTimestamp> result = speechResults.getResults().get(0).getAlternatives().get(0).getTimestamps();
                    for(SpeechTimestamp st : result){
                        Double start_time = st.getStartTime();
                        Double end_time = st.getEndTime();
                        String word = st.getWord();
                        Log.d("test","Word: "+word);
                        MyTranscripts myTranscripts = new MyTranscripts();
                        myTranscripts.setEnd_time(end_time);
                        myTranscripts.setStart_time(start_time);
                        myTranscripts.setWord(word);
                        all_timestamps.add(myTranscripts);
                        Log.d("test","trans size: "+all_timestamps.size());
                    }

                }

            }
        }
        @Override public void onConnected() {
        }
        @Override public void onError(Exception e) {
            showError(e);
            enableMicButton();
        }
        @Override public void onDisconnected() {
            enableMicButton();
        }
    }
    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                inputMessage.setText(text);
            }
        });
    }
    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                btnRecord.setEnabled(true);
            }
        });
    }
    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(Meeting.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    public void proceed()
    {


    }

    public void compute(){

        dAdapter= new DialogueAdapter(Meeting.this,final_results);
        rview.setAdapter(dAdapter);
        Collections.sort(all_timestamps, new Comparator<MyTranscripts>() {
            @Override
            public int compare(MyTranscripts o1, MyTranscripts o2) {
                return  o1.getStart_time().compareTo(o2.getStart_time());
            }
        });
        Collections.sort(all_speakers, new Comparator<AllSpeakers>() {
            @Override
            public int compare(AllSpeakers o1, AllSpeakers o2) {
                return  o1.getStart_time().compareTo(o2.getStart_time());
            }
        });

        inputMessage.setVisibility(View.GONE);

        int index=0;
        Log.d("test","all speakers "+all_speakers.size());
        for(AllSpeakers i: all_speakers)
        {
            Log.d("test","allspeakers: "+all_speakers.size()+" transcripts: "+all_timestamps.size());
            Double st = i.getStart_time();
            Double et = i.getEnd_time();
            Dialogue d = new Dialogue();
            d.setEnd_time(et);
            d.setStart_time(st);
            d.setSpeakerId(i.getSpeakerId());

            StringBuffer text= new StringBuffer();
            for(int x=index;x<all_timestamps.size();x++)
            {
                if(all_timestamps.get(x).getEnd_time()>et)
                {
                    break;
                }
                text=text.append(" "+all_timestamps.get(x).getWord());
                index++;
            }

            d.setTranscript(text.toString());

            final_results.add(d);

            dAdapter.notifyDataSetChanged();

        }


        ArrayList<StringBuffer> speech = new ArrayList<>(10);
        StringBuffer for_summ = new StringBuffer();
        ArrayList<String> convo = new ArrayList<>();
        ArrayList<String> string_speech = new ArrayList<>();
        Log.d("test","final size "+ final_results.size());
        /*for(Dialogue dg: final_results)
        {
            Log.d("test","speakr: "+dg.getSpeakerId()+"msg: "+dg.getTranscript());
            speech.get(dg.getSpeakerId()).append(dg.getTranscript());
            for_summ.append(dg.getTranscript()+" ");
            convo.add(dg.getSpeakerId()+"@"+dg.getTranscript());
            Log.d("test",".");
        }

        Log.d("test","trans: "+for_summ);



        for(StringBuffer sb: speech)
        {
            string_speech.add(sb.toString());
            Log.d("test","convo: "+sb.toString());
        }*/



        Intent intent = new Intent(Meeting.this, Summary.class);
        //intent.putStringArrayListExtra("conversation",convo);
        //intent.putExtra("transcript",for_summ.toString());
        intent.putExtra("meetingId",meetingId);
        intent.putExtra("agenda",agenda);
        //intent.putStringArrayListExtra("memberText",string_speech);
        intent.putParcelableArrayListExtra("final_results",final_results);
        startActivity(intent);


    }
}
