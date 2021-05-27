package com.example.qrcodescanner.SimpleClasses;

import android.content.Context;
import android.os.Vibrator;

public class Vibro {
    public static void vibrate(Context context, long ms){
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(ms);
    }
}
