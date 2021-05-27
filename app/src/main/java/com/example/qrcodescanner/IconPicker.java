package com.example.qrcodescanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.qrcodescanner.Adapters.IconsAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class IconPicker extends AppCompatActivity {

    public static ArrayList<Drawable> icons = new ArrayList<>();
    public static String[] files;
    static IconsAdapter adapter;
    public static int selectedIcon = -1;

    private void getData(){
        try {

            AssetManager assetManager = getApplicationContext().getAssets();
            files = assetManager.list("icons");
            InputStream inputStream = null;

            try {
                for (String file : files) {
                    inputStream = assetManager.open("icons/" + file);
                    Drawable d = Drawable.createFromStream(inputStream,file);
                    icons.add(d);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream.close();

        }catch (Exception e){
            Log.e("getData() - IconPicker", e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_picker);
        getData();

        if (selectedIcon == -1)
            findViewById(R.id.constraintLayout3).setVisibility(View.GONE);
        else{
            ImageView imageView = findViewById(R.id.imageView6);
            imageView.setImageDrawable(icons.get(selectedIcon));
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewIcons);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getBaseContext(), 4);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new IconsAdapter(getBaseContext());
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getBaseContext(),
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        int l = selectedIcon;
                        selectedIcon = position;
                        if (l != -1) adapter.notifyItemChanged(l);

                        ImageView imageView = findViewById(R.id.imageView6);
                        imageView.setImageDrawable(icons.get(position));

                        findViewById(R.id.constraintLayout3).setVisibility(View.VISIBLE);
                        adapter.notifyItemChanged(position);
                    }
                }));
        findViewById(R.id.setButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    setResult(1, (new Intent()).putExtra("iconID", selectedIcon));
                }catch (Exception e){
                    setResult(-1, null);
                }
                finish();
            }
        });

        findViewById(R.id.imageView6).setBackgroundColor(Color.GRAY);

    }

}