package com.example.mediacontentprovider

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.mediacontentprovider.ui.theme.MediaContentProviderTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ImageVM>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    loadImages()
                }
            }

        val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                this,
                requiredPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadImages()
        } else {
            requestPermissionLauncher.launch(requiredPermission)
        }
        enableEdgeToEdge()
        setContent {
            MediaContentProviderTheme {
                Surface{
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        Modifier.fillMaxSize().padding(top = 32.dp , bottom = 32.dp)) {
                        items(viewModel.images){
                            ImageItem(it)
                        }
                    }
                }
            }
        }
    }
    private fun loadImages() {
        val projections = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN
        )
        val selection = MediaStore.Images.Media.DATE_TAKEN + " >= ?"
        val prevDay= Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR,-1)
        }.timeInMillis
        val selectionArgs = arrayOf(prevDay.toString())
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projections,
            selection,
            selectionArgs,
            sortOrder
        )?.use{ cursor-> //used tp iterate over large data sets
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val images = mutableListOf<Image>()
            while (cursor.moveToNext()){
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(displayNameColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                images.add(Image(id,name,uri))
            }
            viewModel.updateImages(images)
        }
    }
}


@Composable
fun ImageItem(image: Image) {
    Card(modifier = Modifier.padding(8.dp)) {
        AsyncImage(
            model = image.uri,
            contentDescription = null,
            modifier = Modifier.padding(8.dp).height(150.dp).width(150.dp),
            contentScale = ContentScale.Crop
        )
        Text(text = image.name, modifier = Modifier.padding(8.dp))
    }

}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MediaContentProviderTheme {
        ImageItem(Image(1,"name", Uri.EMPTY))
    }
}

data class Image(
    val id: Long,
    val name: String,
    val uri : Uri
)
