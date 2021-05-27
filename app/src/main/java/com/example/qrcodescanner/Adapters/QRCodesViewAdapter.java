package com.example.qrcodescanner.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrcodescanner.FragmentsCode.QRScanner;
import com.example.qrcodescanner.FragmentsCode.SavedPicFragment;
import com.example.qrcodescanner.FragmentsCode.SavedPicGeneratedFragment;
import com.example.qrcodescanner.SimpleClasses.QRCodeItem;
import com.example.qrcodescanner.R;
import com.google.android.gms.vision.barcode.Barcode;

public class QRCodesViewAdapter extends RecyclerView.Adapter<QRCodesViewAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private boolean generated;

    public QRCodesViewAdapter(Context context, boolean generated){
        this.inflater = LayoutInflater.from(context);
        this.generated = generated;
    }

    @NonNull
    @Override
    public QRCodesViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_recycler_view, parent,false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QRCodeItem item = generated? SavedPicGeneratedFragment.itemList.get(position):SavedPicFragment.itemList.get(position);

        String s = QRScanner.textType.get(item.getTextFormat());
        if(s == null) s = "null";
        if (generated){
            s = item.getTextOfQRCode();
            holder.QRCodeTextView.setText(s.length() > 10 ? s.substring(0, 11).concat("...") : s);
        }
        else {
            if (item.getTextFormat() != Barcode.TEXT) {
                holder.QRCodeTextView.setText(s);
            } else {
                s = item.getTextOfQRCode();
                holder.QRCodeTextView.setText(s.length() > 10 ? s.substring(0, 11).concat("...") : s);
            }
        }

        holder.QRCodeTextView.setBackgroundColor((generated?SavedPicGeneratedFragment.selectedId.contains(position):SavedPicFragment.selectedId.contains(position))?Color.BLUE:Color.TRANSPARENT);
        holder.QRCodeImageView.setImageBitmap(item.getQrCodeBitmap());
    }

    @Override
    public int getItemCount() {
        return generated?SavedPicGeneratedFragment.itemList.size():SavedPicFragment.itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView QRCodeImageView;
        final TextView QRCodeTextView;
        ViewHolder(View view){
            super(view);
            QRCodeImageView = view.findViewById(R.id.qrView);
            QRCodeTextView = view.findViewById(R.id.dataTextView);
        }
    }

}