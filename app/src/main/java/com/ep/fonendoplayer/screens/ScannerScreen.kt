package com.ep.fonendoplayer.screens

import android.widget.Toast
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
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ep.fonendoplayer.BluetoothViewModel
import com.ep.fonendoplayer.LoadingState
import com.juul.kable.Advertisement
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ep.fonendoplayer.ui.theme.FonendoPlayerTheme
import com.ep.fonendoplayer.utils.play
import com.ep.fonendoplayer.utils.track
import kotlinx.coroutines.flow.FlowCollector

// scanner screen with list of bluetooth devices found
@ExperimentalAnimationApi
@Composable
fun ScannerScreen(viewModel: BluetoothViewModel = viewModel()) {
    Scan({ viewModel.scan() }, viewModel.advertisements, { viewModel.connect(it)})
    Loading(viewModel.loadingState)
    Error(viewModel)
    PlayBack(viewModel)
}

@Composable
fun PlayBack(viewModel: BluetoothViewModel) {
    val byteArray by viewModel.playbackState.observeAsState()
    byteArray?.let { track.play(it) }
}

// Just show a toast with a message
@Composable
fun Error(viewModel: BluetoothViewModel) {
    val state by viewModel.errorState.observeAsState()
    state?.message.takeIf { !it.isNullOrBlank() }?.run {
        Toast.makeText(LocalContext.current, this, Toast.LENGTH_LONG).show()
    }
}

/**
 * This view has a scan button with a list o devices (bluetooth occurrences)
 */
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

        BluetoothDevicesList(advertisements, onItemSelected)
    }

}

/**
 * List with bluetooth devices
 */
@Composable
fun BluetoothDevicesList(advertisements: StateFlow<List<Advertisement>>, onItemSelected: (String) -> Unit) {
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

@ExperimentalAnimationApi
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
