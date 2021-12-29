package com.ep.fonendoplayer.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ep.fonendoplayer.BluetoothViewModel
import com.ep.fonendoplayer.utils.play
import com.ep.fonendoplayer.utils.track
import com.juul.kable.Characteristic
import com.juul.kable.DiscoveredService
import kotlinx.coroutines.FlowPreview

// screen with characteristics of some service

@ExperimentalAnimationApi
@Composable
fun CharacteristicsScreen(viewModel: BluetoothViewModel = viewModel(), serviceUuId: String) {
    CharacteristicList(services = viewModel.getCharacteristics(serviceUuId), onItemSelected = { characteristicUuId ->
       viewModel.playData(serviceUuId, characteristicUuId)
    })
    Loading(loadingState = viewModel.loadingState)
}

@Composable
fun CharacteristicList(services: List<Characteristic>, onItemSelected: (String) -> Unit) {
    var selectedItem by remember { mutableStateOf("") }
    val isSelected : (String, String) -> Boolean = {item, selected -> item == selected}

    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        items(items = services, itemContent = {
            Row(
                modifier = Modifier
                    .padding(4.dp) // margin
                    .border(
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = if (isSelected(
                                it.characteristicUuid.toString(),
                                selectedItem
                            )
                        ) Color.Blue else Color.White, shape = RoundedCornerShape(4.dp)
                    )
                    .fillMaxWidth()
                    .padding(8.dp) // padding
                    .clickable {
                        selectedItem = it.characteristicUuid.toString()
                        onItemSelected(selectedItem)
                    }
            ) {
                Text(text = "$it.", color = if(isSelected(it.characteristicUuid.toString(), selectedItem)) Color.White else Color.Black)
            }
        })
    }
}