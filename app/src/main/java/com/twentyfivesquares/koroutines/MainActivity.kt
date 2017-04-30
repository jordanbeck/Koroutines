package com.twentyfivesquares.koroutines

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.imageBitmap
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    val photos = arrayOf(
            "http://www.beeculture.com/wp-content/uploads/2016/08/Working-bees-on-honey-cells-Shutterstock-800x430.jpg",
            "http://www.panna.org/sites/default/files/d6_issue_lead_image/lead-image-bee-campaign.jpg",
            "http://www.takepart.com/sites/default/files/styles/large/public/honeybees-comb.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/e/ed/Bienenkoenigin3.jpg"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launch(UI) {
            var tempPhotos = photos.copyOf().toMutableList()
            val url1 = randomUrl(tempPhotos)
            tempPhotos.remove(url1)
            val url2 = randomUrl(tempPhotos)

            main_image_1.imageBitmap = asyncGetBitmap(url1).await()
            main_image_2.imageBitmap = asyncGetBitmap(url2).await()
            main_image_3.imageBitmap = asyncCombineBitmaps(url1, url2).await()
        }
    }

    fun randomUrl(urls: MutableList<String>) : String {
        val random = Random()
        return urls[random.nextInt(urls.size)]
    }

    fun asyncGetBitmap(url: String) = async(CommonPool) {
        val connection = URL(url).openConnection()
        connection.doInput = true
        connection.connect()
        val inputStream = connection.getInputStream()

        Log.d(TAG, "Get bitmap on thread: " + Thread.currentThread().name)

        return@async BitmapFactory.decodeStream(inputStream)
    }

    fun asyncCombineBitmaps(url1: String, url2: String) = async(CommonPool) {
        val bitmap1 = asyncGetBitmap(url1).await()
        val bitmap2 = asyncGetBitmap(url2).await()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.alpha = 120

        Log.d(TAG, "Combine bitmap on thread: " + Thread.currentThread().name)

        val combinedBitmap = Bitmap.createBitmap(bitmap1.width, bitmap1.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(bitmap1, 0f, 0f, null)
        canvas.drawBitmap(bitmap2, 0f, 0f, paint)
        return@async combinedBitmap
    }
}
