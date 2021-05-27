package com.example.qrcodescanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.qrcodescanner.FragmentsCode.QRCodeCreateFragment;
import com.example.qrcodescanner.FragmentsCode.QRScanner;
import com.example.qrcodescanner.FragmentsCode.SavedPicFragment;
import com.example.qrcodescanner.FragmentsCode.SavedPicGeneratedFragment;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import ezvcard.Ezvcard;
import ezvcard.VCard;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private int itemOnView;
    private boolean readyToClose;

    static ArrayList<String> allowedType = new ArrayList<>();

    static {
        allowedType.add("text/plain");
        allowedType.add("text/x-vcard");
        allowedType.add("text/calendar");
        allowedType.add("text/x-vcalendar");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/ScannedQRCode/").mkdir();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_open,R.string.navigation_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.e("123123", action + " " + type);
        try {
            if (action != null) {
                if (type != null)
                if (action.equals(Intent.ACTION_SEND)) {
                    if (allowedType.contains(type)) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new QRCodeCreateFragment()).commit();
                        itemOnView = R.id.nav_QRCodeGenerator;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                if (!type.equals("text/plain")) {
                                    Log.e("123123", type);
                                    Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                                    Scanner scanner = null;
                                    StringBuilder s = new StringBuilder();
                                    try {
                                        scanner = new Scanner(getContentResolver().openInputStream(uri));
                                        while (scanner.hasNext()) {
                                            s.append(scanner.nextLine()).append("\n");
                                        }
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    Intent intent1 = new Intent(MainActivity.this, PicViewActivity.class);
                                    QRGEncoder qrgEncoder;

                                    switch (type) {
                                        case "text/x-vcard":
                                            intent1.putExtra("codeFormat", Barcode.CONTACT_INFO);
                                            Barcode.ContactInfo contactInfo = new Barcode.ContactInfo();
                                            VCard vcard = Ezvcard.parse(s.toString()).first();
                                            Log.e("123123", vcard != null?(vcard.toString()):("")+ "\n");
                                            Log.e("123123",vcard.getSourceDisplayText() != null?vcard.getSourceDisplayText().getValue():"0");
                                            //Log.e("123123",Ezvcard.write(vcard).go());
                                            s = new StringBuilder(Ezvcard.write(vcard).go());

                                            contactInfo.name = new Barcode.PersonName();
                                            //Log.e("123123", vcard.getFormattedName().getValue() + ", " + contactInfo.name.formattedName);
                                            contactInfo.name.formattedName = vcard.getFormattedName().getValue() == null ? "(Неизвестно)" : vcard.getFormattedName().getValue();
                                            contactInfo.addresses = new Barcode.Address[vcard.getAddresses().size()];

                                            if (contactInfo.addresses.length != 0) {
                                                for (int i = 0; i<contactInfo.addresses.length; i++) {
                                                    contactInfo.addresses[i].addressLines =  new String[]{vcard.getAddresses().get(i).getStreetAddress()};
                                                    Log.e("123123", contactInfo.addresses[i].addressLines[0]);
                                                    contactInfo.addresses[i].type = ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME;
                                                }
                                            }

                                            contactInfo.emails = new Barcode.Email[vcard.getEmails().size()];

                                            if (contactInfo.emails.length != 0) {
                                                for (int i = 0; i<contactInfo.emails.length; i++) {
                                                    contactInfo.emails[i].address = vcard.getEmails().get(i).getValue();
                                                    Log.e("123123", vcard.getEmails().get(i).getValue() + ", " + contactInfo.emails[i].address);
                                                    contactInfo.emails[i].type = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
                                                    Log.e("123123", ContactsContract.CommonDataKinds.Email.TYPE_HOME + ", " + contactInfo.emails[i].type);
                                                }
                                            }

                                            contactInfo.phones = new Barcode.Phone[vcard.getTelephoneNumbers().size()];
                                            if (contactInfo.phones.length != 0) {
                                                for (int i = 0; i< contactInfo.phones.length; i++) {
                                                    contactInfo.phones[i] = new Barcode.Phone();
                                                    Log.e("123123", "1");
                                                    Log.e("123123", vcard.getTelephoneNumbers().get(i).getText());
                                                    Log.e("123123", "2");
                                                    contactInfo.phones[i].number = vcard.getTelephoneNumbers().get(i).getText();
                                                    Log.e("123123", contactInfo.phones[i].number);
                                                    Log.e("123123", "3");


                                                    Log.e("123123", "1");
                                                    Log.e("123123", String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME));
                                                    Log.e("123123", "2");
                                                    contactInfo.phones[i].type = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
                                                    Log.e("123123", String.valueOf(contactInfo.phones[i].type));
                                                }
                                            }

                                            intent1.putExtra("extra",contactInfo);



                                            break;
                                        case "text/x-vcalendar":
                                            intent1.putExtra("codeFormat", Barcode.CALENDAR_EVENT);
                                            Barcode.CalendarEvent calendarEvent = new Barcode.CalendarEvent();

                                            ICalendar iCalendar = Biweekly.parse(s.toString()).first();
                                            VEvent event = iCalendar.getEvents().get(0);

                                            StringBuilder newS = new StringBuilder();
                                            newS.append("BEGIN:VEVENT\n");


                                            if (event.getSummary() != null){
                                                calendarEvent.summary = event.getSummary().getValue();
                                                newS.append("SUMMARY:").append(calendarEvent.summary).append("\n");
                                            }
                                            if (event.getLocation() != null){
                                                calendarEvent.location = event.getLocation().getValue();
                                                newS.append("LOCATION:").append(calendarEvent.location).append("\n");
                                            }
                                            if (event.getDescription() != null){
                                                calendarEvent.description = event.getDescription().getValue();
                                                newS.append("DESCRIPTION:").append(calendarEvent.description).append("\n");
                                            }

                                            if (event.getOrganizer() != null){
                                                calendarEvent.organizer = event.getOrganizer().getEmail();
                                                newS.append("ORGANIZER:").append(calendarEvent.organizer).append("\n");
                                            }

                                            if (event.getDateStart() != null) {
                                                calendarEvent.start = new Barcode.CalendarDateTime();
                                                calendarEvent.start.year = event.getDateStart().getValue().getRawComponents().getYear();
                                                calendarEvent.start.month = event.getDateStart().getValue().getRawComponents().getMonth();
                                                calendarEvent.start.day = event.getDateStart().getValue().getRawComponents().getDate();
                                                calendarEvent.start.hours = event.getDateStart().getValue().getRawComponents().getHour();
                                                calendarEvent.start.minutes = event.getDateStart().getValue().getRawComponents().getMinute();
                                                calendarEvent.start.seconds = event.getDateStart().getValue().getRawComponents().getSecond();
                                                newS.append("DTSTART:").append(calendarEvent.start.year).append(String.valueOf(calendarEvent.start.month).length()==2?calendarEvent.start.month:"0"+calendarEvent.start.month).append(String.valueOf(calendarEvent.start.day).length()==2?calendarEvent.start.day:"0"+calendarEvent.start.day).append("T")
                                                        .append(String.valueOf(calendarEvent.start.hours).length()==2?calendarEvent.start.hours:"0"+calendarEvent.start.hours).append(String.valueOf(calendarEvent.start.minutes).length()==2?calendarEvent.start.minutes:"0"+calendarEvent.start.minutes).append(String.valueOf(calendarEvent.start.seconds).length()==2?calendarEvent.start.seconds:"0"+calendarEvent.start.seconds).append("\n");
                                            }
                                            calendarEvent.end = new Barcode.CalendarDateTime();
                                            if (event.getDateEnd() != null) {
                                                calendarEvent.end.year = event.getDateEnd().getValue().getRawComponents().getYear();
                                                calendarEvent.end.month = event.getDateEnd().getValue().getRawComponents().getMonth();
                                                calendarEvent.end.day = event.getDateEnd().getValue().getRawComponents().getDate();
                                                calendarEvent.end.hours = event.getDateEnd().getValue().getRawComponents().getHour();
                                                calendarEvent.end.minutes = event.getDateEnd().getValue().getRawComponents().getMinute();
                                                calendarEvent.end.seconds = event.getDateEnd().getValue().getRawComponents().getSecond();

                                                newS.append("DTEND:").append(calendarEvent.end.year).append(String.valueOf(calendarEvent.end.month).length()==2?calendarEvent.end.month:"0"+calendarEvent.end.month).append(String.valueOf(calendarEvent.end.day).length()==2?calendarEvent.end.day:"0"+calendarEvent.end.day).append("T")
                                                        .append(String.valueOf(calendarEvent.end.hours).length()==2?calendarEvent.end.hours:"0"+calendarEvent.end.hours).append(String.valueOf(calendarEvent.end.minutes).length()==2?calendarEvent.end.minutes:"0"+calendarEvent.end.minutes).append(String.valueOf(calendarEvent.end.seconds).length()==2?calendarEvent.end.seconds:"0"+calendarEvent.end.seconds).append("\n");
                                            }

                                            newS.append("END:VEVENT");
                                            s = newS;
                                            intent1.putExtra("extra", calendarEvent);
                                            break;
                                    }

                                    qrgEncoder = new QRGEncoder(s.toString(), null, QRGContents.Type.TEXT, 500);
                                    qrgEncoder.setColorBlack(Color.GRAY);

                                    Bitmap bitmap = qrgEncoder.getBitmap();


                                    File file = new File(getApplicationContext()
                                            .getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic");

                                    Log.e("123123", Boolean.toString(file.mkdirs()));

                                    {
                                        Log.e("123123", file.getAbsolutePath());
                                        Log.e("123123", Boolean.toString(file.isFile()));
                                        Log.e("123123", Boolean.toString(file.isDirectory()));
                                    }

                                    String s1 = "intentPicG.png";
                                    file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic"
                                            , s1);

                                    Log.e("123123", Boolean.toString(file.createNewFile()));

                                    {
                                        Log.e("123123", file.getAbsolutePath());
                                        Log.e("123123", Boolean.toString(file.isFile()));
                                        Log.e("123123", Boolean.toString(file.isDirectory()));
                                        Log.e("123123", file.canWrite() ? "Yes" : "No");
                                        Log.e("123123", "1");
                                    }

                                    OutputStream fOut;
                                    fOut = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                    fOut.flush();
                                    fOut.close();

                                    intent1.putExtra("codeText", s.toString())
                                            .putExtra("generated", false)
                                            .putExtra("nameOfPic", s1)
                                            .putExtra("type", Barcode.QR_CODE)
                                            .putExtra("View?", false);


                                    startActivity(intent1);

                                    Log.e("123123", "done");
                                } else {
                                    QRCodeCreateFragment.handleTextIntent(intent);
                                }
                                } catch (Exception e){
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.e("123123", e.getMessage());
                                }
                            }
                            }, 100);
                        return;
                    }
                }
                if (action.equals("serviceShowPic")){
                    boolean gen = intent.getBooleanExtra("generated", false);
                    if (gen){
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SavedPicGeneratedFragment()).commit();
                        itemOnView = R.id.nav_savedPic_generated;
                    } else {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new SavedPicFragment()).commit();
                        itemOnView = R.id.nav_savedPic;
                    }
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    long id = intent.getLongExtra("idSQL", -1);
                                    if (gen){
                                        SavedPicGeneratedFragment.showPic(getApplicationContext(), (int)id);
                                    }else{
                                        SavedPicFragment.showPic(getApplicationContext(), (int)id);
                                    }
                                }
                            }
                    , 800);
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("123123", e.toString());
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new QRScanner()).commit();
        itemOnView = R.id.nav_QRScanner;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_QRCodeGenerator:{
                if (itemOnView != R.id.nav_QRCodeGenerator)getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new QRCodeCreateFragment()).commit();
                itemOnView = R.id.nav_QRCodeGenerator;
                break;
            }
            case R.id.nav_QRScanner:{
                if (itemOnView != R.id.nav_QRScanner)getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new QRScanner()).commit();
                itemOnView = R.id.nav_QRScanner;
                break;
            }
            case R.id.nav_savedPic:{
                if (itemOnView != R.id.nav_savedPic)getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new SavedPicFragment()).commit();
                itemOnView = R.id.nav_savedPic;
                break;
            }
            case R.id.nav_savedPic_generated:{
                if (itemOnView != R.id.nav_savedPic_generated)getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SavedPicGeneratedFragment()).commit();
                itemOnView = R.id.nav_savedPic_generated;
                break;
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (itemOnView == R.id.nav_savedPic) {
                if (SavedPicFragment.isSelectMode)
                    SavedPicFragment.setIsSelectMode(false);
                else exit();
            } else if (itemOnView == R.id.nav_savedPic_generated) {
                if (SavedPicGeneratedFragment.isSelectMode)
                    SavedPicGeneratedFragment.setSelectMod(false);
                else exit();
            }
            else{
                exit();
            }
        }
    }

    public void exit(){
        if (readyToClose) {
            super.onBackPressed();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    readyToClose = false;
                }
            }, 2000);
            readyToClose = true;
            Toast.makeText(getApplicationContext(), "Нажмите ещё раз чтоб выйти", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        //notificationManager.cancelAll();
        super.onDestroy();
    }
}
