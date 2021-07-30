package com.example.qrcodescanner

import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.example.qrcodescanner.SimpleClasses.ViewGroup
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.OnColorSelectionListener

class ScannerSettingsActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var settings: SharedPreferences

    companion object Const{
        val SETTINGS_FOR_ACTIVE_QR: String = "0"
        val SETTINGS_FOR_NOT_ACTIVE_QR: String = "1"
    }

    var views = ArrayList<ViewGroup>()

    lateinit var components: ArrayList<View>
    lateinit var viewX: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_settings)

        components = arrayListOf(
            findViewById<Button>(R.id.colorForActiveQR),
            findViewById<Button>(R.id.colorForNotActiveQR)
        )

        viewX = layoutInflater.inflate(R.layout.dialog_color_picker, null)

        viewX.findViewById<HSLColorPicker>(R.id.HSLColorPicker)
            .setColorSelectionListener(object : OnColorSelectionListener {
                override fun onColorSelected(color: Int) {
                    viewX.findViewById<Button>(R.id.pickedColor).setBackgroundColor(color)
                    pickedColor = color
                }

                override fun onColorSelectionEnd(color: Int) {

                }

                override fun onColorSelectionStart(color: Int) {

                }

            })


        settings = getSharedPreferences("Scanner", MODE_PRIVATE)
        //settings.edit().clear().apply()

        findViewById<Button>(R.id.startSettingsButton).setOnClickListener {
            deleteSettings()
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveSettings()
        }

        if (!settings.contains("0")) {
            deleteSettings()
        } else {
            loadSettings()
        }

        for ((l, view) in components.withIndex()) {

            val x = settings.getString(l.toString(), "1")!!

            when (view) {
                is Button -> {
                    view.setOnClickListener(this)
                    view.setBackgroundColor(x.toInt())
                }
            }
        }



        /*for ((l, view) in views.withIndex()){
            when (view){
                is Button -> {
                    view.setOnClickListener(this)
                    view.setBackgroundColor(settings.getString(l.toString(), "")!!.toInt())
                }
            }
        }*/

    }

    private fun loadSettings(){
        views.clear()

        for ((l, view) in components.withIndex()){
            views.add(ViewGroup(view,settings.getString(l.toString(),"1")!!))
        }
    }

    private fun saveSettings() {
        val settingEditor = settings.edit()

        for ((l, view) in views.withIndex()) {
            settingEditor.putString(l.toString(), view.setting)
        }

        settingEditor.apply()
        finish()
    }

    private fun deleteSettings() {
        val settingsEditor = settings.edit()

        findViewById<Button>(R.id.colorForActiveQR).setBackgroundColor(Color.RED)
        settingsEditor.putString(SETTINGS_FOR_ACTIVE_QR, Color.RED.toString())
        findViewById<Button>(R.id.colorForNotActiveQR).setBackgroundColor(Color.BLUE)
        settingsEditor.putString(SETTINGS_FOR_NOT_ACTIVE_QR, Color.BLUE.toString())

        settingsEditor.apply()

        loadSettings()
    }

    var pickedColor = 0
    var dialog: AlertDialog? = null
    var viewGroupElementIndex: Int = 0

    override fun onClick(v: View?) {

        viewGroupElementIndex = views.indexOf(views.filter { x -> x.view == v }[0])

        pickedColor = views[viewGroupElementIndex].setting.toInt()
        if (dialog == null) dialog = AlertDialog.Builder(this)
            .setTitle("")
            .setView(viewX)
            .setNegativeButton("Отмена") { dialog, p ->
                dialog.dismiss()
            }
            .setPositiveButton("OK") { dialog, p ->
                views[viewGroupElementIndex].view.setBackgroundColor(pickedColor)
                views[viewGroupElementIndex].setting = pickedColor.toString()
                dialog.dismiss()
            }
            .create()

        val set = views[viewGroupElementIndex].setting.toInt()
        with(viewX){
            findViewById<HSLColorPicker>(R.id.HSLColorPicker).setColor(set)
            findViewById<Button>(R.id.pickedColor).setBackgroundColor(set)
        }


        dialog!!.show()
    }
}