package com.ep.fonendoplayer

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.tooling.preview.Preview
import com.ep.fonendoplayer.ui.theme.FonendoPlayerTheme
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.ep.fonendoplayer.utils.play
import com.ep.fonendoplayer.utils.playFonendo
import com.ep.fonendoplayer.utils.playThunder
import com.ep.fonendoplayer.utils.track
import com.juul.kable.Advertisement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


@ExperimentalAnimationApi
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
                    Scan({ viewModel.scan() }, viewModel.advertisements, { viewModel.pairDevice(it)})
                    Loading(viewModel.loadingState)
                }
            }
        }
        observeErrors(viewModel)
        observePlayBack(viewModel)

    }

    private fun observePlayBack(viewModel: BluetoothViewModel) {
        viewModel.playbackState.observe(this) {
            track.play(it)
        }
    }

    // Just show a toast with a message
    private fun observeErrors(viewModel: BluetoothViewModel) {
        viewModel.errorState.observe(this) { errorState ->
            errorState.message.takeIf { it.isNotEmpty() }?.run {
                Toast.makeText(applicationContext, this, Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun Scan(scan: () -> Unit, advertisements: StateFlow<List<Advertisement>>, onItemSelected: (String) -> Unit) {
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

        BluetoothDevices(advertisements, onItemSelected)
    }

}

/**
 * List with bluetooth devices
 */
@Composable
fun BluetoothDevices(advertisements: StateFlow<List<Advertisement>>, onItemSelected: (String) -> Unit) {
    var selectedItem by remember { mutableStateOf("") } //state keeps the address of the bluetooth
    val state = advertisements.collectAsState()
    val isSelected : (String, String) -> Boolean = {item, selected -> item == selected}

    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        items(items = state.value, itemContent = {
            Row(
                modifier = Modifier
                    .padding(4.dp) // margin
                    .border(
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = if (isSelected(
                                it.address,
                                selectedItem
                            )
                        ) Color.Blue else Color.White, shape = RoundedCornerShape(4.dp)
                    )
                    .fillMaxWidth()
                    .padding(8.dp) // padding
                    .clickable {
                        selectedItem = it.address
                        onItemSelected(selectedItem)
                    }
            ) {
                Text(text = "$it.", color = if(isSelected(it.address,selectedItem)) Color.White else Color.Black)
            }
        })
    }
}

@ExperimentalAnimationApi
@Composable
fun Loading(loadingState: StateFlow<LoadingState>) {
    val state = loadingState.collectAsState()
    AnimatedVisibility(visible = state.value) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
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
        Scan({ }, advertisement, {})
    }
}
