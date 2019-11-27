package com.example.image_downloader_ce.builder

import android.graphics.Point
import android.widget.ImageView
import androidx.annotation.DrawableRes

class LoadRequest private constructor( val urlToLoad : String = "", @DrawableRes val errorDrawableRes : Int,
                                       @DrawableRes val loadingDrawableRes : Int, val size : Point? = null, val useCached : Boolean){


    fun hasErrorDrawableRes() : Boolean = errorDrawableRes != 0

    fun hasLoadingDrawableRes() : Boolean = loadingDrawableRes != 0

    fun shouldUseCached() : Boolean = useCached

    fun hasResize() : Boolean = null != size


    /**
     * Builder for building an image load request
     */
    class Builder(private val onReadyCallback : (LoadRequest, ImageView) -> Unit) {
        private var urlToLoad : String = ""
        var size : Point? = null
        @DrawableRes
        private var errorDrawableRes : Int = 0
        @DrawableRes
        private var loadingDrawableRes : Int = 0
        /* May be useful until improving the cache for sames image with different dimensions etc.. - true by default */
        private var useCached : Boolean = true

        /**
         * Specify the image url to load
         * @return the load request builder instance
         */
        fun load(url : String) : Builder {
            urlToLoad = url
            return this
        }

        /*
         For internal use only,
         As we use the same Load Request instance,
         We may need to clear previous parameters set
         before reuse
         */
        internal fun clear() : Builder {
            urlToLoad = ""
            size = null
            errorDrawableRes = 0
            loadingDrawableRes = 0
            useCached = true

            return this
        }

        /**
         * Resize the image to the specified dimensions after the image has been downloaded
         * @return the load request builder instance
         */
        fun resize(width : Int, height : Int) : Builder {
            size = Point(width, height)
            return this
        }

        /**
         * Specify a drawable resource to be set in the target while if request has encountered error(s)
         * @return the load request builder instance
         */
        fun onErrorShow(@DrawableRes drawableRes : Int) : Builder {
            errorDrawableRes = drawableRes
            return this
        }

        /**
         * Specify a drawable resource to be set in the target while the request is processing
         * @return the load request builder instance
         */
        fun onLoadingShow(@DrawableRes drawableRes : Int) : Builder {
            loadingDrawableRes = drawableRes
            return this
        }

        /**
         * Whether use or not a cached version if available, the cached version will be the image with the same name
         * which have been downloaded first, eg. : if requesting image1 with size of 200 x 200, it will be the cached version,
         * set to false if you need exceptionally the same image with another dimensions,
         * caching the images improve the load performance
         * @return the load request builder instance
         */
        fun useCachedVersion(cached : Boolean) : Builder {
            useCached = cached
            return this
        }

        /**
         * Specify the target ImageView where the image will be loaded,
         * if the remote image is bigger than the ImageView dimensions,
         * the remote image will be resized
         * @return the load request builder instance
         */
        fun into(imageView : ImageView) {
            onReadyCallback(LoadRequest(urlToLoad, errorDrawableRes, loadingDrawableRes, size, useCached), imageView)
        }
    }


}