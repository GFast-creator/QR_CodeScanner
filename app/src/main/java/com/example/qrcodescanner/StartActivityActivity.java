package com.example.qrcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class StartActivityActivity extends AppCompatActivity {

    private final static int REQUEST_CODE = 1;
    private static boolean permissionsGraded = false;
    private static boolean accessDeaned = false;
    private static final String[] perm = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_CONTACTS
    };
    private static AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        setTitle("Подождите...");
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Разрешение")
                .setMessage("Эти разрешения нужны для корректной работы! Для добавления разрешений нужно зайти в настройки приложения.")
                .setNegativeButton("ОК", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        permissionsGraded = true;
                    }
                })
                .create();


        ActivityCompat.requestPermissions(this,perm,REQUEST_CODE);


        new Thread(){
            @Override
            public void run() {
                while (!permissionsGraded) {
                    try {
                        sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Intent i = new Intent(StartActivityActivity.this, MainActivity.class);
                if (!accessDeaned) startActivity(i);
                finish();
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        accessDeaned = false;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Чтоб пользоваться приложением, нужно выдать разрешения! ", Toast.LENGTH_LONG).show();
                accessDeaned = true;
                break;
            }
        }

        if (accessDeaned) {
            alertDialog.show();
            return;
        }
        permissionsGraded = true;
    }
}