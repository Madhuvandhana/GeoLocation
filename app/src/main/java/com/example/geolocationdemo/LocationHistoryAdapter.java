package com.example.geolocationdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationHistoryAdapter extends RecyclerView.Adapter<LocationHistoryAdapter.CustomViewHolder> {
    private List<LocationData> locationDataList;
    private Context mContext;

    public LocationHistoryAdapter(Context context, List<LocationData> locationData) {
        this.locationDataList = locationData;
        this.mContext = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_data, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        LocationData locationData = locationDataList.get(i);
        customViewHolder.location.setText("Location: "+locationData.getAddress());
        customViewHolder.date.setText("Date: "+locationData.getDate());
    }

    @Override
    public int getItemCount() {
        return (null != locationDataList ? locationDataList.size() : 0);
    }

    public void updateList(List<LocationData> locationData){
        this.locationDataList = locationData;
        notifyDataSetChanged();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView location;
        TextView date;

         CustomViewHolder(View view) {
            super(view);
            this.location = (TextView) view.findViewById(R.id.location);
            this.date = (TextView) view.findViewById(R.id.date);
        }
    }
}
