package com.ep.fonendoplayer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ep.fonendoplayer.decoders.AudioDecoder
import com.juul.kable.*
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

class BluetoothViewModel : ViewModel() {

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

    private val _error = MutableLiveData<ErrorState>()
    val errorState: LiveData<ErrorState> = _error

    private val _loading = MutableStateFlow(false)
    val loadingState: StateFlow<LoadingState> = _loading

    private val _playback = MutableLiveData<ByteArray>()
    val playbackState : LiveData<ByteArray> = _playback

    fun scan() {
        _loading.value = true
        viewModelScope.launch {
            withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                scanner.advertisements
                    .catch {
                        _loading.value = false
                        _error.value = ErrorState.FetchingBluetoothFailure
                        Log.e("BluetoothViewModel", "Error fetching bluetooth: $it")
                    } // kotlin High Order Functions exposes any possible parameter of the lambda as `it`
                    .onEach { advertisement -> bluetoothMap[advertisement.address] = advertisement }
                    .onCompletion { _loading.value = false }
                    .collect {
                        _advertisements.value = bluetoothMap.values.toList()
                    }
            }
        }
    }

    /**
     * @param address - of the device we wanna pair with
     */
    fun pairDevice(address: String) {
        _loading.value = true
        val advertisement = _advertisements.value.find { it.address == address } ?: throw IllegalArgumentException("Invalid Advertisement for address: $address") // no handling error here just throwing an exception
        viewModelScope.launch {
            val peripheral = peripheral(advertisement) {
                setUpLogging()
                onServicesDiscovered {
                    Log.i("BluetoothViewModel", "onServicesDiscovered" )
                }
            }

            //try to connect with the peripheral. This method will wait until connection is reached before move to peripheral.state.collect magic below
            runCatching { peripheral.connect() } // FIXME here always fails to connect with exception Failed to connect :_(  Could be im always trying to pair with a device that is way to far...
                .onSuccess {  Log.i("BluetoothViewModel", "Connection succeed with peripheral: $peripheral" ) }
                .onFailure {
                    _loading.value = false
                    _error.value = ErrorState.ConnectionFailed
                    Log.e("BluetoothViewModel", "Error connecting with peripheral: $it")
                }

            peripheral.state.collect { state ->
                Log.i("BluetoothViewModel", "Bluetooth peripheral state: $state")
                when (state) {
                    State.Connected -> fetchData(peripheral)
                    else -> {/*no-op, in case i leave commented other states*/}
//                    State.Disconnecting ->
//                    is State.Disconnected ->
//                    State.Connecting.Bluetooth -> TODO()
//                    State.Connecting.Services -> TODO()
//                    State.Connecting.Observes -> TODO()
                }
            }
        }
    }

    private suspend fun fetchData(peripheral: Peripheral) {
        // Data from bluetooth is exposed in a kinda like tree structure: listOf(Services(listOf(Characteristics(listOf(Descriptor)))))
        val characteristicList = peripheral.services?.flatMap { service ->
            service.characteristics.map { characteristic ->
                characteristicOf(
                    service = characteristic.serviceUuid.toString(),
                    characteristic = characteristic.characteristicUuid.toString()
                )
            }
        }.orEmpty()

        characteristicList.forEach {
            peripheral.observe(it)
                .catch {
                    _loading.value = false
                    _error.value = ErrorState.PeripheralObserveFailure
                    Log.e("BluetoothViewModel", "Error observing peripheral: $it")
                }
                .onCompletion {
                    // Allow 5 seconds for graceful disconnect before forcefully closing `Peripheral`.
                    _loading.value = false
                    withTimeoutOrNull(5000L) { peripheral.disconnect() }
                }
                .collect { data ->
                    Log.v("BluetoothViewModel", "Data for characteristic: $it: $data")
                    // TODO process data here to play
                    val decodedData = AudioDecoder().decode(data)
                    _playback.value = decodedData
                }
        }
    }

    private fun PeripheralBuilder.setUpLogging() {
        logging {
            level = Logging.Level.Data
            // 2 options to log the I/O data
//                                data = Hex {
//                                    separator = " "
//                                    lowerCase = false
//                                }
                                data = Logging.DataProcessor { bytes ->
                                    // Convert `bytes` to desired String representation, for example:
                                    bytes.joinToString { byte -> byte.toString() } // Show data as integer representation of bytes.
                                }
            identifier = "Naran" // prefix just to find our data easily
        }
    }
}

sealed class ErrorState(val message: String) {
    object ConnectionFailed: ErrorState("Connection with device failed, try again...")
    object PeripheralObserveFailure: ErrorState("Error while observing data from peripheral")
    object FetchingBluetoothFailure: ErrorState("Cannot fetch bluetooth devices. Check bluetooth is on!!")
}

typealias LoadingState = Boolean