package com.example.qrcodescanner.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcodescanner.IconPicker;
import com.example.qrcodescanner.R;

public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.ViewHolder> {
    private final LayoutInflater inflater;

    public IconsAdapter(Context context){
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.icon_recycler_view, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drawable drawable = IconPicker.icons.get(position);
        holder.iconView.setImageDrawable(drawable);
        holder.textView1.setText((IconPicker.files[position]).substring(0,IconPicker.files[position].length()-4));
        if (position == IconPicker.selectedIcon){
            holder.textView1.setBackgroundColor(Color.BLUE);
        } else {
            holder.textView1.setBackgroundColor(Color.TRANSPARENT);
            int i = 0;
        }
    }

    @Override
    public int getItemCount() {
        return IconPicker.icons.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconView;
        final TextView textView1;
        ViewHolder(View view){
            super(view);
            iconView = view.findViewById(R.id.iconView);
            textView1 = view.findViewById(R.id.textView3);
        }
    }
}
