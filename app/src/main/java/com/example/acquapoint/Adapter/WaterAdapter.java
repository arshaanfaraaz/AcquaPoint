package com.example.acquapoint.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.example.acquapoint.Models.Water;
import com.example.acquapoint.R;
import java.util.List;


public class WaterAdapter extends RecyclerView.Adapter< WaterAdapter.CustomViewHolder> {
    List<Water> waters;
    Context context;

    private  onItemClickListener mListener;
    public  interface onItemClickListener{
        void  respond(int position);
    }
    public  void setOnItemClickListener(onItemClickListener listener){//item click listener initialization
        mListener=listener;
    }

   public static class  CustomViewHolder extends RecyclerView.ViewHolder{
       Button buttonRespond;
       TextView textViewFoodName,textViewAddress,textViewStatus;
        public CustomViewHolder(View itemView, final onItemClickListener listener) {
            super(itemView);
            textViewFoodName=itemView.findViewById(R.id.textViewFoodName);
            textViewAddress=itemView.findViewById(R.id.textViewAddress);
            textViewStatus=itemView.findViewById(R.id. textViewStatus);
            buttonRespond=itemView.findViewById(R.id.buttonRespond);


            buttonRespond.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener!=null){
                        int position=getAdapterPosition();
                        if(position!= RecyclerView.NO_POSITION){
                            listener.respond(position);
                        }
                    }
                }
            });

        }
    }
    public WaterAdapter(List<Water> waters, Context context) {
        this.waters = waters;
        this.context = context;
    }
    @Override
    public int getItemViewType(int position) {

            return R.layout.water_item;
    }
    @Override
    public int getItemCount() {
        return waters.size();
    }
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false),mListener);
    }
    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        holder.textViewFoodName.setText(waters.get(position).getFoodName());
        holder.textViewAddress.setText(waters.get(position).getFoodAddress());
        holder.textViewStatus.setText(waters.get(position).getFoodStatus());
        //Toast.makeText(context, foods.get(position).getFoodStatus(), Toast.LENGTH_SHORT).show();
        String status= waters.get(position).getFoodStatus();
       if(status.equals("Requested")){
           holder.buttonRespond.setVisibility(View.VISIBLE);
       }else {
           holder.buttonRespond.setVisibility(View.GONE);
       }


    }
}
