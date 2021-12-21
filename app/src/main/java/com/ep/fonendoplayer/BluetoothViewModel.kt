package com.ep.fonendoplayer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Advertisement
import com.juul.kable.PeripheralBuilder
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import com.juul.kable.peripheral
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

class BluetoothViewModel(): ViewModel() {

    private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)

    /**
     * Map with bluetooth occurrences
     */
    private val bluetoothMap = hashMapOf<String, Advertisement>()

    private val scanner = Scanner {
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Data
            format = Logging.Format.Multiline
        }
    }

    private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList()) // internal state for the controller
    val advertisements = _advertisements.asStateFlow() // exposed state to the view

    fun scan() {
        viewModelScope.launch {
            withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                scanner.advertisements
                    .catch {  Log.e("BluetoothViewModel", "Error fetching bluetooth: $it") } // kotlin High Order Functions exposes any possible parameter of the lambda as `it`
                    .onEach { advertisement -> bluetoothMap[advertisement.address] = advertisement }
                    .collect{
                       _advertisements.value = bluetoothMap.values.toList()
                    }
            }
        }
    }

    /**
     * @param address - of the device we wanna pair with
     */
    fun pairDevice(address: String) {
        val advertisement = _advertisements.value.find { it.address == address } ?: throw IllegalArgumentException("Invalid Advertisement for address: $address") // no handling error here just throwing an exception
        viewModelScope.launch {
            peripheral(advertisement){
                setUpLogging()
                // TODO immplement connect()

            }
        }

    }

    private fun PeripheralBuilder.setUpLogging() {
        logging {
            level = Logging.Level.Data
            // 2 options to log the I/O data
    //                    data = Hex {
    //                        separator = " "
    //                        lowerCase = false
    //                    }
    //                    data = Logging.DataProcessor { bytes ->
    //                        // Convert `bytes` to desired String representation, for example:
    //                        bytes.joinToString { byte -> byte.toString() } // Show data as integer representation of bytes.
    //                    }
            identifier = "Naran" // prefix just to find our data easily
        }
    }
}