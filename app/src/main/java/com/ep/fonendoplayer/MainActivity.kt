package com.ep.fonendoplayer

import android.Manifest
import android.content.Context
import android.media.*
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.ep.fonendoplayer.ui.theme.FonendoPlayerTheme
import java.io.*
import java.lang.Exception
import java.util.*
import android.media.AudioManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ep.fonendoplayer.decoders.AudioDecoder
import com.ep.fonendoplayer.decoders.AudioDecoder2


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FonendoPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Column(Modifier.fillMaxWidth().fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Hello $name!")
        Button(
            onClick = {}
        ) {
            Text(text = "play me")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FonendoPlayerTheme {
        Greeting("Android")
    }
}
