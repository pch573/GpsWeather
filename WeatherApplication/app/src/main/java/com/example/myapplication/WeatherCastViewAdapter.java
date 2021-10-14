package com.example.myapplication;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WeatherCastViewAdapter extends RecyclerView.Adapter<WeatherCastViewAdapter.ViewHolder> {
    private ArrayList<WeatherCastItem> itemList = new ArrayList<>();
    private Context context;

    public WeatherCastViewAdapter(Context context, ArrayList<WeatherCastItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WeatherCastItem item = itemList.get(position);

        switch (item.getWeather()) {
            case 0: holder.iw_ivWeather.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.sun)); break;
            case 1: holder.iw_ivWeather.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.rain)); break;
            case 2: holder.iw_ivWeather.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.rainsnow)); break;
            case 3: holder.iw_ivWeather.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.snow)); break;
            case 4: holder.iw_ivWeather.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.shower)); break;
        }
        if (item.getDay() == 0) holder.iw_tvDay.setText("오늘");
        else if (item.getDay() == 1) holder.iw_tvDay.setText("내일");
        else if (item.getDay() == 2) holder.iw_tvDay.setText("모래");
        if (item.getTime() <= 12) holder.iw_tvTime.setText("오전 " + item.getTime() + "시");
        else holder.iw_tvTime.setText("오후 " + (item.getTime() - 12) + "시");
        if (item.getTemp() <= -900 || item.getTemp() >= 900) holder.iw_tvTemp.setText("측정불가");
        else holder.iw_tvTemp.setText(item.getTemp() + "℃");
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iw_ivWeather;
        public TextView iw_tvDay;
        public TextView iw_tvTime;
        public TextView iw_tvTemp;

        public ViewHolder(View view) {
            super(view);
            iw_ivWeather = (ImageView) view.findViewById(R.id.iw_ivWeather);
            iw_tvDay = (TextView) view.findViewById(R.id.iw_tvDay);
            iw_tvTime = (TextView) view.findViewById(R.id.iw_tvTime);
            iw_tvTemp = (TextView) view.findViewById(R.id.iw_tvTemp);
        }
    }
}
