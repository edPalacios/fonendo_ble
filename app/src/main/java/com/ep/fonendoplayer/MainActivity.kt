package com.ep.fonendoplayer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ep.fonendoplayer.ui.theme.FonendoPlayerTheme
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needed to make bluetooth work: to find the devices we need to access the location of the user, so permission is requried to the user
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1234) // ignore the request code but be sure you dont deny permissions!!
        val viewModel = viewModels<BluetoothViewModel>().value


        setContent {
            FonendoPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Scan(viewModel)
                }
            }
        }
    }
}

@Composable
fun Scan(viewModel: BluetoothViewModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp, 16.dp, 16.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            modifier =  Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            onClick = {
                viewModel.scan()
            }
        ) {
            Text(text = "Scan")
        }

        BluetoothDevices(viewModel = viewModel)
    }

}

@Composable
fun BluetoothDevices(viewModel: BluetoothViewModel) {
    val state = viewModel.advertisements.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxHeight()){
        items(items = state.value, itemContent = {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(text = "$it")
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FonendoPlayerTheme {
//        Greeting("Android", viewModels<BluetoothViewModel>().value)
    }
}
