# MediaContentProvider
This project demonstrates how to use ContentResolver with MediaStore to fetch images from the device storage and display them in a Jetpack Compose UI. The app requests runtime permissions (supporting both READ_MEDIA_IMAGES for Android 13+ and READ_EXTERNAL_STORAGE for older versions) and loads only the images captured/added in the last 24 hours.
