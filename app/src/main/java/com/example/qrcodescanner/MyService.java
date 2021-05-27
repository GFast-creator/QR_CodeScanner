package com.example.qrcodescanner;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import java.io.File;

public class MyService extends Service {
    public static Uri uri;
    public static boolean gen = false;
    public static long newId = -1;
    NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.e("Noti11", "service start");
            newId = intent.getLongExtra("extra",-3);
            gen = intent.getBooleanExtra("extra1", false);
            String action = intent.getAction();
            String s = "sharePic.png";
            File file = new File(getApplicationContext().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic", s);
            uri = FileProvider.getUriForFile(getApplicationContext(),"com.example.myapplication.provider",file);

            Log.e("Noti11", "uri: " + uri.toString() + "\n" +
                    "gen: " + gen + "\n" +
                    "newId: " + newId + "\n" +
                    "action: " + action);

            switch (action){
                case "copy":
                    ClipboardManager mClipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData theClip = ClipData.newUri(getApplicationContext().getContentResolver(), "12312111", uri);
                    mClipboard.setPrimaryClip(theClip);

                    Log.e("Noti11", "copy");

                    Toast.makeText(getApplicationContext(), "Картинка скопирована!", Toast.LENGTH_LONG).show();
                    break;
                case "to":
                    try {
                        /*Intent intent1 = new Intent(this, PicViewActivity.class);
                        QRCodeItem q = gen? SavedPicGeneratedFragment.itemList.get((int)newId):SavedPicFragment.itemList.get((int)newId);

                        intent1.putExtra("generated", true);

                        int s1 = q.getTextFormat();
                        intent1.putExtra("codeFormat", s1);

                        s1 = q.getFormat();
                        intent1.putExtra("type", s1);

                        intent1.putExtra("codeText", q.getTextOfQRCode());
                        intent1.putExtra("View?", true);

                        String s2 = "intent.png";
                        intent1.putExtra("nameOfPic", s2);
                        intent1.putExtra("idSQL", newId);

                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);*/
                        Intent intent1 = new Intent(this, MainActivity.class);
                        intent1.setAction("serviceShowPic");
                        intent1.putExtra("generated", gen);
                        intent1.putExtra("idSQL", newId-1);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent1);
                        NotificationManagerCompat.from(getApplicationContext()).cancelAll();
                    } catch (Exception e) {
                        Log.e("Noti11", e.getMessage());
                    }

                    Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    getApplicationContext().sendBroadcast(it);
                    break;
            }
            /*Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);*/
            //NotificationManagerCompat.from(getApplicationContext()).cancelAll();
        } catch (Exception e){
            Log.e("Noti11", e.getMessage());
        }
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}