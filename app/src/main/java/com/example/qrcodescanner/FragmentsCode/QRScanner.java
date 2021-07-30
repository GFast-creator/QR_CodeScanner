package com.example.qrcodescanner.FragmentsCode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.qrcodescanner.PicViewActivity;
import com.example.qrcodescanner.R;
import com.example.qrcodescanner.ResultPicker;
import com.example.qrcodescanner.ScannerSettingsActivity;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.TreeMap;

import ezvcard.Ezvcard;
import ezvcard.VCard;

public class QRScanner extends Fragment {
    public static TreeMap<Integer,String> textType = new TreeMap<>();

    static {
        textType.put(Barcode.URL,"Ссылка");
        textType.put(Barcode.EMAIL,"Email");
        textType.put(Barcode.GEO,"Геолокация");
        textType.put(Barcode.PHONE,"Номер телефона");
        textType.put(Barcode.TEXT,"Текст");
        textType.put(Barcode.WIFI,"Wi-Fi");
        textType.put(Barcode.CALENDAR_EVENT,"Событие");
        textType.put(Barcode.CONTACT_INFO,"Контакт");
        textType.put(Barcode.PRODUCT,"Код продукта");
    }

    public static TreeMap<Integer,String> qrCodeType = new TreeMap<>();
    static {
        qrCodeType.put(Barcode.AZTEC,"AZTEC");
        qrCodeType.put(Barcode.CODABAR,"CODABAR");
        qrCodeType.put(Barcode.CODE_39,"CODE_39");
        qrCodeType.put(Barcode.CODE_93, "CODE_93");
        qrCodeType.put(Barcode.CODE_128,"CODE_128");
        qrCodeType.put(Barcode.QR_CODE,"QR_CODE");
        qrCodeType.put(Barcode.DATA_MATRIX,"DATA_MATRIX");
        qrCodeType.put(Barcode.EAN_8,"EAN_8");
        qrCodeType.put(Barcode.EAN_13,"EAN_13");
        qrCodeType.put(Barcode.ISBN,"ISBN");
        qrCodeType.put(Barcode.ITF,"ITF");
        qrCodeType.put(Barcode.PDF417,"PDF417");
        qrCodeType.put(Barcode.UPC_A,"UPC_A");
        qrCodeType.put(Barcode.UPC_E,"UPC_E");
    }

    private int firstColor;
    private int secondColor;
    static View buttonView;
    public Uri imageUri;
    SparseArray<Barcode> qrCodes;
    ImageView buttonTakePic;
    static public SurfaceView surfaceView;
    SurfaceView cV;
    SurfaceHolder cVHolder;
    CameraSource cameraSource;
    static public float h1 = 0;
    public static int w,h;
    View view;

    TextView textView;
    ShapeableImageView imageButton;

    BarcodeDetector barcodeDetector;
    Canvas canvas;
    Paint paint = new Paint();

