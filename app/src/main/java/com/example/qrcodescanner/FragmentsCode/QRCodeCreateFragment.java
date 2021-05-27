package com.example.qrcodescanner.FragmentsCode;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.qrcodescanner.IconPicker;
import com.example.qrcodescanner.PicViewActivity;
import com.example.qrcodescanner.R;
import com.google.android.gms.vision.barcode.Barcode;
import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRCodeCreateFragment extends Fragment {
    static ColorPicker colorPiker = null;
    Button saveButton;
    static EditText editText;
    static ImageView qrCode;
    static boolean isCreated;
    static Drawable background;
    View view;
    static QRGEncoder qrgEncoder;
    static int colorBlack;
    static Drawable drawable;

    Intent intent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        intent = new Intent(getContext(), PicViewActivity.class);
        view = inflater.inflate(R.layout.fragment_qrcode_create, container, false);

        return view;
    }

    private static void setQrCode() {
        String data = editText.getText().toString();

        if (data.isEmpty()) {
            isCreated = false;
            qrCode.setImageDrawable(background);
        } else {
            isCreated = true;
            qrgEncoder = new QRGEncoder(data, null, QRGContents.Type.TEXT, 500);
            qrgEncoder.setColorBlack(colorBlack);
            newQRCode();
        }
    }

    private static void newQRCode() {
        try {
            Bitmap bitmap = qrgEncoder.getBitmap();
            if (drawable != null){
                Canvas canvas = new Canvas(bitmap);
                Bitmap bitmap1 = ((BitmapDrawable)drawable).getBitmap();
                Matrix matrix = new Matrix();


                float scale = 0.1f;
                matrix.postScale(scale, scale);
                matrix.postTranslate((bitmap.getWidth()/2f)-(bitmap1.getWidth()*scale/2f), (bitmap.getHeight()/2f)-(bitmap1.getHeight()*scale/2f));

                Paint paint = new Paint();
                paint.setAntiAlias(true);

                paint.setColor(Color.WHITE);
                //canvas.drawCircle(bitmap.getWidth()/2f,bitmap.getHeight()/2f,bitmap1.getWidth()*scale,paint);
                Rect rect = new Rect();
                float k = 1.2f;
                rect.top = (bitmap.getHeight()/2) - (int)(bitmap1.getHeight()*scale/2f*k);
                rect.bottom = (bitmap.getHeight()/2) + (int)(bitmap1.getHeight()*scale/2f*k);
                rect.left = (bitmap.getWidth()/2) - (int)(bitmap1.getWidth()*scale/2f*k);
                rect.right = (bitmap.getWidth()/2) + (int)(bitmap1.getWidth()*scale/2f*k);
                canvas.drawRect(rect,paint);


                paint.setColor(Color.GRAY);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                //canvas.drawCircle(bitmap.getWidth()/2f,bitmap.getHeight()/2f,bitmap1.getWidth()*scale,paint);
                canvas.drawRect(rect,paint);

                Paint paint1 = new Paint();
                paint1.setAntiAlias(true);
                canvas.drawBitmap(bitmap1,matrix,paint1);

                //canvas.drawBitmap(bitmap1, (bitmap.getWidth()/2f) - (bitmap1.getWidth()/2f),(bitmap.getHeight()/2f) - (bitmap1.getHeight()/2f),new Paint());
            }
            qrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e("genQRCode", e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Генератор QR-кода");

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        isCreated = false;

        assert v != null;

        colorPiker = v.findViewById(R.id.colorPicker);
        colorBlack = Color.BLACK;
        colorPiker.setColor(colorBlack);
        colorPiker.setColorSelectionListener(new SimpleColorSelectionListener() {
            @Override
            public void onColorSelected(int color) {
                colorBlack = color;
                setQrCode();
            }
        });

        editText = v.findViewById(R.id.editText);
        qrCode = v.findViewById(R.id.imageView2);
        background = qrCode.getDrawable();
        saveButton = v.findViewById(R.id.button2);
        
        /*qrCode.getLayoutParams().height = qrCode.getLayoutParams().width;
        qrCode.requestLayout();*/

        v.findViewById(R.id.deleteIconButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawable = null;
                v.findViewById(R.id.deleteIconButton).setVisibility(View.GONE);
                newQRCode();
            }
        });

        v.findViewById(R.id.iconPickerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getContext(), IconPicker.class);
                startActivityForResult(intent2, 1);

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setQrCode();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        KeyboardVisibilityEvent.setEventListener(getActivity(),
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean b) {
                        ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) qrCode.getLayoutParams();
                        float fl = -250;
                        if (b) {
                            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(qrCode, "scaleX", 1f, 0.4f);
                            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(qrCode, "scaleY", 1f, 0.4f);
                            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(qrCode, "translationY", 0, fl);

                            ObjectAnimator ob1 = ObjectAnimator.ofFloat(editText, "translationY", 0, (fl) * 2);
                            AnimatorSet animationSet1 = new AnimatorSet();
                            animationSet1.play(objectAnimator3).with(objectAnimator2);
                            animationSet1.play(objectAnimator2).with(objectAnimator1);
                            animationSet1.play(objectAnimator1).with(ob1);
                            animationSet1.play(ob1);
                            animationSet1.start();

                            //v.findViewById(R.id.colorPicker).setVisibility(View.GONE);
                            //v.findViewById(R.id.deleteIconButton).setVisibility(View.GONE);
                            //v.findViewById(R.id.iconPickerButton).setVisibility(View.GONE);
                            //v.findViewById(R.id.colorPicker).setAlpha(0f);

                            ObjectAnimator obj = ObjectAnimator.ofFloat(v.findViewById(R.id.colorPicker),"alpha", 1f,0f);
                            obj.setDuration(0);
                            obj.start();

                            ObjectAnimator obj1 = ObjectAnimator.ofFloat(v.findViewById(R.id.textView2),"alpha", 1f,0f);
                            obj1.setDuration(0);
                            obj1.start();

                            ObjectAnimator obj2 = ObjectAnimator.ofFloat(v.findViewById(R.id.iconPickerButton),"alpha", 1f,0f);
                            obj2.setDuration(0);
                            obj2.start();

                            ObjectAnimator obj3 = ObjectAnimator.ofFloat(v.findViewById(R.id.deleteIconButton),"alpha", 1f,0f);
                            obj3.setDuration(0);
                            obj3.start();
                        } else {

                            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(qrCode, "scaleX", 0.5f, 1f);
                            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(qrCode, "scaleY", 0.5f, 1f);
                            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(qrCode, "translationY", fl, 0);

                            ObjectAnimator ob1 = ObjectAnimator.ofFloat(editText, "translationY", (fl) * 2, 0);
                            AnimatorSet animationSet = new AnimatorSet();
                            animationSet.play(objectAnimator3).with(objectAnimator2);
                            animationSet.play(objectAnimator2).with(objectAnimator1);
                            animationSet.play(objectAnimator1).with(ob1);
                            animationSet.play(ob1);
                            animationSet.start();

                            //v.findViewById(R.id.colorPicker).setVisibility(View.VISIBLE);
                            //v.findViewById(R.id.deleteIconButton).setVisibility(View.VISIBLE);
                            //v.findViewById(R.id.iconPickerButton).setVisibility(View.VISIBLE);

                            ObjectAnimator obj = ObjectAnimator.ofFloat(v.findViewById(R.id.colorPicker),"alpha", 0f,1f);
                            obj.setDuration(0);
                            obj.start();

                            ObjectAnimator obj1 = ObjectAnimator.ofFloat(v.findViewById(R.id.textView2),"alpha", 0f,1f);
                            obj1.setDuration(0);
                            obj1.start();

                            ObjectAnimator obj2 = ObjectAnimator.ofFloat(v.findViewById(R.id.iconPickerButton),"alpha", 0f,1f);
                            obj2.setDuration(0);
                            obj2.start();

                            ObjectAnimator obj3 = ObjectAnimator.ofFloat(v.findViewById(R.id.deleteIconButton), "alpha", 0f, 1f);
                            obj3.setDuration(0);
                            obj3.start();

                        }
                    }
                });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCreated) {
                    Bitmap bitmap = ((BitmapDrawable) qrCode.getDrawable()).getBitmap();
                    try {
                        File file = new File(getContext()
                                .getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic");

                        Log.e("saveImage:", Boolean.toString(file.mkdirs()));

                        {
                            Log.e("saveImage:", file.getAbsolutePath());
                            Log.e("saveImage:", Boolean.toString(file.isFile()));
                            Log.e("saveImage:", Boolean.toString(file.isDirectory()));
                        }

                        String s = "intentPicG.png";
                        file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/intentPic"
                                , s);

                        Log.e("saveImage:", Boolean.toString(file.createNewFile()));

                        {
                            Log.e("saveImage:", file.getAbsolutePath());
                            Log.e("saveImage:", Boolean.toString(file.isFile()));
                            Log.e("saveImage:", Boolean.toString(file.isDirectory()));
                            Log.e("saveImage:", file.canWrite() ? "Yes" : "No");
                            Log.e("saveImage:", "1");
                        }

                        OutputStream fOut;
                        fOut = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.flush();
                        fOut.close();

                        intent.putExtra("codeFormat", Barcode.TEXT);
                        intent.putExtra("codeText", editText.getText().toString());
                        intent.putExtra("generated", true);
                        intent.putExtra("nameOfPic", s);
                        intent.putExtra("type", Barcode.QR_CODE);
                        intent.putExtra("View?", false);

                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("saveImage:", e.getLocalizedMessage());
                    }
                } else {
                    Toast.makeText(getContext(), "Текс QR-кода не заполнен", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null){
            drawable = IconPicker.icons.get(data.getIntExtra("iconID",0));
            newQRCode();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.findViewById(R.id.deleteIconButton).setVisibility(View.VISIBLE);
                }
            }, 200);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        drawable = null;
    }

    public static void handleTextIntent(Intent intent/*, ContentResolver contentResolver*/) {
        StringBuffer displayName;
        displayName = new StringBuffer(intent.getStringExtra(Intent.EXTRA_TEXT));
        editText.setText(displayName);
        setQrCode();
    }
}