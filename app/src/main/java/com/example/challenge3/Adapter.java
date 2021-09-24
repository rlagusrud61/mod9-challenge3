package com.example.challenge3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<Model> activityList;

    public Adapter (List<Model> activityList) {this.activityList = activityList;}

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_design,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull Adapter.ViewHolder holder, int position) {
        int activity_picture = activityList.get(position).getImageview1();
        String activity_name = activityList.get(position).getActivityType();
        String current_time = activityList.get(position).getCurrentTime();
        String divider = activityList.get(position).getDivider();

        holder.setData(activity_picture,activity_name,current_time,divider);

    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView ActivityType, CurrentTime, Divider;
        private ImageView imageview1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageview1 = itemView.findViewById(R.id.imageview1);
            ActivityType = itemView.findViewById(R.id.ActivityType);
            CurrentTime = itemView.findViewById(R.id.CurrentTime);
            Divider = itemView.findViewById(R.id.Divider);

        }

        public void setData(int activity_picture, String activity_name, String current_time, String divider) {
            imageview1.setImageResource(activity_picture);
            ActivityType.setText(activity_name);
            CurrentTime.setText(current_time);
            Divider.setText(divider);
        }
    }
}
