package com.example.qrcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qrcodescanner.FragmentsCode.QRScanner;
import com.example.qrcodescanner.FragmentsCode.SavedPicFragment;
import com.example.qrcodescanner.FragmentsCode.SavedPicGeneratedFragment;
import com.example.qrcodescanner.SimpleClasses.DBHelper;
import com.example.qrcodescanner.SimpleClasses.DBSavePic;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TreeSet;

public class PicViewActivity extends AppCompatActivity {
    ImageView imageView;
    TextView picInfo;
    Button save_button;
    File file;
    TextView qrCodeText;
    Button actionButton, copyText, copyBitmap;
    String json = "";
    boolean view;
    public static NotificationManagerCompat notificationManager = null;
    String val = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pic_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_del:
                boolean b = getIntent().getBooleanExtra("View?",false);
                boolean gen = getIntent().getBooleanExtra("generated",false);
                if (b){
                    NotificationManagerCompat.from(getApplicationContext()).cancelAll();
                    int i = getIntent().getIntExtra("idSQL", 0);
                    TreeSet<Integer> treeSet = new TreeSet<>();
                    treeSet.add(i);
                    DBSavePic.deleteItems(getApplicationContext(),gen?"generatedPic":"pictures", treeSet);
                    if (gen){
                        SavedPicGeneratedFragment.itemList.remove(i);
                        SavedPicGeneratedFragment.adapter.notifyItemRemoved(i);
                    } else {
                        SavedPicFragment.itemList.remove(i);
                        SavedPicFragment.adapter.notifyItemRemoved(i);
                    }
                }
                finish();
                break;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_view);

        setTitle("Предпросмотр");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (notificationManager == null) notificationManager = NotificationManagerCompat.from(getApplicationContext());

        Bitmap bitmap = null;
        view = getIntent().getBooleanExtra("View?",true);
        if (view) {
            SQLiteDatabase db = (new DBHelper(getApplicationContext())).getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT bitmap, extra FROM "
                    + (getIntent().getBooleanExtra("generated",false)?"generatedPic":"pictures")
                    +" WHERE id = "
                    + (getIntent().getIntExtra("idSQL", 0) + 1), null);
            if (cursor.moveToFirst()) {
                byte[] b = cursor.getBlob(cursor.getColumnIndex("bitmap"));
                bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                json = cursor.getString(cursor.getColumnIndex("extra"));
            }
            cursor.close();
            db.close();
        } else {
            String fileName = getIntent().getStringExtra("nameOfPic");

            file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()+"/intentPic",
                    fileName);

            bitmap = BitmapFactory.decodeFile(file.getPath());
        }

        imageView = findViewById(R.id.qrcodeview);
        imageView.setImageBitmap(bitmap);


        picInfo = findViewById(R.id.picInfo);
        qrCodeText = findViewById(R.id.qrCodeText);
        actionButton = findViewById(R.id.actionButton);
        StringBuffer textCode1 = new StringBuffer(getIntent().getStringExtra("codeText"));
        StringBuffer textCode = textCode1;
        int codeFormat = getIntent().getIntExtra("codeFormat", Barcode.TEXT);

        int type = getIntent().getIntExtra("type", Barcode.QR_CODE);

        switch (getIntent().getIntExtra("codeFormat",Barcode.TEXT)){
            case (Barcode.WIFI):
                Barcode.WiFi wifi = view?((new Gson()).fromJson(json, Barcode.WiFi.class)):(getIntent().getParcelableExtra("extra"));
                json = new Gson().toJson(wifi);
                textCode = new StringBuffer();
                textCode.append("точка доступа WI-FI:").append("\n")
                        .append("Имя точки: ").append(wifi.ssid).append("\n")
                        .append("Пароль от WI-FI: ").append(wifi.password);
                //TODO: не тестил
                actionButton.setText("Подключиться к точке доступа(test)");
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            //String[] sw = getIntent().getStringExtra("codeText").split(" ");
                            WifiConfiguration wifiConfig = new WifiConfiguration();
                            wifiConfig.SSID = wifi.ssid; //String.format("\"%s\"", sw[0])
                            wifiConfig.preSharedKey = wifi.password;//String.format("\"%s\"", sw[1])

                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                            int netId = wifiManager.addNetwork(wifiConfig);
                            wifiManager.disconnect();
                            wifiManager.enableNetwork(netId, true);
                            wifiManager.reconnect();
                        } catch (Exception e){
                            Snackbar.make(v, e.getMessage(),Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
                break;

            case (Barcode.GEO):
                String s1 = getIntent().getStringExtra("codeText");
                Barcode.GeoPoint geoPoint = view?((new Gson()).fromJson(json, Barcode.GeoPoint.class)):getIntent().getParcelableExtra("extra");
                json = new Gson().toJson(geoPoint);
                textCode = new StringBuffer();
                textCode.append("Точка с координатами: (x: ").append(geoPoint.lat).append(", y: ").append(geoPoint.lng).append(")");
                actionButton.setText("Открыть точку на карте");
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse("geo:" + geoPoint.lat + geoPoint.lng +"?q=" + geoPoint.lat + "," + geoPoint.lng + "(Точка из QR-кода)");
                        //Uri uri = Uri.parse("google.navigation:q=" + geoPoint.lat + "," + geoPoint.lng + "&avoid=tf");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                });
                break;
            case (Barcode.CALENDAR_EVENT):
                Barcode.CalendarEvent calendarEvent = view?((new Gson()).fromJson(json, Barcode.CalendarEvent.class)):getIntent().getParcelableExtra("extra");
                textCode = new StringBuffer();
                json = new Gson().toJson(calendarEvent);

                Calendar beginTime = Calendar.getInstance();
                beginTime.set(calendarEvent.start.year, calendarEvent.start.month, calendarEvent.start.day, calendarEvent.start.hours, calendarEvent.start.minutes,calendarEvent.start.seconds);

                Calendar endTime = Calendar.getInstance();
                endTime.set(calendarEvent.end.year, calendarEvent.end.month, calendarEvent.end.day, calendarEvent.end.hours, calendarEvent.end.minutes,calendarEvent.end.seconds);
                textCode
                        .append("Дата начала: ").append(calendarEvent.start.day).append(".").append(calendarEvent.start.month).append(".").append(calendarEvent.start.year)
                        .append(" Время:").append(String.valueOf(calendarEvent.start.hours).length() == 2?calendarEvent.start.hours:"0"+calendarEvent.start.hours).append(":")
                        .append(String.valueOf(calendarEvent.start.minutes).length() == 2?calendarEvent.start.minutes:"0"+calendarEvent.start.minutes).append(":")
                        .append(String.valueOf(calendarEvent.start.seconds).length() == 2?calendarEvent.start.seconds:"0"+calendarEvent.start.seconds).append("\n")
                        .append("Дата конца: ").append(calendarEvent.end.day).append(".").append(calendarEvent.end.month).append(".").append(calendarEvent.end.year)
                        .append(" Время:").append(String.valueOf(calendarEvent.end.hours).length() == 2?calendarEvent.end.hours:"0"+calendarEvent.end.hours).append(":")
                        .append(String.valueOf(calendarEvent.end.minutes).length() == 2?calendarEvent.end.minutes:"0"+calendarEvent.end.minutes).append(":")
                        .append(String.valueOf(calendarEvent.end.seconds).length() == 2?calendarEvent.end.seconds:"0"+calendarEvent.end.seconds).append("\n");
                if (calendarEvent.summary != null)textCode.append("Название: ").append(calendarEvent.summary).append("\n");
                if (calendarEvent.description != null)textCode.append("Описание: ").append(calendarEvent.description).append("\n");
                if (calendarEvent.location != null)textCode.append("Локация: ").append(calendarEvent.location).append("\n");
                if (calendarEvent.organizer != null)textCode.append("Организатор: ").append(calendarEvent.organizer).append("\n");

                actionButton.setText("Открыть с помощью календаря");
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            Intent intent = new Intent(Intent.ACTION_INSERT)
                                    .setData(CalendarContract.Events.CONTENT_URI)
                                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                                    .putExtra(CalendarContract.Events.TITLE, calendarEvent.summary)
                                    .putExtra(CalendarContract.Events.DESCRIPTION, calendarEvent.description)
                                    .putExtra(CalendarContract.Events.EVENT_LOCATION, calendarEvent.location)
                                    .putExtra(CalendarContract.Attendees.ATTENDEE_EMAIL, calendarEvent.organizer);

                            startActivity(intent);
                        } catch (Exception e) {
                            Snackbar.make(v, "Ошибка: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });


                break;

            case (Barcode.CONTACT_INFO):
                Barcode.ContactInfo contactInfo = view?((new Gson()).fromJson(json, Barcode.ContactInfo.class)):getIntent().getParcelableExtra("extra");
                json = new Gson().toJson(contactInfo);
                textCode = new StringBuffer();
                textCode.append(contactInfo.name.formattedName).append("\n");

                if(contactInfo.emails.length > 0)
                    textCode.append("Email ").append(": ").append(contactInfo.emails[0].address).append("\n");

                if(contactInfo.phones.length > 0)
                    textCode.append("Телефон ").append(": ").append(contactInfo.phones[0].number).append("\n");



                actionButton.setText("Открыть контакт в приложении");
                actionButton.setVisibility(View.VISIBLE);
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION)
                                    .setType(ContactsContract.RawContacts.CONTENT_TYPE)
                                    .putExtra(ContactsContract.Intents.Insert.NAME, contactInfo.name.formattedName);
                            if (contactInfo.emails.length != 0) {
                                if (contactInfo.emails[0] != null)
                                    intent.putExtra(ContactsContract.Intents.Insert.EMAIL, contactInfo.emails[0].address);
                                if (contactInfo.emails[0] != null)
                                    intent.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, contactInfo.emails[0].type);
                            }
                            if (contactInfo.phones.length != 0) {
                                if (contactInfo.phones[0] != null)
                                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, contactInfo.phones[0].number);
                                if (contactInfo.phones[0] != null)
                                    intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, contactInfo.phones[0].type);
                            }

                            startActivity(intent);

                        } catch (Exception e){
                            Snackbar.make(v,"Ошибка: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
                break;
        }


        picInfo.setText(String.format("Формат: %s", QRScanner.textType.get(codeFormat)).concat(String.format(" | Тип кода: %s", QRScanner.qrCodeType.get(type))));
        qrCodeText.setText(textCode);

        save_button = findViewById(R.id.save_button);
        Bitmap finalBitmap = bitmap;

        StringBuffer finalTextCode1 = textCode;
        findViewById(R.id.shareButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                String s = "sharePic" + new Random().nextInt() + ".png";
                                File file = new File(getApplicationContext().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic", s);
                                boolean b = file.createNewFile();
                                if (!b) {
                                    b = file.delete();
                                    b = file.createNewFile();
                                }

                                Log.e("saveImage:", String.valueOf(b));

                                OutputStream fOut = new FileOutputStream(file);
                                Bitmap bb = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                bb.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                fOut.flush();
                                fOut.close();


                                Intent sendIntent = new Intent(Intent.ACTION_SEND);

                                Uri uri = FileProvider.getUriForFile(PicViewActivity.this,
                                        "com.example.myapplication.provider", file);

                                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "Формат: " + QRScanner.textType.get(codeFormat) + "\n"
                                        + finalTextCode1);
                                sendIntent.setType("*/*");

                                Intent chosenIntent = Intent.createChooser(sendIntent, "M");
                                startActivity(chosenIntent);
                            } catch (Exception e){
                                
                            }
                        }
                    }.start();
