package com.example.image_downloader_ce

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.image_downloader_ce.builder.LoadRequest
import com.example.image_downloader_ce.network.ImageLoader
import kotlinx.coroutines.*

/* Sample library to load images asynchronously into an ImageView - First Version - CE */

class ImageFetcherCE private constructor() {

    private val imageLoader : ImageLoader by lazy {
       return@lazy ImageLoader().apply { setCacheDir(ContextCompat.getCodeCacheDir(appContext).absolutePath) }
    }
    private var loadRequestBuilder : LoadRequest.Builder = LoadRequest.Builder(this::onRequestReady)

    // New to Coroutine
    private val scope : CoroutineScope = CoroutineScope(newFixedThreadPoolContext(4, "image_request"))

    private val handlerMain : Handler = Handler(Looper.getMainLooper())


    companion object {
        private var instance : ImageFetcherCE? = null
        internal lateinit var appContext : Context
        fun get(context : Context) : ImageFetcherCE {
            appContext = if(context !is Application)
                context.applicationContext
            else context

            return if(null == instance) {
                instance = ImageFetcherCE()
                instance!!
            } else {
                instance!!
            }
        }
    }

    /**
     * Build an Image Load Request
     * @param url : the url of the image to load
     * @return the load request builder instance
     */
    fun load(url : String) : LoadRequest.Builder {
        return loadRequestBuilder.clear().load(url)
    }

    private fun onRequestReady(loadRequest: LoadRequest, imageView : ImageView) {
        if(imageView.width == 0 || imageView.height == 0) {
            /* If View dimensions are not available, get it post-layout */
            imageView.post {
                loadIntoTargetImageView(checkLoadRequest(loadRequest), imageView)
            }
        } else {
            loadIntoTargetImageView(checkLoadRequest(loadRequest), imageView)
        }

    }

    // Should be called post-layout when the ImageView dimensions are available
    private fun loadIntoTargetImageView(loadRequest: LoadRequest, imageView : ImageView) {
        scope.launch {
            if(loadRequest.useCached) {
                val cachedBitmap = imageLoader.getCachedVersion(getFileNameFromUrl(loadRequest.urlToLoad))
                if(null != cachedBitmap) {
                    handlerMain.post { imageView.setImageBitmap(cachedBitmap) }
                    return@launch
                }
            }

            if(loadRequest.hasLoadingDrawableRes())
                handlerMain.post { imageView.setImageResource(loadRequest.loadingDrawableRes) }

            var bitmap = imageLoader.loadImage(loadRequest.urlToLoad, imageView.width, imageView.height)

            when (bitmap) {
                null -> {
                    if(loadRequest.hasErrorDrawableRes())
                        handlerMain.post { imageView.setImageResource(loadRequest.errorDrawableRes) }
                }
                else -> {
                    if(loadRequest.hasResize()) {
                        bitmap = Bitmap.createScaledBitmap(bitmap, loadRequest.size!!.x, loadRequest.size.y, false)
                    }
                    if(loadRequest.useCached)
                       imageLoader.saveBitmapToCacheForLaterReuse(getFileNameFromUrl(loadRequest.urlToLoad), bitmap!!)

                    handlerMain.post { imageView.setImageBitmap(bitmap) }
                }
            }

        }
    }

    /**
     * Cancel all load request actually running
     */
    fun cancel() {
        scope.cancel()
    }

    private fun getFileNameFromUrl(url : String) = url.substring(url.lastIndexOf("/") + 1, url.length)

    private fun checkLoadRequest(loadRequest: LoadRequest) : LoadRequest {
        when {
            loadRequest.urlToLoad.isEmpty() || !loadRequest.urlToLoad.startsWith("http") -> throw IllegalArgumentException("Url is invalid")
        }

        return loadRequest
    }


}
