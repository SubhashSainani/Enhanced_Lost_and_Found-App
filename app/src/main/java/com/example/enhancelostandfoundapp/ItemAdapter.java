package com.example.enhancelostandfoundapp;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    private List<Item> itemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lost_found, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.itemType.setText(item.getType().toUpperCase());
        GradientDrawable background = (GradientDrawable) holder.itemType.getBackground();
        if (item.getType().equals("Lost")) {
            background.setColor(Color.parseColor("#FF5722")); // Orange for Lost
        } else {
            background.setColor(Color.parseColor("#4CAF50")); // Green for Found
        }

        holder.itemName.setText(item.getName());

        SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        try {
            Date itemDate = format.parse(item.getDate());
            Date now = Calendar.getInstance().getTime();
            long diffInMillies = Math.abs(now.getTime() - itemDate.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            if (diff == 0) {
                holder.itemDate.setText("Today");
            } else if (diff == 1) {
                holder.itemDate.setText("Yesterday");
            } else {
                holder.itemDate.setText(diff + " days ago");
            }
        } catch (ParseException e) {
            holder.itemDate.setText(item.getDate());
        }

        // Set location
        holder.itemLocation.setText(item.getLocation());

        holder.itemCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView itemCardView;
        TextView itemType;
        TextView itemName;
        TextView itemDate;
        TextView itemLocation;
        TextView itemDescription;
        TextView contactInfo;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCardView = (CardView) itemView;
            itemType = itemView.findViewById(R.id.itemType);
            itemName = itemView.findViewById(R.id.itemName);
            itemDate = itemView.findViewById(R.id.itemDate);
            itemLocation = itemView.findViewById(R.id.itemLocation);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            contactInfo = itemView.findViewById(R.id.contactInfo);
        }
    }
}