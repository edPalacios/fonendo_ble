package com.ep.fonendoplayer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ep.fonendoplayer.decoders.AudioDecoder
import com.ep.fonendoplayer.navigation.ADDRESS
import com.ep.fonendoplayer.navigation.CHARACTERISTICS
import com.ep.fonendoplayer.navigation.SERVICES
import com.ep.fonendoplayer.navigation.SERVICE_UUID
import com.ep.fonendoplayer.utils.play
import com.ep.fonendoplayer.utils.track
import com.juul.kable.*
import com.juul.kable.logs.Logging
import com.juul.kable.logs.SystemLogEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import kotlin.collections.List
import kotlin.collections.emptyList
import kotlin.collections.find
import kotlin.collections.hashMapOf
import kotlin.collections.isNullOrEmpty
import kotlin.collections.joinToString
import kotlin.collections.orEmpty
import kotlin.collections.set
import kotlin.collections.toList

class BluetoothViewModel() : ViewModel() {

    private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)

    /**
     * Map with bluetooth occurrences
     */
    private val bluetoothMap = hashMapOf<String, Advertisement>()

    private lateinit var peripheral: Peripheral

    private val scanner = Scanner {
        logging {
            engine = SystemLogEngine
            level = Logging.Level.Data
            format = Logging.Format.Multiline
        }
    }

    private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList()) // internal state for the controller
    val advertisements = _advertisements.asStateFlow() // exposed state to the view

    private val _error = MutableLiveData<ErrorType>()
    val errorState: LiveData<ErrorType> = _error

    private val _loading = MutableStateFlow(false)
    val loadingState: StateFlow<LoadingState> = _loading

    @FlowPreview
    fun scan() {
        _loading.value = true
        viewModelScope.launch {
            withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                fetchAdvertisements()
            }
        }
    }

    @FlowPreview
    private suspend fun fetchAdvertisements() = scanner.advertisements
        .onEach {advertisement -> bluetoothMap[advertisement.address] = advertisement   }
        .catch {
            _error.value = ErrorType.FetchingBluetoothFailure
            Log.e("BluetoothViewModel", "Error fetching bluetooth: $it")
        }
        .onCompletion { _loading.value = false }
        .collect{
            _advertisements.value = bluetoothMap.values.toList()
        }

    /**
     * @param address - of the device we wanna pair with
     */
    fun connect(address: String, navigate: (NavigationType) -> Unit) {
        _loading.value = true
        val advertisement = _advertisements.value.find { it.address == address } ?: throw IllegalArgumentException("Invalid Advertisement for address: $address") // no handling error here just throwing an exception
        viewModelScope.launch {
            peripheral = peripheral(advertisement) {
                setUpLogging()
                onServicesDiscovered {
                    Log.i("BluetoothViewModel", "onServicesDiscovered" )
                }
            }

            //try to connect with the peripheral. This method will wait until connection is reached before move to peripheral.state.collect magic below
            runCatching { peripheral.connect() } // FIXME here always fails to connect with exception Failed to connect :_(  Could be im always trying to pair with a device that is way to far...
                .onSuccess {
                    _loading.value = false
                    Log.i("BluetoothViewModel", "Connection succeed with peripheral: $peripheral" )
                }
                .onFailure {
                    _loading.value = false
                    _error.value = ErrorType.ConnectionFailed
                    Log.e("BluetoothViewModel", "Error connecting with peripheral: $it")
                }

            peripheral.state.collect { state ->
                Log.i("BluetoothViewModel", "Bluetooth peripheral state: $state")
                // Data from bluetooth is exposed in a kinda like tree structure: listOf(Services(listOf(Characteristics(listOf(Descriptor)))))
                when (state) {
                    State.Connected -> openServicesScreen(peripheral.services.isNullOrEmpty(), address, navigate)
                    else -> {/*no-op, in case i leave commented other states*/}
                }
            }
        }
    }

    private fun openServicesScreen(
        noServices: Boolean,
        address: String,
        navigate: (NavigationType) -> Unit
    ) {
        if (noServices) {
            _error.value = ErrorType.NoServices
        } else {
            navigate(NavigationType.ServicesScreen(address))
        }
    }

    // Data from bluetooth is exposed in a kinda like tree structure: listOf(Services(listOf(Characteristics(listOf(Descriptor)))))
    fun getServices(): List<DiscoveredService> {
        return peripheral.services.orEmpty()
    }

    fun getCharacteristics(serviceUuId: String): List<DiscoveredCharacteristic> {
        return peripheral.services?.find { it.serviceUuid.toString() ==  serviceUuId}?.characteristics.orEmpty()
    }

    fun playData(serviceUuId: String, characteristicUuId: String) {
        viewModelScope.launch {
            val characteristic = requireNotNull(getCharacteristics(serviceUuId).find { it.characteristicUuid.toString() == characteristicUuId }) { "characteristic not found in serviceUuId: $serviceUuId and characteristicUuId: $characteristicUuId" }
            Log.v("BluetoothViewModel", "Observing characteristic: $characteristic")
                peripheral.observe(characteristic) // FIXME never ending loop, sometimes data is not emitted and flow never ends :(
                    .catch {
                        _error.value = ErrorType.PeripheralObserveFailure
                        Log.e("BluetoothViewModel", "Error observing peripheral: $it")
                    }
                    .onCompletion {
                        // Allow 5 seconds for graceful disconnect before forcefully closing `Peripheral`.
//                        withTimeoutOrNull(5000L) { peripheral.disconnect() } // TODO at the moment dont disconnect never
                    }
                    .collect { data ->
                        Log.v("BluetoothViewModel", "Data for characteristic: $characteristic: $data")
                        // TODO process data here to play
                        val decodedData = AudioDecoder().decode(data)
                        track.play(decodedData)
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

sealed class ErrorType(val message: String) {
    object ConnectionFailed: ErrorType("Connection with device failed, try again...")
    object PeripheralObserveFailure: ErrorType("Error while observing data from peripheral")
    object FetchingBluetoothFailure: ErrorType("Cannot fetch bluetooth devices. Check bluetooth is on!!")
    object NoServices: ErrorType("No services available")
}

typealias LoadingState = Boolean

sealed class NavigationType(val route: String){
    data class ServicesScreen(val address: String): NavigationType(SERVICES.replace(ADDRESS, address))
    data class CharacteristicScreen(val serviceUUid: String): NavigationType(CHARACTERISTICS.replace(SERVICE_UUID, serviceUUid))
}
