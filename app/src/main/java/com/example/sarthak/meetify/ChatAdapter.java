package com.example.sarthak.meetify;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder>{
    Context mContext;
    List<User> dList;


    public ChatAdapter(Context context, List<User> status)
    {
        mContext = context;
        dList = status;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_element, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.person.setText(dList.get(position).getName());
     //   holder.message.setText(dList.get(position).getString("message"));
        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    dList.get(position).setSelected(true);
                }
                else
                {
                    dList.get(position).setSelected(false);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return dList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView person;//, description, updated_at, notes_id;
        TextView message;
        CheckBox cb;

        public MyViewHolder(View convertView) {
            super(convertView);
            //v = convertView;
            cb = convertView.findViewById(R.id.cb);
            person = (TextView) convertView.findViewById(R.id.name);
            //message=(TextView) convertView.findViewById(R.id.message);
        }
    }

}
