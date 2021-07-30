package com.example.qrcodescanner

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import com.example.qrcodescanner.FragmentsCode.QRScanner
import com.example.qrcodescanner.SimpleClasses.BitmapCum
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.Snackbar
import ezvcard.Ezvcard
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ResultPicker : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var listView: ListView
    var itemChoices = -1
    val RESULT_LOAD_IMG = 1101
    var firstColor = Color.RED
    var secondColor = Color.BLUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_picker)

        imageView = findViewById(R.id.picQR)
        listView = findViewById(R.id.listView)


        //получение цветов из настроек
        var settings = getSharedPreferences("Scanner", MODE_PRIVATE)
        firstColor = settings.getString(ScannerSettingsActivity.SETTINGS_FOR_ACTIVE_QR,Color.RED.toString())!!.toInt()
        secondColor = settings.getString(ScannerSettingsActivity.SETTINGS_FOR_NOT_ACTIVE_QR,Color.BLUE.toString())!!.toInt()



        val photoPickerIntent = Intent(Intent.ACTION_PICK);
        photoPickerIntent.type = "image/*";
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        findViewById<Button>(R.id.backButton).visibility = View.VISIBLE

        if (data != null) {

            when (requestCode) {
                RESULT_LOAD_IMG ->

                try {
                    val barcodeDetector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
                    val imageUri = data.getData()
                    val imageStream = applicationContext.contentResolver.openInputStream(imageUri!!)//getContext().getContentResolver().openInputStream(imageUri);]

                    selectedImage = BitmapFactory.decodeStream(imageStream)
                    var copyImage = selectedImage.copy(Bitmap.Config.ARGB_8888,true)


                    val frame = Frame.Builder().setBitmap(selectedImage).build()
                    barcodes = barcodeDetector.detect(frame)
                    val arr = mutableListOf<String>()
                    val canvas = Canvas(copyImage)
                    var paint1 = Paint()

                    with(paint1){
                        setColor(secondColor)
                        strokeWidth = 23F
                    }

                    barcodes.forEach { key, value ->
                        arr.add(value.displayValue)
                        QRScanner.draw(value.cornerPoints,canvas,paint1)
                    }
                    imageView.setImageBitmap(copyImage)

                    val imageAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,arr)
                    listView.adapter = imageAdapter
                    listView.choiceMode = AbsListView.CHOICE_MODE_SINGLE
                    listView.setOnItemClickListener { parent, view, position, id ->

                        itemChoices = position

                        copyImage = selectedImage.copy(Bitmap.Config.ARGB_8888,true)
                        var canvas1 = Canvas(copyImage)

                        var l = 0
                        barcodes.forEach { _, value ->

                            if (itemChoices == l)
                                paint1.color = firstColor
                            else
                                paint1.color = secondColor

                            QRScanner.draw(value.cornerPoints,canvas1,paint1)
                            l++
                        }
                        imageView.setImageBitmap(copyImage)
                        findViewById<Button>(R.id.conButton).visibility = View.VISIBLE


                    }

                    findViewById<Button>(R.id.conButton).setOnClickListener {
                        if (itemChoices != -1)
                            toResult()
                        else
                            Toast.makeText(applicationContext,"Не выбран ни один элеиент",Toast.LENGTH_LONG).show()
                    }

                    findViewById<Button>(R.id.backButton).setOnClickListener {
                        finish()
                    }

                } catch (e:Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show();
                    Log.e("saveImage:", "7 :" + e.message);
                }

            }

        }
    }

    lateinit var selectedImage:Bitmap
    lateinit var barcodes:SparseArray<Barcode>

    fun toResult(){
        try{

            var intent = Intent(this, PicViewActivity::class.java)
            intent.putExtra("codeFormat",barcodes.valueAt(itemChoices).valueFormat)
            intent.putExtra("codeText",barcodes.valueAt(itemChoices).displayValue)
            val x: Bundle? = null
            intent.putExtra("extra", x)

            when (barcodes.valueAt(itemChoices).valueFormat) {
                Barcode.CONTACT_INFO -> {
                    val contactInfo = barcodes.valueAt(itemChoices).contactInfo

                    if (barcodes.valueAt(itemChoices).rawValue.startsWith("VCARD")){
                        val vCard = Ezvcard.parse(barcodes.valueAt(itemChoices).rawValue).first();
                        contactInfo.name.formattedName = vCard.getFormattedName().getValue();
                    }
                    intent.putExtra("extra", contactInfo)
                }
                Barcode.WIFI->
                    intent.putExtra("extra", barcodes.valueAt(itemChoices).wifi)
                Barcode.PHONE->
                    intent.putExtra("extra", barcodes.valueAt(itemChoices).phone)
                Barcode.URL->
                    intent.putExtra("extra", barcodes.valueAt(itemChoices).url)
                Barcode.EMAIL->
                    intent.putExtra("extra", barcodes.valueAt(itemChoices).email)
                Barcode.GEO->
                    intent.putExtra("extra", barcodes.valueAt(itemChoices).geoPoint)
                Barcode.CALENDAR_EVENT->
                    intent.putExtra("extra", barcodes.valueAt(itemChoices).calendarEvent)
            }

            intent.putExtra("type",barcodes.valueAt(itemChoices).format);

            var file = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()+"/intentPic");

            Log.e("saveImage:", file.mkdir().toString());

            Log.e( "saveImage:", file.getAbsolutePath())
            Log.e( "saveImage:", file.isFile().toString())
            Log.e( "saveImage:", file.isDirectory().toString())

            var s = "intentPic.png";
            file = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()+"/intentPic"
                ,s);

            Log.e("saveImage:", file.createNewFile().toString());

            Log.e( "saveImage:", file.getAbsolutePath());
            Log.e( "saveImage:", file.isFile().toString())
            Log.e( "saveImage:", file.isDirectory().toString())
            Log.e( "saveImage:", file.canWrite().toString())
            Log.e("saveImage:" , "1");


            var fOut:OutputStream?
            fOut = FileOutputStream(file);
            (BitmapCum().cutBitmap(selectedImage,barcodes.valueAt(itemChoices).cornerPoints)).compress(
                Bitmap.CompressFormat.PNG, 100, fOut
            )
            fOut.flush();
            fOut.close();

            intent.putExtra("nameOfPic",s)
            intent.putExtra("generated", 0)
            intent.putExtra("View?", false)

            startActivity(intent)
        } catch (e:Exception){
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show();
            Log.e("saveImage:", "7 :" + e.message);
        }
    }
}