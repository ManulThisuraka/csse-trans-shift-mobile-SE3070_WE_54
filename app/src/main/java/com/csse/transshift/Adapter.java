package com.csse.transshift;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private Context context;
    List<String> titles;
    List<Integer> images;
    LayoutInflater inflater;

    public Adapter(Context context, List<String> titles, List<Integer> images) {
        this.titles = titles;
        this.images = images;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_grid_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
        holder.title.setText(titles.get(position));
        holder.gridIcon.setImageResource(images.get(position));
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView gridIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            title = itemView.findViewById(R.id.textView_icon);
            gridIcon = itemView.findViewById(R.id.imageView_icon);

            itemView.setOnClickListener(v -> {
                final Intent intent;
                switch (getAdapterPosition()) {
                    case 0:
                        intent = new Intent(context, ScanActivity.class);
                        break;
                    case 1:
                        intent = new Intent(context, BalanceActivity.class);
                        break;
                    case 2:
                        intent = new Intent(context, PaymentMainActivity.class);
                        break;
                    default:
                        intent = new Intent(context, UserprofileActivity.class);
                        break;
                }
                context.startActivity(intent);
            });
        }
    }
}