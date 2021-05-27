package com.example.qrcodescanner.FragmentsCode;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.qrcodescanner.Adapters.QRCodesViewAdapter;
import com.example.qrcodescanner.PicViewActivity;
import com.example.qrcodescanner.RecyclerItemClickListener;
import com.example.qrcodescanner.SimpleClasses.DBSavePic;
import com.example.qrcodescanner.SimpleClasses.QRCodeItem;
import com.example.qrcodescanner.R;
import com.example.qrcodescanner.SimpleClasses.DBHelper;
import com.example.qrcodescanner.SimpleClasses.Vibro;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeSet;


public class SavedPicFragment extends Fragment {
    static ImageButton selectModeButton;
    static ImageButton deleteButton;
    static ImageView imageView;
    public static TreeSet<Integer> selectedId = new TreeSet<>();
    public static QRCodesViewAdapter adapter;
    public static boolean isSelectMode = false;
    private SQLiteDatabase db;
    private final String SQL = "SELECT * FROM pictures";
    public static ArrayList<QRCodeItem> itemList = new ArrayList<>();
    static Context context;
    static Snackbar snackbar;

    private void setTextVisibility(){
        if (itemList.size() > 0){
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void getData(){
        Cursor cursor = db.rawQuery(SQL,null);
        itemList.clear();
        try{
            cursor.moveToFirst();
            do{
                byte[] b = cursor.getBlob(cursor.getColumnIndex("bitmap"));

                itemList.add(new QRCodeItem(cursor.getInt(cursor.getColumnIndex("id")),
                        BitmapFactory.decodeByteArray(b,0,b.length),
                        cursor.getString(cursor.getColumnIndex("text")),
                        cursor.getInt(cursor.getColumnIndex("format")),
                        cursor.getInt(cursor.getColumnIndex("textFormat"))));
            }while (cursor.moveToNext());


        } catch (Exception e){
            Log.e("getData()",e.getMessage());
        }

        cursor.close();
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        assert v!=null;
        snackbar = Snackbar.make(v,"Режим удаления активен", BaseTransientBottomBar.LENGTH_INDEFINITE).setBackgroundTint(Color.RED);
        db = (new DBHelper(getContext())).getReadableDatabase();
        selectModeButton = v.findViewById(R.id.selectMode);
        deleteButton = v.findViewById(R.id.deleteButton);

        getData();

        imageView = v.findViewById(R.id.imageView3);
        setTextVisibility();

        RecyclerView recyclerView = v.findViewById(R.id.recyclerViewSaved);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(),4);

        recyclerView.setLayoutManager(mLayoutManager);

        adapter = new QRCodesViewAdapter(getContext(),false);
        recyclerView.setAdapter(adapter);

        context = getContext();


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stack<Integer> stack = new Stack<>();
                for (int id : selectedId){
                    stack.push(id);
                }
                int i;
                while (stack.size() != 0){
                    i = stack.pop();
                    adapter.notifyItemRemoved(i);
                    itemList.remove(i);
                }

                db.close();
                DBSavePic.deleteItems(getContext(),"pictures", selectedId);
                DBSavePic.idUpdate(getContext(),"pictures");
                db = (new DBHelper(getContext())).getReadableDatabase();
                setTextVisibility();
                setIsSelectMode(false);
                NotificationManagerCompat.from(getContext()).cancelAll();
            }
        });

        selectModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsSelectMode(!isSelectMode);
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!isSelectMode) {
                    showPic(getContext(),position);
                } else {
                    try {
                        if (!selectedId.contains(position)) selectedId.add(position);
                        else selectedId.remove(position);
                    }catch (Exception ignored){}

                    if (selectedId.size() != 0){
                        deleteButton.setVisibility(View.VISIBLE);
                    } else {
                        deleteButton.setVisibility(View.GONE);
                    }

                    adapter.notifyItemChanged(position);
                }
            }
        }));
    }
    public static void showPic(Context context, int position){
        try {
            QRCodeItem q = itemList.get(position);
            Intent intent = new Intent(context, PicViewActivity.class);
            intent.putExtra("generated", false);

            int s1 = q.getTextFormat();
            intent.putExtra("codeFormat", s1);

            s1 = q.getFormat();
            intent.putExtra("type",s1);

            intent.putExtra("codeText",q.getTextOfQRCode());
            intent.putExtra("View?", true);

            String s = "intent.png";
            intent.putExtra("nameOfPic", s);
            intent.putExtra("idSQL", position);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception e) {
            Log.e("HHHHhh", e.toString());
        }
    }

    static public void setIsSelectMode(boolean b){
        isSelectMode = b;
        if (isSelectMode){
            selectModeButton.setImageResource(R.drawable.ic_twotone_crop_free_red_24);
            snackbar.show();
        } else {
            selectModeButton.setImageResource(R.drawable.ic_twotone_crop_free_blue_24);
            selectedId.clear();
            adapter.notifyDataSetChanged();
            deleteButton.setVisibility(View.GONE);
            snackbar.dismiss();
        }

        Vibro.vibrate(context,200);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_pic, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        db.close();
        snackbar.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        setTextVisibility();
        getActivity().setTitle("Отсканированные");
        db = (new DBHelper(getContext())).getReadableDatabase();
    }

}
