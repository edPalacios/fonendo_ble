package com.ep.fonendoplayer

import android.Manifest
import android.bluetooth.le.ScanResult
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.juul.kable.Advertisement
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Needed to make bluetooth work: to find the devices we need to access the location of the user, so permission is requried to the user
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1234) // ignore the request code but be sure you dont deny permissions!!

        // the controller for the data of the screen
        val viewModel = viewModels<BluetoothViewModel>().value

        // inside here we build the view
        setContent {
            FonendoPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Scan({ viewModel.scan() }, viewModel.advertisements)
                }
            }
        }
    }
}

@Composable
fun Scan(scan: () -> Unit, advertisements: StateFlow<List<Advertisement>>) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp, 16.dp, 16.dp, 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            onClick = { scan() }
        ) {
            Text(text = "Scan")
        }

        BluetoothDevices(advertisements)
    }

}

/**
 * List with bluetooth devices
 */
@Composable
fun BluetoothDevices(advertisements: StateFlow<List<Advertisement>>) {
    val state = advertisements.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        items(items = state.value, itemContent = {
            Row(
                modifier = Modifier
                    .padding(4.dp) // margin
                    .border(BorderStroke(1.dp, Color.Gray))
                    .fillMaxWidth()
                    .padding(8.dp) // padding
            ) {
                Text(text = "$it")
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val advertisement = object : StateFlow<List<Advertisement>> {
        override val replayCache: List<List<Advertisement>>
            get() = emptyList()
        override val value: List<Advertisement>
            get() = emptyList()

        override suspend fun collect(collector: FlowCollector<List<Advertisement>>): Nothing {
            throw IllegalArgumentException("just a preview")
        }
    }
    FonendoPlayerTheme {
        Scan({ }, advertisement)
    }
}
