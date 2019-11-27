package com.example.image_downloader_ce.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.net.URL
import kotlin.math.ceil
import kotlin.math.min

private const val IMAGE_LOADER_TAG : String = "#ImageDownloader"

internal class ImageLoader {

    private var cacheDirPath : String = ""


    fun loadImage(urlToLoad: String, targetedWidth : Int, targetedHeight : Int) : Bitmap? {

        require(!(targetedWidth == 0 || targetedHeight == 0)) { "Targeted width and targeted height must not be 0" }

        var inputStream : InputStream? = null

            try {
                val url = URL(urlToLoad)
                inputStream = url.openStream()

                val bitmapOption = BitmapFactory.Options()

                // Get the remote image dimensions without loading it in memory
                bitmapOption.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, bitmapOption)

                // Re-initialize the stream for a seconds reading
                inputStream.close()
                inputStream = url.openStream()

                // Prepare to get the image in memory
                bitmapOption.inJustDecodeBounds = false

                /* If the remote image dimensions are bigger than the targeted dimensions, there is no need to load it fully in memory(and caching in disk) */
                if(targetedWidth < bitmapOption.outWidth && targetedHeight < bitmapOption.outHeight) {
                    val inSampleSize = ceil(min(bitmapOption.outWidth.toFloat() / targetedWidth.toFloat(), bitmapOption.outHeight.toFloat() / targetedHeight.toFloat()))
                    println("$IMAGE_LOADER_TAG - remote image is bigger than the targeted dimensions, setting inSampleSize to $inSampleSize")
                    bitmapOption.inSampleSize = inSampleSize.toInt()
                } else {
                    /* May add logic for others cases here such as fit(resize bigger) */
                    println("$IMAGE_LOADER_TAG - no resizing as the remote image dimensions are not bigger than the targeted dimensions")
                }

                val bitmap = BitmapFactory.decodeStream(inputStream, null, bitmapOption)

                println("$IMAGE_LOADER_TAG - downloaded image with width ${bitmapOption.outWidth} and height ${bitmapOption.outHeight} " +
                        "for targeted width $targetedWidth and targeted height $targetedHeight")


                return bitmap
            }
            catch (e : Exception) {
                println("$IMAGE_LOADER_TAG - error catched while loading an image ${e.localizedMessage}")
                return null
            } finally {
                inputStream?.close()
            }
    }

    /* Caching images is a key point for better performance in especially in components such as RecyclerView */
    fun saveBitmapToCacheForLaterReuse(fileName : String, bitmap : Bitmap) {
        val file = File("$cacheDirPath/$fileName")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream)
    }

    fun setCacheDir(cacheFileDir : String) {
        this.cacheDirPath = cacheFileDir
    }

    /* ToDo : change the way we identify the images,
    *   Also, implement a caching policy for when to remove/download again(eg. : if cached version is not the same dimensions than the new target) */
    fun getCachedVersion(fileName : String) : Bitmap? {
        val cachedFile = File("$cacheDirPath/${fileName}")
        println("$IMAGE_LOADER_TAG - cached version ? ${cachedFile.exists()}")
        val bitmap = BitmapFactory.decodeFile(cachedFile.absolutePath)

        return if(cachedFile.exists() && null != bitmap) bitmap  else null
    }

}