# Image-Downloader-CE
Simple image downloader library to load images asynchronousely



Usage example :

<code>ImageFetcherCE.get(context)
.load("url to load")
      .resize(256, 256)
  .useCachedVersion(true/false)
  .onLoadingShow(R.drawable.loading_example)
  .into(imageView)</code>
	
### Features :

- Load images asynchronousely
- Disk caching
- Resize
- Default images for loading/error state
