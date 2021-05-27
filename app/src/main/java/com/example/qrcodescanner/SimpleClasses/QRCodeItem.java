package com.example.qrcodescanner.SimpleClasses;

import android.graphics.Bitmap;

public class QRCodeItem {
    private long id;
    private Bitmap qrCodeBitmap;
    private String textOfQRCode;
    private int format;
    private int textFormat;
    private boolean isChecked;

    public QRCodeItem(long id, Bitmap qrCodeBitmap, String textOfQRCode, int format, int textFormat) {
        this.qrCodeBitmap = qrCodeBitmap;
        this.textOfQRCode = textOfQRCode;
        this.format = format;
        this.textFormat = textFormat;
        this.id = id;
        isChecked = false;
    }

    public Bitmap getQrCodeBitmap() {
        return qrCodeBitmap;
    }

    public void setQrCodeBitmap(Bitmap qrCodeBitmap) {
        this.qrCodeBitmap = qrCodeBitmap;
    }

    public String getTextOfQRCode() {
        return textOfQRCode;
    }

    public void setTextOfQRCode(String textOfQRCode) {
        this.textOfQRCode = textOfQRCode;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getTextFormat() {
        return textFormat;
    }

    public void setTextFormat(int textFormat) {
        this.textFormat = textFormat;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
