package com.example.qrcodescanner.SimpleClasses

import android.graphics.*
import com.example.qrcodescanner.FragmentsCode.QRScanner

class BitmapCum {
    fun cutBitmap(bitmap:Bitmap , points: Array<Point>):Bitmap {

        points[0].x = (points[0].x).toInt()
        points[1].x = (points[1].x).toInt()
        points[2].x = (points[2].x).toInt()
        points[3].x = (points[3].x).toInt()

        points[0].y = (points[0].y).toInt()
        points[1].y = (points[1].y).toInt()
        points[2].y = (points[2].y).toInt()
        points[3].y = (points[3].y).toInt()

        return Bitmap.createBitmap(
            bitmap,
            Math.max(Math.min(points[0].x, points[3].x) - 100, 0),
            Math.max(Math.min(points[0].y, points[1].y) - 100, 0),
            Math.min(
                Math.max(
                    points[1].x, points[2].x
                ) + 100 - Math.max(
                    Math.min(points[0].x, points[3].x) - 100 , 0
                ),
                bitmap.width - Math.max(Math.min(points[0].x, points[3].x) - 100, 0)
            ),
            Math.min(
                Math.max(
                    points[2].y, points[3].y
                ) + 100 - Math.max(
                    Math.min(points[0].y, points[1].y) - 100 , 0
                ),
                bitmap.height - Math.max(Math.min(points[0].y, points[1].y) - 100, 0)
            )
        )
    }

    fun drawOnBitmap (bitmap:Bitmap, points: Array<Point>): Bitmap{
        var bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        var canvas = Canvas(bitmap1)
        var paint = Paint()
        paint.color = Color.GREEN
        /*QRScanner.p1.setXY(points[0].x.toFloat(), points[0].y * QRScanner.h1)
        QRScanner.p2.setXY(points[1].x.toFloat(), points[1].y * QRScanner.h1)
        QRScanner.p3.setXY(points[2].x.toFloat(), points[2].y * QRScanner.h1)
        QRScanner.p4.setXY(points[3].x.toFloat(), points[3].y * QRScanner.h1)*/
        canvas.drawLine(points[0].x.toFloat(), points[0].y.toFloat(), points[1].x.toFloat(), points[1].y.toFloat(), paint)
        canvas.drawLine(points[1].x.toFloat(), points[1].y.toFloat(), points[2].x.toFloat(), points[2].y.toFloat(), paint)
        canvas.drawLine(points[2].x.toFloat(), points[2].y.toFloat(), points[3].x.toFloat(), points[3].y.toFloat(), paint)
        canvas.drawLine(points[3].x.toFloat(), points[3].y.toFloat(), points[0].x.toFloat(), points[0].y.toFloat(), paint)
        canvas.drawPoint(points[0].x.toFloat(), points[0].y.toFloat(), paint)
        canvas.drawPoint(points[1].x.toFloat(), points[1].y.toFloat(), paint)
        canvas.drawPoint(points[2].x.toFloat(), points[2].y.toFloat(), paint)
        canvas.drawPoint(points[3].x.toFloat(), points[3].y.toFloat(), paint)
        return bitmap1
    }
}