//                    //String s = "sharePic1.png";
//                    String s = "sharePic" + new Random().nextInt() + ".png";
//                    File file = new File(getApplicationContext().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic", s);
//                    boolean b = file.createNewFile();
//                    if (!b){
//                        b = file.delete();
//                        b = file.createNewFile();
//                    }
//
//                    Log.e("saveImage:", String.valueOf(b));
//
//                    OutputStream fOut = new FileOutputStream(file);
//                    Bitmap bb = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//                    bb.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//                    fOut.flush();
//                    fOut.close();
//
//
//
//                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
//
//                    Uri uri = FileProvider.getUriForFile(PicViewActivity.this,
//                            "com.example.myapplication.provider", file);
//
//                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
//                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Формат: " + QRScanner.textType.get(codeFormat) + "\n"
//                            + finalTextCode1);
//                    sendIntent.setType("*/*");
//
//                    Intent chosenIntent = Intent.createChooser(sendIntent, "M");
//                    startActivity(chosenIntent);

                    //String s = "sharePic1.png";
                    String s = "sharePic" + new Random().nextInt() + ".png";
                    File file = new File(getApplicationContext().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic", s);
                    boolean b = file.createNewFile();
                    if (!b){
                        b = file.delete();
                        b = file.createNewFile();
                    }

                    Log.e("saveImage:", String.valueOf(b));

                    OutputStream fOut = new FileOutputStream(file);
                    Bitmap bb = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    bb.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();



                    Intent sendIntent = new Intent(Intent.ACTION_SEND);

                    Uri uri = FileProvider.getUriForFile(PicViewActivity.this,
                            "com.example.myapplication.provider", file);

                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);



                    /*if (codeFormat == Barcode.TEXT){
                        val = getIntent().getStringExtra("codeText");
                    } else {

                    }*/

                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Формат: " + QRScanner.textType.get(codeFormat) + "\n"
                            + finalTextCode1);
                    sendIntent.setType("*/*");

                    Intent chosenIntent = Intent.createChooser(sendIntent, "M");
                    startActivity(chosenIntent);
                } catch (Exception e){
                    Log.e("ExceptionIntent", e.toString());
                }
            }
        });

        if (!getIntent().getBooleanExtra("View?", false)){
            save_button.setVisibility(View.VISIBLE);
        } else {
            save_button.setVisibility(View.INVISIBLE);
        }



        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationManager.cancelAll();

                OutputStream fOut = null;
                try {
                    file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/ScannedQRCode/SAVEDPICTURES");

                    Log.e("saveImage:", Boolean.toString(file.mkdirs()));

                    {
                        Log.e("saveImage:", file.getAbsolutePath());
                        Log.e("saveImage:", Boolean.toString(file.isFile()));
                        Log.e("saveImage:", Boolean.toString(file.isDirectory()));

                    }

                    file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/ScannedQRCode/SAVEDPICTURES"
                            , new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss", Locale.getDefault()).format(new Date()) + ".png");

                    Log.e("saveImage:", Boolean.toString(file.createNewFile()));

                    {
                        Log.e("saveImage:", file.getAbsolutePath());
                        Log.e("saveImage:", Boolean.toString(file.isFile()));
                        Log.e("saveImage:", Boolean.toString(file.isDirectory()));
                        Log.e("saveImage:", file.canWrite() ? "Yes" : "No");
                        Log.e("saveImage:", "1");
                    }

                    fOut = new FileOutputStream(file);
                    finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);


                    /*new Thread() {
                        @Override
                        public void run() {
                            MediaStore.Images.Media.insertImage(getApplication().getContentResolver(), finalBitmap,  getIntent().getStringExtra("codeText"), "Формат текста: " + QRScanner.qrCodeType.get(getIntent().getIntExtra("codeFormat",0)));
                        }
                    }.start();*/
                    boolean gen = getIntent().getBooleanExtra("generated", false);
                    long newId = DBSavePic.savePicToDB(getApplicationContext(), finalBitmap, gen, textCode1.toString(),
                            type, codeFormat, json);

                    Snackbar.make(v,"Создано!", Snackbar.LENGTH_LONG).show();

                    String s = "sharePic.png";
                    File file = new File(getApplicationContext().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic", s);
                    fOut = new FileOutputStream(file);



                    finalBitmap.compress(Bitmap.CompressFormat.PNG,100, fOut);
                    String channel_ID = "12311";
                    String channel_name = "QRCodeChannel";
                    String channel_description = "123123111";

                    MyService.gen = getIntent().getBooleanExtra("generated",false);

                    MyService.uri = FileProvider.getUriForFile(getApplicationContext(),"com.example.myapplication.provider", file);

                    //startService(new Intent(PicViewActivity.this, MyService.class))


                    Intent intentA = new Intent(PicViewActivity.this, MyService.class);

                    intentA.setAction("copy");
                    /*PendingIntent pendingIntentA = PendingIntent.getBroadcast(
                            getApplicationContext(),
                            R.drawable.ic_baseline_qr_code_24,
                            intentA,
                            0);*/
                    PendingIntent pendingIntentA = PendingIntent.getService(
                            getApplicationContext(),
                            0,
                            intentA,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

                    Intent intentB = new Intent(getApplicationContext(), MyService.class);
                    intentB.putExtra("extra", newId);
                    intentB.putExtra("extra1", gen);
                    intentB.setAction("to");
                    PendingIntent pendingIntentB = PendingIntent.getService(
                            getApplicationContext(),
                            1,
                            intentB,
                            PendingIntent.FLAG_UPDATE_CURRENT);



                    Notification notification = new NotificationCompat.Builder(getApplicationContext(),"channelQRCode")
                            .setSmallIcon(R.drawable.ic_baseline_qr_code_scanner_24)
                            .setContentTitle(QRScanner.qrCodeType.get(type) + " | " + QRScanner.textType.get(codeFormat))
                            .setContentText("Новое изображение")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setChannelId(channel_ID)
                            .setLargeIcon(finalBitmap)
                            .addAction(R.drawable.ic_baseline_qr_code_24,"Скопировать",pendingIntentA)
                            .addAction(R.drawable.ic_baseline_qr_code_scanner_24,"Перейти", pendingIntentB)
                            //.setContentIntent(PendingIntent.getActivity(getApplicationContext(),0,new Intent(getApplicationContext(),MainActivity.class),PendingIntent.FLAG_CANCEL_CURRENT))
                            .setStyle(
                                    new NotificationCompat.BigPictureStyle()
                                    .bigPicture(finalBitmap)
                            )
                            //.setOngoing(true)
                            .build();



                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(channel_ID, channel_name, NotificationManager.IMPORTANCE_DEFAULT);
                        channel.setDescription(channel_description);
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                    }


                    notificationManager.notify(12311, notification);


                    fOut.flush();
                    fOut.close();

                } catch (Exception e){
                    e.printStackTrace();
                    Log.e("Noti11", e.getMessage());
                }
                finish();
            }
        });

        copyText = findViewById(R.id.copyText);
        String finalTextCode = textCode.toString();
        copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(finalTextCode, finalTextCode));
                Snackbar.make(v,"Текст скопирован",Snackbar.LENGTH_SHORT).setBackgroundTint(Color.DKGRAY).show();
            }
        });

        copyBitmap = findViewById(R.id.copyBitmap);
        copyBitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(finalTextCode, finalTextCode));
                    ClipboardManager mClipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

                    String s = "copyPicFromActivity.png";
                    File file = new File(getApplicationContext().getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic", s);
                    OutputStream fOut = new FileOutputStream(file);
                    finalBitmap.compress(Bitmap.CompressFormat.PNG,100, fOut);

                    Uri uri1 = FileProvider.getUriForFile(getApplicationContext(),"com.example.myapplication.provider", file);
                    ClipData theClip = ClipData.newUri(getApplicationContext().getContentResolver(), "bitmap", uri1);
                    mClipboard.setPrimaryClip(theClip);

                    Snackbar.make(v, "Картинка скопирована", Snackbar.LENGTH_SHORT).setBackgroundTint(Color.DKGRAY).show();
                } catch (Exception e){
                    Snackbar.make(v,"Ошибка: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

}