package com.example.sarthak.meetify;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class DialogueAdapter extends RecyclerView.Adapter<DialogueAdapter.MyViewHolder>{
    Context mContext;
    List<Dialogue> dList;


    public DialogueAdapter(Context context, List<Dialogue> status)
    {
        mContext = context;
        dList = status;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialogue_element, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.person.setText(dList.get(position).getSpeakerId()+"");
        holder.message.setText(dList.get(position).getTranscript());
    }

    @Override
    public int getItemCount()
    {
        return dList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView person;//, description, updated_at, notes_id;
        TextView message;

        public MyViewHolder(View convertView) {
            super(convertView);
            //v = convertView;
            person = (TextView) convertView.findViewById(R.id.name);
            message=(TextView) convertView.findViewById(R.id.message);
        }
    }

}

