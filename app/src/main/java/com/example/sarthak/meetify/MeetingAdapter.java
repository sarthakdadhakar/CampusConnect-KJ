package com.example.sarthak.meetify;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MyViewHolder>{
    Context mContext;
    List<QueryDocumentSnapshot> dList;


    public MeetingAdapter(Context context, List<QueryDocumentSnapshot> status)
    {
        mContext = context;
        dList = status;
    }

    @Override
    public MeetingAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_meeting, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        //Toast.makeText(mContext, "in adapter", Toast.LENGTH_SHORT).show();
        final QueryDocumentSnapshot doc = dList.get(i);
        //java.sql.Timestamp ts = doc.getTimestamp("date");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //String fdate = sdf.format(ts);


        myViewHolder.text.setText(doc.getString("transcript"));
        myViewHolder.date.setText(doc.getString("date"));
        myViewHolder.name.setText(doc.getString("name"));

        myViewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,AllDetails.class);
                intent.putExtra("meetingId",doc.getId());
                mContext.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount()
    {
        return dList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView date;//, description, updated_at, notes_id;
        TextView name;
        TextView text;
        CardView cv;
        public MyViewHolder(View convertView) {
            super(convertView);
            //v = convertView;
            cv=convertView.findViewById(R.id.cv);
            date = (TextView) convertView.findViewById(R.id.tvDate);
            name=(TextView) convertView.findViewById(R.id.tvTopic);
            text = convertView.findViewById(R.id.tvSummary);
        }

    }

}