    static boolean isChecked = false;
    static boolean canScreen = true;
    static boolean screenTouch = false;
    static final int RESULT_LOAD_IMG = 110;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_qrscanner, container, false);
        bitmap = null;
        intent = new Intent(getContext(), PicViewActivity.class);

        view.findViewById(R.id.settingsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ScannerSettingsActivity.class));
            }
        });

        view.findViewById(R.id.imageButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ResultPicker.class));
            }
        });

        view.findViewById(R.id.takePic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonTakePic.setImageResource(R.drawable.ic_twotone_camera_touched_24);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buttonTakePic.setImageResource(R.drawable.ic_twotone_camera_24);
                    }
                }, 200);

                if (canScreen && qrCodes.size()!=0) {
                    canScreen = false;
                    screenTouch = true;

                    intent.putExtra("codeFormat",qrCodes.valueAt(0).valueFormat);
                    intent.putExtra("codeText",qrCodes.valueAt(0).displayValue);
                    intent.putExtra("extra", (Bundle) null);

                    switch (qrCodes.valueAt(0).valueFormat){
                        case Barcode.CONTACT_INFO:
                            Barcode.ContactInfo contactInfo = qrCodes.valueAt(0).contactInfo;
                            String val = qrCodes.valueAt(0).rawValue;
                            if (val.startsWith("VCARD")){
                                VCard vCard = Ezvcard.parse(val).first();
                                contactInfo.name.formattedName = vCard.getFormattedName().getValue();
                            }
                            intent.putExtra("extra", contactInfo);
                            break;
                        case Barcode.WIFI:
                            intent.putExtra("extra", qrCodes.valueAt(0).wifi);
                            break;
                        case Barcode.PHONE:
                            intent.putExtra("extra", qrCodes.valueAt(0).phone);
                            break;
                        case Barcode.URL:
                            intent.putExtra("extra", qrCodes.valueAt(0).url);
                            break;
                        case Barcode.EMAIL:
                            intent.putExtra("extra", qrCodes.valueAt(0).email);
                            break;
                        case Barcode.GEO:
                            intent.putExtra("extra", qrCodes.valueAt(0).geoPoint);
                            break;
                        case Barcode.CALENDAR_EVENT:
                            intent.putExtra("extra", qrCodes.valueAt(0).calendarEvent);
                            break;
                    }

                    intent.putExtra("type",qrCodes.valueAt(0).format);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            canScreen = true;
                        }
                    }, 500);
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Сканер QR-кода");

        SharedPreferences settings = getActivity().getSharedPreferences("Scanner", Context.MODE_PRIVATE);

        firstColor = Integer.parseInt(
                settings.getString(
                        ScannerSettingsActivity.Const.getSETTINGS_FOR_ACTIVE_QR(),
                        String.valueOf(Color.RED)
                )
        );

        secondColor = Integer.parseInt(
                settings.getString(
                        ScannerSettingsActivity.Const.getSETTINGS_FOR_NOT_ACTIVE_QR(),
                        String.valueOf(Color.BLUE)
                )
        );

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();


        assert v != null;
        cV = v.findViewById(R.id.cV);
        cV.setZOrderOnTop(true);


        cVHolder = cV.getHolder();
        surfaceView = v.findViewById(R.id.camerapreview);
        textView = v.findViewById(R.id.textView);
        buttonTakePic = v.findViewById(R.id.takePic);
        imageButton = v.findViewById(R.id.imageButton);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    isChecked = !isChecked;
                    imageButton.setImageResource(isChecked ? R.drawable.ic_baseline_highlight_on_24 : R.drawable.ic_outline_highlight_off_24);
                    setFlash();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        barcodeDetector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();
        cameraSource = new CameraSource.Builder(getContext(), barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .setRequestedFps(60.0f)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getContext(), "Нет разрешения на использование камеры", Toast.LENGTH_LONG).show();
                } else {

                    try {
                        cameraSource.start(holder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }

        });


        cVHolder.setFormat(PixelFormat.TRANSPARENT);
        paint.setColor(secondColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(23);


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                qrCodes = detections.getDetectedItems();

                canvas = cVHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                if (canvas != null) {

                    if (h1 == 0){
                        w = canvas.getWidth();
                        h = canvas.getHeight();
                        h1 = (float)(canvas.getHeight()) / 1920f;

                        Log.e("EEE11", String.valueOf(((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight()));
                        //h1 = (float)(canvas.getHeight()) / getActivity().getWindow().getDecorView().getHeight();
                        //h1 = (float)(canvas.getHeight()) / (((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight()-365);
                    }

                    try {

                        if (qrCodes.size() != 0) {
                            for (int i = 0; i < qrCodes.size(); i++) {

                                if (i == 0) {
                                    paint.setColor(firstColor);
                                } else if (i == 1) paint.setColor(secondColor);
                                try {
                                    qrCodes.valueAt(i).cornerPoints[0].y *= h1;
                                    qrCodes.valueAt(i).cornerPoints[1].y *= h1;
                                    qrCodes.valueAt(i).cornerPoints[2].y *= h1;
                                    qrCodes.valueAt(i).cornerPoints[3].y *= h1;
                                    draw(qrCodes.valueAt(i).cornerPoints, canvas, paint);
                                } catch (IndexOutOfBoundsException e) {
                                    Log.e("OutOfBounds", e.getMessage());
                                }

                            }

                            if (screenTouch) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        takePicture(qrCodes.valueAt(0).cornerPoints);
                                    }
                                }.start();
                                screenTouch = false;
                            }
                        }

                    } finally {
                        cVHolder.unlockCanvasAndPost(canvas);
                    }
                    if (qrCodes.size() != 0) {
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    textView.setText(qrCodes.valueAt(0).displayValue.length()<=25?
                                            qrCodes.valueAt(0).displayValue:
                                            qrCodes.valueAt(0).displayValue.substring(0,26).concat("..."));
                                } catch (Exception ignored){}
                            }
                        });
                    } else {
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(R.string.textForNoQrCode);
                            }
                        });
                    }
                }
            }
        });
    }

    public static void draw(Point[] points, Canvas canvas2, Paint paint) {
        /*p1.setXY(points[0].x, points[0].y*h1);
        p2.setXY(points[1].x, points[1].y*h1);
        p3.setXY(points[2].x, points[2].y*h1);
        p4.setXY(points[3].x, points[3].y*h1);*/
        canvas2.drawLine(points[0].x, points[0].y, points[1].x, points[1].y, paint);
        canvas2.drawLine(points[1].x, points[1].y, points[2].x, points[2].y, paint);
        canvas2.drawLine(points[2].x, points[2].y, points[3].x, points[3].y, paint);
        canvas2.drawLine(points[3].x, points[3].y, points[0].x, points[0].y, paint);
        canvas2.drawPoint(points[0].x, points[0].y, paint);
        canvas2.drawPoint(points[1].x, points[1].y, paint);
        canvas2.drawPoint(points[2].x, points[2].y, paint);
        canvas2.drawPoint(points[3].x, points[3].y, paint);
    }

    public static Camera getCamera(CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    return (Camera) field.get(cameraSource);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        return null;
    }


    @SuppressLint("MissingPermission")
    private void setFlash() throws IOException {
        getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Нет доступа к камере", Toast.LENGTH_LONG).show();
            return;
        }
        cameraSource.start(surfaceView.getHolder());
        Camera _cam = getCamera(cameraSource);
        if (_cam != null) {
            Camera.Parameters _pareMeters = _cam.getParameters();
            _pareMeters.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            _cam.setParameters(_pareMeters);
            _cam.startPreview();
        }
    }

    Bitmap bitmap;
    Intent intent;
    private void takePicture(Point[] points) {
        cameraSource.takePicture(
                null,
                new CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(@NonNull byte[] bytes) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                canScreen = true;
                            }
                        }, 1000);

                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        //поворот на 90 градусов
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);

                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                        Log.e("saveImage", bitmap.getWidth() + " " + bitmap.getHeight());

                        Paint paint1 = new Paint();
                        paint1.setColor(secondColor);
                        paint1.setStyle(Paint.Style.STROKE);
                        paint1.setStrokeWidth(46);

                        Canvas canvas1 = new Canvas(bitmap);

                        double height1 = canvas1.getHeight()/(1.0*h);
                        double width1 = canvas1.getWidth()/(1.0*w);

                        points[0].x = (int)(points[0].x*width1)-100;
                        points[1].x = (int)(points[1].x*width1)+100;
                        points[2].x = (int)(points[2].x*width1)+100;
                        points[3].x = (int)(points[3].x*width1)-100;

                        points[0].y = (int)(points[0].y*height1/h1)-100;
                        points[1].y = (int)(points[1].y*height1/h1)-100;
                        points[2].y = (int)(points[2].y*height1/h1)+100;
                        points[3].y = (int)(points[3].y*height1/h1)+100;



                        try{
                            bitmap = Bitmap.createBitmap(
                                    bitmap,
                                    Math.max(points[0].x, 0),
                                    Math.max(points[0].y + 100, 0),
                                    Math.max(points[1].x,points[2].x) < bitmap.getWidth()? Math.max(points[1].x,points[2].x) - Math.min(points[0].x, points[3].x) : bitmap.getWidth() - Math.max(points[0].x, 0),
                                    Math.max(points[2].y,points[3].y) < bitmap.getHeight()? Math.max(points[2].y,points[3].y) + 100 - Math.min(points[0].y, points[1].y) : bitmap.getHeight() - Math.max(points[0].y, 0)
                            );
                        } catch (Exception e){
                            draw(points,canvas1,paint1);
                            Toast.makeText(getContext(),"QR-код выходит за рамки", Toast.LENGTH_LONG).show();
                        }


                        try {

                            File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()+"/intentPic");

                            Log.e("saveImage:", Boolean.toString(file.mkdir()));

                            /*{Log.e( "saveImage:", file.getAbsolutePath());
                            Log.e( "saveImage:", Boolean.toString(file.isFile()));
                            Log.e( "saveImage:", Boolean.toString(file.isDirectory()));
                            }*/

                            String s = "intentPic.png";
                            file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()+"/intentPic"
                                    ,s);

                            Log.e("saveImage:", Boolean.toString(file.createNewFile()));

                            /*{Log.e( "saveImage:", file.getAbsolutePath());
                            Log.e( "saveImage:", Boolean.toString(file.isFile()));
                            Log.e( "saveImage:", Boolean.toString(file.isDirectory()));
                            Log.e( "saveImage:", file.canWrite()?"Yes":"No");
                            Log.e("saveImage:" , "1");
                            }*/

                            OutputStream fOut = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                            fOut.flush();
                            fOut.close();

                            intent.putExtra("nameOfPic",s);
                            intent.putExtra("generated", 0);
                            intent.putExtra("View?", false);
                            startActivity(intent);

                        } catch (Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("saveImage:", "7 :" + e.getMessage());
                        }
                    }
                }
        );
    }

}
